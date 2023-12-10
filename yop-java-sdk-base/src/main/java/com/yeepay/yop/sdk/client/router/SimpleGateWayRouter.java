package com.yeepay.yop.sdk.client.router;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.client.ClientReporter;
import com.yeepay.yop.sdk.client.metric.report.host.YopHostStatusChangePayload;
import com.yeepay.yop.sdk.client.metric.report.host.YopHostStatusChangeReport;
import com.yeepay.yop.sdk.client.router.enums.ModeEnum;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.utils.CheckUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

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
    private static final BlockServerPool BLOCKED_SERVER_POOL = new BlockServerPool();

    // sentinel 限制，最多6000资源，超出后不再熔断，预留1000
    private static final int BLOCKED_MAX_SIZE = Constants.MAX_SLOT_CHAIN_SIZE - 1000;
    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
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
        // sentinel监控
        EventObserverRegistry.getInstance().addStateChangeObserver("BLOCKED_SERVERS_CHANGED",
                (prevState, newState, rule, snapshotValue) -> {
                    try {
                        final UriResource uriResource = UriResource.parseResourceKey(rule.getResource());
                        final URI serverRoot = uriResource.getResource();
                        LOGGER.info("ServerRoot Block State Changed, serverRoot:{}, old:{}, new:{}, rule:{}",
                                serverRoot, prevState, newState, rule);
                        Set<ServerRootType> serverRootTypes = ALL_SERVER_TYPES.get(serverRoot);
                        if (CollectionUtils.isNotEmpty(serverRootTypes)) {
                            rwl.writeLock().lock();
                            try {
                                BLOCKED_SERVER_POOL.onServerStatusChange(uriResource, prevState, newState, rule, serverRootTypes);
                            } finally {
                                rwl.writeLock().unlock();
                            }
                        }
                        ClientReporter.asyncReportToQueue(new YopHostStatusChangeReport(
                                new YopHostStatusChangePayload(serverRoot.toString(), prevState.name(), newState.name(), rule.toString())));
                    } catch (Exception e) {
                        LOGGER.warn("UnexpectedError, MonitorServerRoot ex:", e);
                    }
                });
    }

    private static List<String> getAllServerRoots(ServerRootType serverRootType) {
        final CopyOnWriteArrayList<URI> serverRoots = ALL_SERVER.get(serverRootType);
        if (CollectionUtils.isEmpty(serverRoots)) {
            return Collections.emptyList();
        }
        return serverRoots.stream().map(URI::toString).collect(Collectors.toList());
    }

    @Override
    public UriResource route(String appKey, Request<?> request, List<URI> excludeServerRoots) {
        if (isAppInSandbox(appKey)) {
            return new UriResource(space.getSandboxServerRoot());
        }

        final YopRequestConfig requestConfig = request.getOriginalRequestObject().getRequestConfig();
        final ServerRootType serverRootType = request.isYosRequest() ? ServerRootType.YOS : ServerRootType.COMMON;

        if (StringUtils.isNotBlank(requestConfig.getServerRoot())) {
            URI serverRoot = CheckUtils.checkServerRoot(requestConfig.getServerRoot());
            if (isExcludeServerRoots(serverRoot, excludeServerRoots)) {
                throw new YopClientException("RequestConfig Error, serverRoot excluded:" + serverRoot);
            }
            addServerRoot(serverRoot, MANUAL_SERVER_ROOT_TYPES);
            return new UriResource(serverRoot);
        } else {
            // 独立网关，依然走openapi，serviceName是apiGroup的变形，需要还原
            String apiGroup = request.getServiceName().toLowerCase().replace(CharacterConstants.UNDER_LINE, CharacterConstants.DASH_LINE);
            if (independentApiGroups.contains(apiGroup)) {
                final URI independentServerRoot = independentServerRoot(apiGroup, request);
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
            return BLOCKED_SERVER_POOL.select(serverRootType, mainServer);
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

    private static class BlockServer implements Serializable {

        private static final long serialVersionUID = -1L;
        private UriResource uri;

        private boolean available;

        private long nextAvailableTime;

        public BlockServer(UriResource uri, boolean available) {
            this.uri = uri;
            this.available = available;
        }

        public BlockServer(UriResource uri, boolean available, long nextAvailableTime) {
            this.uri = uri;
            this.available = available;
            this.nextAvailableTime = nextAvailableTime;
        }

        public boolean isAvailable() {
            return available;
        }

        public BlockServer setAvailable(boolean available) {
            this.available = available;
            return this;
        }

        public long getNextAvailableTime() {
            return nextAvailableTime;
        }

        public BlockServer setNextAvailableTime(long nextAvailableTime) {
            this.nextAvailableTime = nextAvailableTime;
            return this;
        }

        @Override
        public int hashCode() {
            return uri.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BlockServer) {
                BlockServer blockServer = (BlockServer) obj;
                return uri.equals(blockServer.uri);
            }
            return false;
        }
    }

    private static final Comparator COMPARATOR = new Comparator();

    private static class Comparator implements java.util.Comparator<BlockServer> {

        @Override
        public int compare(BlockServer o1, BlockServer o2) {
            if (o1.available) {
                return 1;
            }
            if (o2.available) {
                return -1;
            }
            if (o1.getNextAvailableTime() < o2.getNextAvailableTime()) {
                return 1;
            }
            return 0;
        }
    }

    private static class BlockServerPool {
        private Map<URI, Map<String, BlockServer>> serverMap = Maps.newConcurrentMap();
        private static final Map<ServerRootType, List<URI>> serverBlockList = Maps.newConcurrentMap();

        private static final String RETAIN_RESOURCE_ID = "0000";

        private int size;

        public void blockServer(UriResource uri, long nextAvailableTime) {
            final URI resource = uri.getResource();
            final Map<String, BlockServer> servers = serverMap.computeIfAbsent(resource, p-> Maps.newHashMap());
            final boolean available = nextAvailableTime > 0 && nextAvailableTime < System.currentTimeMillis();
            final String resourceKey = uri.computeResourceKey();
            BlockServer blockServer = servers.get(resourceKey);
            if (null == blockServer) {
                servers.put(resourceKey, new BlockServer(uri, available, nextAvailableTime));
                size++;
            } else {
                blockServer.setAvailable(available).setNextAvailableTime(nextAvailableTime);
            }
        }

        public UriResource select(ServerRootType serverType, URI mainServer) {
            rwl.readLock().lock();
            try {
                URI oldestFailServer = null;
                final List<URI> failedServers = serverBlockList.get(serverType);
                if (null != failedServers && !failedServers.isEmpty()) {
                    oldestFailServer = failedServers.get(0);
                }
                // 熔断列表为空(说明其他线程已半开成功)，选主域名即可
                if (null == oldestFailServer) {
                    oldestFailServer = mainServer;
                }

                // 查找可用资源
                BlockServer availableServer = availableServer(oldestFailServer);
                if (null != availableServer) {
                    return availableServer.uri;
                }

                // 无可用资源，则重新初始化一个
                rwl.readLock().unlock();
                rwl.writeLock().lock();
                try {
                    // 二次查找
                    if (null != (availableServer = availableServer(oldestFailServer))) {
                        return availableServer.uri;
                    }
                    return initServer(oldestFailServer);
                } finally {
                    rwl.readLock().lock();// 参考官方示例
                    rwl.writeLock().unlock();
                }
            } finally {
                rwl.readLock().unlock();
            }
        }

        private BlockServer availableServer(URI uri) {
            final Map<String, BlockServer> blockServers = serverMap.get(uri);
            if (null != blockServers && !blockServers.isEmpty()) {
                for (BlockServer blockServer : blockServers.values()) {
                    if (blockServer.available) {
                        return blockServer;
                    }
                }
            }
            return null;
        }

        private UriResource initServer(URI oldestFailServer) {
            if (size >= BLOCKED_MAX_SIZE) {
                LOGGER.error("Blocked Server OverFlow");
                return new UriResource(UriResource.ResourceType.BLOCKED,
                        RETAIN_RESOURCE_ID, oldestFailServer);
            }
            String resourceId = String.valueOf(size++);
            final UriResource uriResource = new UriResource(UriResource.ResourceType.BLOCKED,
                    resourceId, oldestFailServer);
            final Map<String, BlockServer> blockServers = serverMap.computeIfAbsent(oldestFailServer, p -> Maps.newHashMap());
            final BlockServer blockServer = new BlockServer(uriResource, true);
            blockServers.put(uriResource.computeResourceKey(), blockServer);
            return uriResource;

        }

        public void resumeServer(UriResource uri) {
            final URI resource = uri.getResource();
            final Map<String, BlockServer> servers = serverMap.computeIfAbsent(resource, p-> Maps.newHashMap());
            final BlockServer blockServer = servers.get(uri.computeResourceKey());
            if (null != blockServer) {
                blockServer.setAvailable(true).setNextAvailableTime(0);
            }
        }

        public void onServerStatusChange(UriResource uriResource, CircuitBreaker.State prevState,
                                         CircuitBreaker.State newState, DegradeRule rule,
                                         Set<ServerRootType> serverRootTypes) {
            updateServerOrder(uriResource, serverRootTypes, newState);
            updateServerStatus(uriResource, rule, newState);
        }

        // 更新资源池状态
        private void updateServerStatus(UriResource uriResource, DegradeRule rule,
                                        CircuitBreaker.State newState) {
            final long now = System.currentTimeMillis();
            switch (newState) {
                case OPEN:
                    blockServer(uriResource, rule.getTimeWindow() * 1000L + now);
                    break;
                case CLOSED:
                case HALF_OPEN:
                    resumeServer(uriResource);
                    break;
                default:
            }
        }

        // 更新熔断列表排序
        private void updateServerOrder(UriResource uriResource, Set<ServerRootType> serverRootTypes,
                                       CircuitBreaker.State newState) {
            final URI serverRoot = uriResource.getResource();
            for (ServerRootType serverRootType : serverRootTypes) {
                final List<URI> blockedServers = serverBlockList.computeIfAbsent(serverRootType,
                        p -> new ArrayList<>());
                blockedServers.removeIf(serverRoot::equals);
                switch (newState) {
                    case OPEN:
                        blockedServers.add(serverRoot);
                    case CLOSED:
                    case HALF_OPEN:
                        blockedServers.add(0, serverRoot);
                        break;
                    default:
                }
            }
        }
    }
}
