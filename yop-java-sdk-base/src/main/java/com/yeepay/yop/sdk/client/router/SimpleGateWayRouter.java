package com.yeepay.yop.sdk.client.router;

import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.client.router.enums.ModeEnum;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.utils.CheckUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * title: 简单网关路由<br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-03-12 19:58
 */
public class SimpleGateWayRouter implements GateWayRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleGateWayRouter.class);

    private static final Map<ServerRootType, CopyOnWriteArrayList<URI>> ALL_SERVER = Maps.newConcurrentMap();
    private static final Map<URI, Set<ServerRootType>> ALL_SERVER_TYPES = Maps.newConcurrentMap();
    private static final Map<ServerRootType, URI> MAIN_SERVER = Maps.newConcurrentMap();
    private static final Map<ServerRootType, List<URI>> BACKUP_SERVERS = Maps.newConcurrentMap();
    private static final Map<ServerRootType, LinkedBlockingDeque<URI>> BLOCKED_SERVERS = Maps.newConcurrentMap();

    private static final String SYSTEM_SDK_MODE_KEY = "yop.sdk.mode";
    private static final String SANDBOX_APP_ID_PREFIX = "sandbox_";

    private static final List<ServerRootType> MANUAL_SERVER_ROOT_TYPES = Lists.newArrayList(ServerRootType.COMMON, ServerRootType.YOS);

    static {
        monitorServerRoot();
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
            for (URI uri : space.getPreferredEndPoint()) {
                addServerRoot(uri, ServerRootType.COMMON);
            }
        }
        addServerRoot(space.getServerRoot(), ServerRootType.COMMON);

        if (CollectionUtils.isNotEmpty(space.getPreferredYosEndPoint())) {
            for (URI uri : space.getPreferredYosEndPoint()) {
                addServerRoot(uri, ServerRootType.YOS);
            }
        }
        addServerRoot(space.getYosServerRoot(), ServerRootType.YOS);
    }

    private static boolean addServerRoot(URI serverRoot, ServerRootType serverRootType) {
        if (null != serverRoot) {
            ALL_SERVER_TYPES.computeIfAbsent(serverRoot, p -> Sets.newHashSet()).add(serverRootType);
            final CopyOnWriteArrayList<URI> serverRoots = ALL_SERVER.computeIfAbsent(serverRootType, p -> Lists.newCopyOnWriteArrayList());
            return serverRoots.addIfAbsent(serverRoot);
        }
        return false;
    }

    private static void addServerRoot(URI serverRoot, List<ServerRootType> serverRootTypes) {
        if (null != serverRoot && CollectionUtils.isNotEmpty(serverRootTypes)) {
            for (ServerRootType serverRootType : serverRootTypes) {
                addServerRoot(serverRoot, serverRootType);
            }
        }
    }

    private static void monitorServerRoot() {
        EventObserverRegistry.getInstance().addStateChangeObserver("BLOCKED_SERVERS_CHANGED",
                (prevState, newState, rule, snapshotValue) -> {
                    final URI serverRoot = URI.create(rule.getResource());
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("ServerRoot Block State Changed, value:{}, old:{}, new:{}", serverRoot, prevState, newState);
                    }
                    Set<ServerRootType> serverRootTypes = ALL_SERVER_TYPES.get(serverRoot);
                    if (CollectionUtils.isNotEmpty(serverRootTypes)) {
                        for (ServerRootType serverRootType : serverRootTypes) {
                            switch (newState) {
                                case OPEN:
                                    BLOCKED_SERVERS.computeIfAbsent(serverRootType, p -> new LinkedBlockingDeque<>()).add(serverRoot);
                                    break;
                                case CLOSED:
                                    final LinkedBlockingDeque<URI> blockedServers = BLOCKED_SERVERS.get(serverRootType);
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

    @Override
    public URI route(String appKey, Request<?> request, List<URI> excludeServerRoots) {
        if (isAppInSandbox(appKey)) {
            return space.getSandboxServerRoot();
        }

        final YopRequestConfig requestConfig = request.getOriginalRequestObject().getRequestConfig();
        final ServerRootType serverRootType = request.isYosRequest() ? ServerRootType.YOS : ServerRootType.COMMON;

        if (StringUtils.isNotBlank(requestConfig.getServerRoot())) {
            URI serverRoot = CheckUtils.checkServerRoot(requestConfig.getServerRoot());
            if (isExcludeServerRoots(serverRoot, excludeServerRoots)) {
                throw new YopClientException("RequestConfig Error, serverRoot excluded:" + serverRoot);
            }
            addServerRoot(serverRoot, MANUAL_SERVER_ROOT_TYPES);
            recordMainServer(serverRoot, MANUAL_SERVER_ROOT_TYPES, true);
            return serverRoot;
        } else {
            // 独立网关，依然走openapi，serviceName是apiGroup的变形，需要还原
            String apiGroup = request.getServiceName().toLowerCase().replace(CharacterConstants.UNDER_LINE, CharacterConstants.DASH_LINE);
            if (independentApiGroups.contains(apiGroup)) {
                final URI independentServerRoot = independentServerRoot(apiGroup, request);
                if (isExcludeServerRoots(independentServerRoot, excludeServerRoots)) {
                    throw new YopClientException("Config Error, ServerRoot excluded:" + independentServerRoot);
                }
                return independentServerRoot;
            }

            // 主域名
            URI mainServer = MAIN_SERVER.get(serverRootType);
            if (null != mainServer && !isExcludeServerRoots(mainServer, excludeServerRoots)) {
                return mainServer;
            }

            final CopyOnWriteArrayList<URI> serverRoots = ALL_SERVER.get(serverRootType);
            if (CollectionUtils.isEmpty(serverRoots)) {
                throw new YopClientException("Config Error, No ServerRoot Found, type:" + serverRootType);
            }

            // 随机选主
            if (null == mainServer) {
                final List<URI> randomList = RouteUtils.randomList(serverRoots);
                mainServer = randomList.remove(0);
                if (recordMainServer(mainServer, serverRootType)) {
                    BACKUP_SERVERS.put(serverRootType, randomList);
                }
                return MAIN_SERVER.get(serverRootType);
            }

            // 主域名故障，临时启用备选域名
            final List<URI> backupServers = BACKUP_SERVERS.get(serverRootType);
            if (CollectionUtils.isNotEmpty(backupServers)) {
                for (URI backup : backupServers) {
                    if (!isExcludeServerRoots(backup, excludeServerRoots)) {
                        return backup;
                    }
                }
            }

            // 备用域名故障，选用最早故障的域名
            final LinkedBlockingDeque<URI> failedServers = BLOCKED_SERVERS.get(serverRootType);
            URI oldestFailServer = null;
            if (null != failedServers && !failedServers.isEmpty()) {
                oldestFailServer = failedServers.peek();
            }

            // 主域名兜底
            return null != oldestFailServer ? oldestFailServer : mainServer;
        }
    }

    private boolean isExcludeServerRoots(URI serverRoot, List<URI> excludeServerRoots) {
        return null != excludeServerRoots && null != serverRoot && excludeServerRoots.contains(serverRoot);
    }

    private boolean recordMainServer(URI serverRoot, ServerRootType serverRootType) {
        return recordMainServer(serverRoot, serverRootType, false);
    }

    private void recordMainServer(URI serverRoot, List<ServerRootType> serverRootTypes, boolean force) {
        if (CollectionUtils.isEmpty(serverRootTypes)) {
            throw new YopClientException("Config Error, No ServerRootType Specified");
        }
        for (ServerRootType serverRootType : serverRootTypes) {
            recordMainServer(serverRoot, serverRootType, force);
        }
    }

    private boolean recordMainServer(URI serverRoot, ServerRootType serverRootType, boolean force) {
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

    private URI independentServerRoot(String apiGroup, Request<?> request) {
        try {
            URI serverRoot = request.isYosRequest() ? space.getYosServerRoot() : space.getServerRoot();
            return new URI(serverRoot.getScheme(), serverRoot.getUserInfo(),
                    getIndependentApiGroupHost(apiGroup, serverRoot.getHost(), request.isYosRequest()),
                    serverRoot.getPort(), serverRoot.getPath(), serverRoot.getQuery(), serverRoot.getFragment());
        } catch (Exception ex) {
            throw new YopClientException("Route Request Failure, ex:", ex);
        }
    }

    private boolean isAppInSandbox(String appKey) {
        if (systemMode == null) {
            return StringUtils.startsWith(appKey, SANDBOX_APP_ID_PREFIX);
        }
        return systemMode == ModeEnum.sandbox;
    }

    private String getIndependentApiGroupHost(String apiGroup, String originHost, boolean isYosRequest) {
        //目前只有普通api的请求才需要路由到独立网关
        if (isYosRequest) {
            return originHost;
        }
        int index = StringUtils.indexOf(originHost, CharacterConstants.DOT);
        return StringUtils.substring(originHost, 0, index) + CharacterConstants.DASH_LINE + apiGroup + StringUtils.substring(originHost, index);
    }

    private enum ServerRootType {
        COMMON,
        YOS,
        SANDBOX
    }
}
