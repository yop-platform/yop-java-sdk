package com.yeepay.g3.sdk.yop.client.router;

import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.config.AppSdkConfig;
import com.yeepay.g3.sdk.yop.config.AppSdkConfigProviderRegistry;
import com.yeepay.g3.sdk.yop.config.enums.ModeEnum;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
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
import java.util.concurrent.LinkedBlockingDeque;

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

    private static final String SYSTEM_SDK_MODE_KEY = "yop.sdk.mode";

    private static final Map<ServerRootType, CopyOnWriteArrayList<String>> ALL_SERVER = Maps.newConcurrentMap();
    private static final Map<String, Set<ServerRootType>> ALL_SERVER_TYPES = Maps.newConcurrentMap();
    private static final Map<ServerRootType, String> MAIN_SERVER = Maps.newConcurrentMap();
    private static final Map<ServerRootType, List<String>> BACKUP_SERVERS = Maps.newConcurrentMap();
    private static final Map<ServerRootType, LinkedBlockingDeque<String>> BLOCKED_SERVERS = Maps.newConcurrentMap();

    private static final List<ServerRootType> MANUAL_SERVER_ROOT_TYPES = Lists.newArrayList(ServerRootType.COMMON, ServerRootType.YOS);

    static {
        monitorServerRoot();
    }

    private static void monitorServerRoot() {
        EventObserverRegistry.getInstance().addStateChangeObserver("BLOCKED_SERVERS_CHANGED",
                (prevState, newState, rule, snapshotValue) -> {
                    final String serverRoot = rule.getResource();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("ServerRoot Block State Changed, value:{}, old:{}, new:{}", serverRoot, prevState, newState);
                    }
                    Set<ServerRootType> serverRootTypes = ALL_SERVER_TYPES.get(serverRoot);
                    if (CollectionUtils.isNotEmpty(serverRootTypes)) {
                        for (ServerRootType serverRootType : serverRootTypes) {
                            switch (newState) {
                                case OPEN:
                                    final LinkedBlockingDeque<String> oldBlocked =
                                            BLOCKED_SERVERS.computeIfAbsent(serverRootType, p -> new LinkedBlockingDeque<>());
                                    oldBlocked.removeIf(serverRoot::equals);
                                    oldBlocked.add(serverRoot);
                                    break;
                                case CLOSED:
                                    final LinkedBlockingDeque<String> blockedServers = BLOCKED_SERVERS.get(serverRootType);
                                    if (null != blockedServers) {
                                        blockedServers.removeIf(serverRoot::equals);
                                    }
                                    break;
                                default:
                            }
                        }
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
                addServerRoot(uri, ServerRootType.COMMON);
            }
        }
        addServerRoot(space.getServerRoot(), ServerRootType.COMMON);

        if (CollectionUtils.isNotEmpty(space.getPreferredYosEndPoint())) {
            for (String uri : space.getPreferredYosEndPoint()) {
                addServerRoot(uri, ServerRootType.YOS);
            }
        }
        addServerRoot(space.getYosServerRoot(), ServerRootType.YOS);
    }

    private static boolean addServerRoot(String serverRoot, ServerRootType serverRootType) {
        if (null != serverRoot) {
            ALL_SERVER_TYPES.computeIfAbsent(serverRoot, p -> Sets.newHashSet()).add(serverRootType);
            final CopyOnWriteArrayList<String> serverRoots = ALL_SERVER.computeIfAbsent(serverRootType, p -> Lists.newCopyOnWriteArrayList());
            return serverRoots.addIfAbsent(serverRoot);
        }
        return false;
    }

    private static void addServerRoot(String serverRoot, List<ServerRootType> serverRootTypes) {
        if (null != serverRoot && CollectionUtils.isNotEmpty(serverRootTypes)) {
            for (ServerRootType serverRootType : serverRootTypes) {
                addServerRoot(serverRoot, serverRootType);
            }
        }
    }

    @Override
    public String route(String apiUri, YopRequest request) {
        String serverRoot;
        if (isAppInSandbox(request.getAppSdkConfig().getAppKey())) {
            serverRoot = space.getSandboxServerRoot();
        } else {
            String apiGroup = extractApiGroupFromApiUri(apiUri);
            if (independentApiGroups.contains(apiGroup)) {
                boolean isYosRequest = isYosRequest(apiUri, request);
                URI serverRootURL = isYosRequest ? space.getYosServerRootURL() : space.getServerRootURL();
                URI independentServerRootURL;
                try {
                    independentServerRootURL = new URI(serverRootURL.getScheme(), serverRootURL.getUserInfo(),
                            getIndependentApiGroupHost(apiGroup, serverRootURL.getHost(), isYosRequest),
                            serverRootURL.getPort(), serverRootURL.getPath(), serverRootURL.getQuery(), serverRootURL.getFragment());
                } catch (URISyntaxException e) {
                    throw new YopClientException("route request failure");
                }
                serverRoot = independentServerRootURL.toString();
            } else {
                serverRoot = isYosRequest(apiUri, request) ? space.getYosServerRoot() : space.getServerRoot();
            }
        }
        return serverRoot;
    }

    @Override
    public String route(String apiUri, YopRequest request, List<String> excludeServerRoots) {
        final String appKey = request.getAppSdkConfig().getAppKey();
        if (isAppInSandbox(appKey)) {
            return space.getSandboxServerRoot();
        }

        final ServerRootType serverRootType = isYosRequest(apiUri, request) ? ServerRootType.YOS : ServerRootType.COMMON;

        // 独立网关，依然走openapi，serviceName是apiGroup的变形，需要还原
        String apiGroup = extractApiGroupFromApiUri(apiUri);
        if (independentApiGroups.contains(apiGroup)) {
            final String independentServerRoot = independentServerRoot(serverRootType, apiGroup, request);
            if (isExcludeServerRoots(independentServerRoot, excludeServerRoots)) {
                throw new YopClientException("Config Error, ServerRoot excluded:" + independentServerRoot);
            }
            return independentServerRoot;
        }

        // 主域名
        String mainServer = MAIN_SERVER.get(serverRootType);
        if (null != mainServer && !isExcludeServerRoots(mainServer, excludeServerRoots)) {
            return mainServer;
        }

        final CopyOnWriteArrayList<String> serverRoots = ALL_SERVER.get(serverRootType);
        if (CollectionUtils.isEmpty(serverRoots)) {
            throw new YopClientException("Config Error, No ServerRoot Found, type:" + serverRootType);
        }

        // 随机选主
        if (null == mainServer) {
            final List<String> randomList = RouteUtils.randomList(serverRoots);
            mainServer = randomList.remove(0);
            if (recordMainServer(mainServer, serverRootType)) {
                BACKUP_SERVERS.put(serverRootType, randomList);
            }
            return MAIN_SERVER.get(serverRootType);
        }

        // 主域名故障，临时启用备选域名
        final List<String> backupServers = BACKUP_SERVERS.get(serverRootType);
        if (CollectionUtils.isNotEmpty(backupServers)) {
            for (String backup : backupServers) {
                if (!isExcludeServerRoots(backup, excludeServerRoots)) {
                    return backup;
                }
            }
        }

        // 备用域名故障，选用最早故障的域名
        final LinkedBlockingDeque<String> failedServers = BLOCKED_SERVERS.get(serverRootType);
        String oldestFailServer = null;
        if (null != failedServers && !failedServers.isEmpty()) {
            oldestFailServer = failedServers.peek();
        }

        // 主域名兜底
        return null != oldestFailServer ? oldestFailServer : mainServer;
    }

    private boolean isExcludeServerRoots(String serverRoot, List<String> excludeServerRoots) {
        return null != excludeServerRoots && null != serverRoot && excludeServerRoots.contains(serverRoot);
    }

    private String independentServerRoot(ServerRootType serverRootType, String apiGroup, YopRequest request) {
        final boolean isYosRequest = ServerRootType.YOS.equals(serverRootType);
        URI serverRootURL = isYosRequest ? space.getYosServerRootURL() : space.getServerRootURL();
        try {
            return new URI(serverRootURL.getScheme(), serverRootURL.getUserInfo(),
                    getIndependentApiGroupHost(apiGroup, serverRootURL.getHost(), isYosRequest),
                    serverRootURL.getPort(), serverRootURL.getPath(), serverRootURL.getQuery(), serverRootURL.getFragment()).toString();
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

    private static boolean recordMainServer(String serverRoot, ServerRootType serverRootType) {
        return recordMainServer(serverRoot, serverRootType, false);
    }

    private static void recordMainServer(String serverRoot, List<ServerRootType> serverRootTypes, boolean force) {
        if (CollectionUtils.isEmpty(serverRootTypes)) {
            throw new YopClientException("Config Error, No ServerRootType Specified");
        }
        for (ServerRootType serverRootType : serverRootTypes) {
            recordMainServer(serverRoot, serverRootType, force);
        }
    }

    private static boolean recordMainServer(String serverRoot, ServerRootType serverRootType, boolean force) {
        if (null == serverRoot) {
            throw new YopClientException("Config Error, No ServerRoot Found, type:" + serverRootType);
        }
        final String oldMain = MAIN_SERVER.putIfAbsent(serverRootType, serverRoot);
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
