package com.yeepay.yop.sdk.client.router;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.client.ClientReporter;
import com.yeepay.yop.sdk.client.metric.report.host.YopHostStatusChangePayload;
import com.yeepay.yop.sdk.client.metric.report.host.YopHostStatusChangeReport;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.router.sentinel.YopDegradeRuleHelper;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.yeepay.yop.sdk.utils.CheckUtils;
import com.yeepay.yop.sdk.utils.EnvUtils;
import com.yeepay.yop.sdk.utils.RandomUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;


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

    private static final Map<ServerRootSpace, ServerRootRouting> SERVER_ROOT_ROUTING = Maps.newConcurrentMap();
    private static final Map<String, Set<ServerRootInfo>> COMMON_SERVER_ROOT_INFOS = Maps.newConcurrentMap();
    private static final BlockResourcePool BLOCK_SERVER_POOL = new BlockResourcePool();
    private static final ThreadPoolExecutor BLOCKED_SWEEPER = new ThreadPoolExecutor(2, 20,
            3, TimeUnit.MINUTES, Queues.newLinkedBlockingQueue(1000),
            new ThreadFactoryBuilder().setNameFormat("yop-blocked-resource-sweeper-%d").setDaemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy());

    static {
        monitorServerRoot();
    }

    private class ServerRootRouting {

        private String serverGroup;
        private Map<ServerRootType, URI> mainServers;
        private Map<ServerRootType, List<URI>> backupServers;
        private Map<ServerRootType, List<URI>> allServers;

        public ServerRootRouting(String serverGroup, Map<ServerRootType, URI> mainServers, Map<ServerRootType, List<URI>> backupServers) {
            this.serverGroup = serverGroup;
            this.mainServers = mainServers;
            this.backupServers = backupServers;
            this.allServers = Maps.newHashMap();
            if (MapUtils.isNotEmpty(mainServers)) {
                mainServers.forEach((k, v) -> this.allServers.computeIfAbsent(k, p -> Lists.newArrayList()).add(v));
            }
            if (MapUtils.isNotEmpty(backupServers)) {
                backupServers.forEach((k, v) -> this.allServers.computeIfAbsent(k, p -> Lists.newArrayList()).addAll(v));
            }
        }

        public String getServerGroup() {
            return serverGroup;
        }

        public Map<ServerRootType, URI> getMainServers() {
            return mainServers;
        }

        public Map<ServerRootType, List<URI>> getBackupServers() {
            return backupServers;
        }

        public Map<ServerRootType, List<URI>> getAllServers() {
            return allServers;
        }
    }


    private final ServerRootSpace space;

    private final Set<String> independentApiGroups;

    private final ServerRootRouting serverRootRouting;

    public SimpleGateWayRouter(ServerRootSpace space) {
        this.space = space;
        this.independentApiGroups = Collections.unmodifiableSet(Sets.newHashSet("bank-encryption"));

        this.serverRootRouting = SERVER_ROOT_ROUTING.computeIfAbsent(space, p -> {
            collectServerRootInfos(space);
            final Map<ServerRootType, URI> mainServers = Maps.newConcurrentMap();
            final Map<ServerRootType, List<URI>> backupServers = Maps.newConcurrentMap();

            // 随机选主:common
            final List<URI> randomCommonList = RandomUtils.randomList(space.getPreferredEndPoint());
            if (recordMainServer(randomCommonList.remove(0), ServerRootType.COMMON, mainServers)) {
                backupServers.put(ServerRootType.COMMON, randomCommonList);
            }
            // yos
            final List<URI> randomYosList = RandomUtils.randomList(CollectionUtils.isEmpty(space.getPreferredYosEndPoint())
                    ? Lists.newArrayList(space.getYosServerRoot()) : space.getPreferredYosEndPoint());
            if (recordMainServer(randomYosList.remove(0), ServerRootType.YOS, mainServers)) {
                backupServers.put(ServerRootType.YOS, randomYosList);
            }
            // sandbox 兼容老沙箱
            final List<URI> randomSandboxList = RandomUtils.randomList(Lists.newArrayList(space.getSandboxServerRoot()));
            if (recordMainServer(randomSandboxList.remove(0), ServerRootType.SANDBOX, mainServers)) {
                backupServers.put(ServerRootType.SANDBOX, randomYosList);
            }
            return new ServerRootRouting(space.getServerGroup(), mainServers, backupServers);
        });
    }

    private boolean recordMainServer(URI serverRoot, ServerRootType serverRootType, Map<ServerRootType, URI> mainServers) {
        return recordMainServer(serverRoot, serverRootType, mainServers, false);
    }

    private boolean recordMainServer(URI serverRoot, ServerRootType serverRootType, Map<ServerRootType, URI> mainServers, boolean force) {
        if (null == serverRoot) {
            throw new YopClientException("Config Error, No ServerRoot Found, type:" + serverRootType);
        }
        final URI oldMain = mainServers.putIfAbsent(serverRootType, serverRoot);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Main ServerRoot Set, value:{}, type:{}", serverRoot, serverRootType);
        }
        if (null != oldMain) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Main ServerRoot Already Set, value:{}", oldMain);
            }
            if (force) {
                mainServers.put(serverRootType, serverRoot);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Main ServerRoot Switched, old:{}, new:{}", oldMain, serverRoot);
                }
                return true;
            }
            return false;
        }
        return true;
    }

    private void collectServerRootInfos(ServerRootSpace space) {
        if (CollectionUtils.isNotEmpty(space.getPreferredEndPoint())) {
            for (URI uri : space.getPreferredEndPoint()) {
                collectServerRootInfo(space.getProvider(), space.getEnv(), new UriResource(space.getServerGroup(), uri), ServerRootType.COMMON);
            }
        }

        if (CollectionUtils.isNotEmpty(space.getPreferredYosEndPoint())) {
            for (URI uri : space.getPreferredYosEndPoint()) {
                collectServerRootInfo(space.getProvider(), space.getEnv(), new UriResource(space.getServerGroup(), uri), ServerRootType.YOS);
            }
        }
        collectServerRootInfo(space.getProvider(), space.getEnv(), new UriResource(space.getServerGroup(), space.getYosServerRoot()), ServerRootType.YOS);
    }

    private void collectServerRootInfo(String provider, String env, UriResource uriResource, ServerRootType serverRootType) {
        if (null != uriResource && null != uriResource.getResource()) {
            COMMON_SERVER_ROOT_INFOS.computeIfAbsent(uriResource.computeResourceKey(),
                    p -> Sets.newHashSet()).add(new ServerRootInfo(provider, env, space.getServerGroup(), serverRootType));
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
                        final String commonServerRootKey = new UriResource(uriResource.getResourceGroup(), serverRoot)
                                .computeResourceKey();

                        Set<ServerRootInfo> serverRootInfos = COMMON_SERVER_ROOT_INFOS.get(commonServerRootKey);
                        ServerRootInfo choosedServerRootInfo = ServerRootInfo.DEFAULT_INFO;
                        Set<String> serverTypes = Collections.emptySet();
                        if (CollectionUtils.isNotEmpty(serverRootInfos)) {
                            serverTypes = Sets.newHashSet();
                            for (ServerRootInfo serverRootInfo : serverRootInfos) {
                                serverTypes.add(serverRootInfo.getServerRootType().name());
                            }
                            choosedServerRootInfo = serverRootInfos.iterator().next();
                        }
                        BLOCK_SERVER_POOL.onServerStatusChange(uriResource, prevState, newState, rule, serverTypes);
                        // 异步上报
                        final YopHostStatusChangeReport report = new YopHostStatusChangeReport(
                                new YopHostStatusChangePayload(serverRoot.toString(), prevState.name(), newState.name(), rule.toString()));
                        report.setProvider(choosedServerRootInfo.getProvider());
                        report.setEnv(choosedServerRootInfo.getEnv());
                        ClientReporter.asyncReportToQueue(report);
                    } catch (Exception e) {
                        LOGGER.warn("UnexpectedError, MonitorServerRoot ex:", e);
                    }
                });
    }

    @Override
    public UriResource route(String appKey, Request<?> request, List<URI> excludeServerRoots) {
        // 兼容旧版沙箱调用
        if (!EnvUtils.isSandBoxEnv(space.getEnv()) && (EnvUtils.isSandboxApp(appKey) || EnvUtils.isSandBoxMode())) {
            return new UriResource(space.getServerGroup(), space.getSandboxServerRoot());
        }

        final YopRequestConfig requestConfig = request.getOriginalRequestObject().getRequestConfig();
        final ServerRootType serverRootType = request.isYosRequest() ? ServerRootType.YOS : ServerRootType.COMMON;

        if (StringUtils.isNotBlank(requestConfig.getServerRoot())) {
            URI serverRoot = CheckUtils.checkServerRoot(requestConfig.getServerRoot());
            if (isExcludeServerRoots(serverRoot, excludeServerRoots)) {
                throw new YopClientException("RequestConfig Error, serverRoot excluded:" + serverRoot);
            }
            final UriResource manualServer = new UriResource(space.getServerGroup(), serverRoot);
            collectServerRootInfo(space.getProvider(), space.getEnv(), manualServer, serverRootType);
            return manualServer;
        } else {
            // 独立网关，依然走openapi，serviceName是apiGroup的变形，需要还原
            String apiGroup = request.getServiceName().toLowerCase().replace(CharacterConstants.UNDER_LINE, CharacterConstants.DASH_LINE);
            if (independentApiGroups.contains(apiGroup)) {
                final URI independentServerRoot = independentServerRoot(apiGroup, request);
                if (isExcludeServerRoots(independentServerRoot, excludeServerRoots)) {
                    throw new YopClientException("Config Error, ServerRoot excluded:" + independentServerRoot);
                }
                return new UriResource(space.getServerGroup(), independentServerRoot);
            }

            // 主域名准备
            URI mainServer = this.serverRootRouting.getMainServers().get(serverRootType);
            if (null == mainServer) {
                throw new YopClientException("Config Error, Main ServerRoot NotFound" + serverRootType);
            }

            // 主域名正常
            if (!isExcludeServerRoots(mainServer, excludeServerRoots)) {
                return new UriResource(space.getServerGroup(), mainServer);
            }

            // 主域名故障，临时启用备选域名
            final List<URI> backupServers = this.serverRootRouting.getBackupServers().get(serverRootType);
            if (CollectionUtils.isNotEmpty(backupServers)) {
                for (URI backup : backupServers) {
                    if (!isExcludeServerRoots(backup, excludeServerRoots)) {
                        return new UriResource(space.getServerGroup(), backup);
                    }
                }
            }

            // 备用域名故障，选用最早故障的域名
            return BLOCK_SERVER_POOL.select(space.getServerGroup(), serverRootType.name(), mainServer,
                    this.serverRootRouting.getAllServers().get(serverRootType));
        }
    }

    private boolean isExcludeServerRoots(URI serverRoot, List<URI> excludeServerRoots) {
        return null != excludeServerRoots && null != serverRoot && excludeServerRoots.contains(serverRoot);
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
        @Deprecated
        SANDBOX
    }

    private static class ServerRootInfo {
        public static ServerRootInfo DEFAULT_INFO = new ServerRootInfo(YopConstants.YOP_DEFAULT_PROVIDER,
                YopConstants.YOP_DEFAULT_ENV, CharacterConstants.EMPTY, ServerRootType.COMMON);
        private String provider;
        private String env;
        private String serverGroup;
        private ServerRootType serverRootType;

        public ServerRootInfo(String provider, String env, String serverGroup, ServerRootType serverRootType) {
            this.provider = provider;
            this.env = env;
            this.serverGroup = serverGroup;
            this.serverRootType = serverRootType;
        }

        public String getProvider() {
            return provider;
        }

        public String getEnv() {
            return env;
        }

        public String getServerGroup() {
            return serverGroup;
        }

        public ServerRootType getServerRootType() {
            return serverRootType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(provider, env, serverGroup, serverRootType);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ServerRootInfo) {
                ServerRootInfo that = (ServerRootInfo) obj;
                return Objects.equals(this.provider, that.provider) &&
                        Objects.equals(this.env, that.env) &&
                        Objects.equals(this.serverGroup, that.serverGroup) &&
                        Objects.equals(this.serverRootType, that.serverRootType);
            }
            return false;
        }
    }

    public static class BlockResourcePool {
        private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        private static final Map<String, List<URI>> serverBlockList = Maps.newConcurrentMap();
        private static final Map<String, AtomicLong> serverBLockSequence = Maps.newConcurrentMap();
        public UriResource select(String serverGroup, String serverType, URI mainServer, List<URI> allServers) {
            rwl.readLock().lock();
            try {
                URI oldestFailServer = null;
                final String serverBlockKey = serverBlockKey(serverGroup, serverType);
                final List<URI> failedServers = serverBlockList.get(serverBlockKey);
                if (null != failedServers && !failedServers.isEmpty()) {
                    for (URI failedServer : failedServers) {
                        if (CollectionUtils.isNotEmpty(allServers) && allServers.contains(failedServer)) {
                            oldestFailServer = failedServer;
                            break;
                        }
                    }
                }
                // 熔断列表为空(说明其他线程已半开成功)，选主域名即可
                if (null == oldestFailServer) {
                    oldestFailServer = mainServer;
                }
                return initServer(serverGroup, serverType, oldestFailServer);
            } finally {
                rwl.readLock().unlock();
            }
        }

        private String serverBlockKey(String serverGroup, String serverType) {
            return serverGroup + CharacterConstants.COMMA + serverType;
        }

        private UriResource initServer(String serverGroup, String serverType, URI oldestFailServer) {

            final String blockSequenceKey = getBlockSequenceKey(serverGroup, serverType, oldestFailServer);
            final AtomicLong blockSequence = serverBLockSequence.computeIfAbsent(blockSequenceKey,
                    p -> new AtomicLong(0));

            String resourcePrefix = getBlockResourcePrefix(serverType, blockSequence.get());
            return new UriResource(UriResource.ResourceType.BLOCKED, serverGroup,
                    resourcePrefix, oldestFailServer);

        }

        private String parseBlockServerType(String blockResourcePrefix) {
            return blockResourcePrefix.split(CharacterConstants.COMMA)[0];
        }

        private Long parseBLockSequence(String blockResourcePrefix) {
            return Long.valueOf(blockResourcePrefix.split(CharacterConstants.COMMA)[1]);
        }

        private String getBlockResourcePrefix(String serverType, Long blockSequence) {
            return serverType + CharacterConstants.COMMA + blockSequence;
        }

        private String getBlockSequenceKey(String serverGroup, String serverType, URI server) {
            return serverGroup + CharacterConstants.COMMA + serverType + CharacterConstants.COMMA + server.toString();
        }

        public void onServerStatusChange(UriResource uriResource, CircuitBreaker.State prevState,
                                         CircuitBreaker.State newState, DegradeRule rule,
                                         Set<String> serverRootTypes) {
            updateBlockedStatus(uriResource, serverRootTypes, !CircuitBreaker.State.OPEN.equals(newState));
            if (newState.equals(CircuitBreaker.State.OPEN) && UriResource.ResourceType.BLOCKED.equals(uriResource.getResourceType())) {
                asyncDiscardOldServers(uriResource);
            }
        }

        // 更新熔断列表排序
        private void updateBlockedStatus(UriResource uriResource, Set<String> serverRootTypes,
                                         boolean successInvoked) {
            rwl.writeLock().lock();
            try {
                final String resourceGroup = uriResource.getResourceGroup();
                URI serverRoot = uriResource.getResource();
                for (String serverRootType : serverRootTypes) {
                    final List<URI> blockedServers = serverBlockList.computeIfAbsent(serverBlockKey(resourceGroup, serverRootType),
                            p -> new ArrayList<>());
                    blockedServers.removeIf(serverRoot::equals);
                    if (successInvoked) {
                        blockedServers.add(0, serverRoot);
                    } else {
                        blockedServers.add(serverRoot);
                    }
                }
                if (UriResource.ResourceType.BLOCKED.equals(uriResource.getResourceType()) && !successInvoked) {
                    final String serverRootType = parseBlockServerType(uriResource.getResourcePrefix());
                    serverBLockSequence.computeIfAbsent(getBlockSequenceKey(uriResource.getResourceGroup(),
                                    serverRootType, uriResource.getResource()),
                            p -> new AtomicLong(0)).getAndAdd(1);
                }

            } finally {
                rwl.writeLock().unlock();
            }
        }

        // 异步清理过期资源
        private void asyncDiscardOldServers(UriResource uriResource) {
            BLOCKED_SWEEPER.submit(() -> {
                try {
                    final String resource = uriResource.computeResourceKey();
                    // 清理资源配置
                    YopDegradeRuleHelper.removeDegradeRule(resource);
                } catch (Exception e) {
                    LOGGER.warn("blocked sweeper failed, ex:", e);
                }
            });
        }
    }

}
