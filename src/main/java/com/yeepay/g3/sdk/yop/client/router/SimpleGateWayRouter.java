package com.yeepay.g3.sdk.yop.client.router;

import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yeepay.g3.sdk.yop.client.ClientReporter;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.metric.report.host.YopHostStatusChangePayload;
import com.yeepay.g3.sdk.yop.client.metric.report.host.YopHostStatusChangeReport;
import com.yeepay.g3.sdk.yop.config.AppSdkConfig;
import com.yeepay.g3.sdk.yop.config.AppSdkConfigProviderRegistry;
import com.yeepay.g3.sdk.yop.config.enums.ModeEnum;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.invoke.model.UriResource;
import com.yeepay.g3.sdk.yop.sentinel.YopSph;
import com.yeepay.g3.sdk.yop.utils.CharacterConstants;
import com.yeepay.g3.sdk.yop.utils.RouteUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * title: 简单网关路由<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2019<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-03-13 10:40
 */
public class SimpleGateWayRouter implements GateWayRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleGateWayRouter.class);

    private static final Map<ServerRootType, CopyOnWriteArrayList<URI>> ALL_SERVER = Maps.newConcurrentMap();
    private static final Map<URI, Set<ServerRootType>> ALL_SERVER_TYPES = Maps.newConcurrentMap();
    private static final Map<ServerRootType, URI> MAIN_SERVER = Maps.newConcurrentMap();
    private static final Map<ServerRootType, List<URI>> BACKUP_SERVERS = Maps.newConcurrentMap();
    private static final YopSph.BlockResourcePool BLOCK_SERVER_POOL = new YopSph.BlockResourcePool();
    private static final String SYSTEM_SDK_MODE_KEY = "yop.sdk.mode";
    private static final String SANDBOX_APP_ID_PREFIX = "sandbox_";

    private static final List<ServerRootType> MANUAL_SERVER_ROOT_TYPES = Lists.newArrayList(ServerRootType.COMMON, ServerRootType.YOS);

    static {
        monitorServerRoot();
    }

    private static void monitorServerRoot() {
        // sentinel监控
        EventObserverRegistry.getInstance().addStateChangeObserver("BLOCKED_SERVERS_CHANGED",
                (prevState, newState, rule, snapshotValue) -> {
                    try {
                        final UriResource uriResource = UriResource.parseResourceKey(rule.getResource());
                        final URI serverRoot = uriResource.getResource();
                        LOGGER.info("ServerRoot Block State Changed, serverRoot:{}, old:{}, new:{}, rule:{}",
                                serverRoot, prevState, newState, rule);
                        Set<ServerRootType> serverRootTypes = ALL_SERVER_TYPES.get(serverRoot);
                        Set<String> serverTypes = CollectionUtils.isEmpty(serverRootTypes) ? Collections.emptySet() :
                                serverRootTypes.stream().map(ServerRootType::name).collect(Collectors.toSet());
                        BLOCK_SERVER_POOL.onServerStatusChange(uriResource, prevState, newState, rule, serverTypes);
                        // 异步上报
                        ClientReporter.asyncReportToQueue(new YopHostStatusChangeReport(
                                new YopHostStatusChangePayload(serverRoot.toString(), prevState.name(), newState.name(), rule.toString())));
                    } catch (Exception e) {
                        LOGGER.warn("UnexpectedError, MonitorServerRoot ex:", e);
                    }
                });
    }

    private final ServerRootSpace space;

    private final Set<String> independentApiGroups;

    private final ModeEnum systemMode;

    public SimpleGateWayRouter(ServerRootSpace space) {
        this.space = space;
        this.independentApiGroups = Collections.unmodifiableSet(Sets.newHashSet("bank-encryption"));
        String systemModeConfig = System.getProperty(SYSTEM_SDK_MODE_KEY);
        this.systemMode = StringUtils.isEmpty(systemModeConfig) ? null : ModeEnum.valueOf(systemModeConfig);
        addServerRoots(space);
    }

    private static void addServerRoots(ServerRootSpace space) {
        if (CollectionUtils.isNotEmpty(space.getPreferredEndPoint())) {
            for (String uri : space.getPreferredEndPoint()) {
                addServerRoot(URI.create(uri), ServerRootType.COMMON);
            }
        }

        if (CollectionUtils.isNotEmpty(space.getPreferredYosEndPoint())) {
            for (String uri : space.getPreferredYosEndPoint()) {
                addServerRoot(URI.create(uri), ServerRootType.YOS);
            }
        }
        addServerRoot(space.getYosServerRootURL(), ServerRootType.YOS);
    }

    private static boolean addServerRoot(URI serverRoot, ServerRootType serverRootType) {
        if (null != serverRoot) {
            ALL_SERVER_TYPES.computeIfAbsent(serverRoot, p -> Sets.newHashSet()).add(serverRootType);
            final CopyOnWriteArrayList<URI> serverRoots = ALL_SERVER.computeIfAbsent(serverRootType, p -> Lists.newCopyOnWriteArrayList());
            return serverRoots.addIfAbsent(serverRoot);
        }
        return false;
    }

    @Override
    public UriResource route(String apiUri, YopRequest request, List<URI> excludeServerRoots) {
        final String appKey = request.getAppSdkConfig().getAppKey();
        if (isAppInSandbox(appKey)) {
            return new UriResource(space.getSandboxServerRootURL());
        }

        final ServerRootType serverRootType = isYosRequest(apiUri, request) ? ServerRootType.YOS : ServerRootType.COMMON;

        // 独立网关，依然走openapi，serviceName是apiGroup的变形，需要还原
        String apiGroup = extractApiGroupFromApiUri(apiUri);
        if (independentApiGroups.contains(apiGroup)) {
            final URI independentServerRoot = independentServerRoot(serverRootType, apiGroup, request);
            if (isExcludeServerRoots(independentServerRoot, excludeServerRoots)) {
                throw new YopClientException("Config Error, ServerRoot excluded:" + independentServerRoot);
            }
            return new UriResource(independentServerRoot);
        }

        final CopyOnWriteArrayList<URI> serverRoots = ALL_SERVER.get(serverRootType);
        if (CollectionUtils.isEmpty(serverRoots)) {
            throw new YopClientException("Config Error, No ServerRoot Found, type:" + serverRootType);
        }

        // 主域名准备
        URI mainServer = MAIN_SERVER.get(serverRootType);
        // 随机选主
        if (null == mainServer) {
            final List<URI> randomList = RouteUtils.randomList(serverRoots);
            if (recordMainServer(randomList.remove(0), serverRootType)) {
                BACKUP_SERVERS.put(serverRootType, randomList);
            }
            mainServer = MAIN_SERVER.get(serverRootType);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Main ServerRoot Set, value:{}", mainServer);
            }
        }

        // 主域名正常
        if (null != mainServer && !isExcludeServerRoots(mainServer, excludeServerRoots)) {
            return new UriResource(mainServer);
        }

        // 主域名故障，临时启用备选域名
        final List<URI> backupServers = BACKUP_SERVERS.get(serverRootType);
        if (CollectionUtils.isNotEmpty(backupServers)) {
            for (URI backup : backupServers) {
                if (!isExcludeServerRoots(backup, excludeServerRoots)) {
                    return new UriResource(backup);
                }
            }
        }

        // 备用域名故障，选用最早故障的域名
        return BLOCK_SERVER_POOL.select(serverRootType.name(), mainServer);
    }

    private boolean isExcludeServerRoots(URI serverRoot, List<URI> excludeServerRoots) {
        return null != excludeServerRoots && null != serverRoot && excludeServerRoots.contains(serverRoot);
    }

    private URI independentServerRoot(ServerRootType serverRootType, String apiGroup, YopRequest request) {
        final boolean isYosRequest = ServerRootType.YOS.equals(serverRootType);
        URI serverRootURL = isYosRequest ? space.getYosServerRootURL() : space.getServerRootURL();
        try {
            return new URI(serverRootURL.getScheme(), serverRootURL.getUserInfo(),
                    getIndependentApiGroupHost(apiGroup, serverRootURL.getHost(), isYosRequest),
                    serverRootURL.getPort(), serverRootURL.getPath(), serverRootURL.getQuery(), serverRootURL.getFragment());
        } catch (URISyntaxException e) {
            throw new YopClientException("route request failure");
        }
    }

    private boolean isYosRequest(String apiUri, YopRequest request) {
        boolean isYosRequest = false;
        if (MapUtils.isNotEmpty(request.getMultipartFiles())) {
            isYosRequest = true;
            return isYosRequest;
        }
        if (StringUtils.startsWith(apiUri, "/yos")) {
            isYosRequest = true;
            return isYosRequest;
        }
        return isYosRequest;
    }

    private String extractApiGroupFromApiUri(String apiUri) {
        int startIndex = StringUtils.ordinalIndexOf(apiUri, CharacterConstants.SLASH, 3);
        if (startIndex < -1 || startIndex == apiUri.length() - 1) {
            throw new YopClientException("illegal apiUri:" + apiUri);
        }
        int endIndex = StringUtils.indexOf(apiUri, CharacterConstants.SLASH, startIndex + 1);
        if (endIndex < -1) {
            throw new YopClientException("illegal apiUri:" + apiUri);
        }
        return StringUtils.mid(apiUri, startIndex + 1, endIndex - startIndex - 1);
    }

    private boolean isAppInSandbox(String appKey) {
        if (systemMode == null) {
            AppSdkConfig appSdkConfig = AppSdkConfigProviderRegistry.getProvider().getConfig(appKey);
            if (appSdkConfig == null) {
                return false;
            }
            return appSdkConfig.getMode() == ModeEnum.sandbox;
        }
        return systemMode == ModeEnum.sandbox;
    }

    private static String getIndependentApiGroupHost(String apiGroup, String originHost, boolean isYosRequest) {
        if (isYosRequest) {
            return originHost;
        }
        int index = StringUtils.indexOf(originHost, CharacterConstants.DOT);
        return StringUtils.substring(originHost, 0, index) + CharacterConstants.DASH_LINE + apiGroup + StringUtils.substring(originHost, index);
    }

    private static boolean recordMainServer(URI serverRoot, ServerRootType serverRootType) {
        return recordMainServer(serverRoot, serverRootType, false);
    }

    private static boolean recordMainServer(URI serverRoot, ServerRootType serverRootType, boolean force) {
        if (null == serverRoot) {
            throw new YopClientException("Config Error, No ServerRoot Found, type:" + serverRootType);
        }
        final URI oldMain = MAIN_SERVER.putIfAbsent(serverRootType, serverRoot);
        if (null != oldMain) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Main ServerRoot Already Set, value:{}", oldMain);
            }
            if (force) {
                MAIN_SERVER.put(serverRootType, serverRoot);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Main ServerRoot Switched, old:{}, new:{}", oldMain, serverRoot);
                }
                return true;
            }
            return false;
        }
        return true;
    }

    private enum ServerRootType {
        COMMON,
        YOS,
        SANDBOX
    }
}
