package com.yeepay.yop.sdk.client.router;

import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.client.ClientReporter;
import com.yeepay.yop.sdk.client.metric.report.host.YopHostStatusChangePayload;
import com.yeepay.yop.sdk.client.metric.report.host.YopHostStatusChangeReport;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.sentinel.YopSph;
import com.yeepay.yop.sdk.utils.CheckUtils;
import com.yeepay.yop.sdk.utils.EnvUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;


/**
 * title: 网关路由-基类<br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-03-12 19:58
 */
abstract public class AbstractGateWayRouter implements GateWayRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGateWayRouter.class);

    /**
     * 记录client的路由信息：主、备
     */
    protected static final Map<ServerRootSpace, ServerRootRouting> SERVER_ROOT_ROUTING = Maps.newConcurrentMap();
    /**
     * 记录所有的serverRoot信息: provider、env、type
     */
    protected static final Map<URI, Set<ServerRootInfo>> ALL_SERVER_INFOS = Maps.newConcurrentMap();

    /**
     * 记录被sentinel block的serverRoot信息
     */
    protected static final YopSph.BlockResourcePool BLOCK_SERVER_POOL = new YopSph.BlockResourcePool();

    static {
        monitorServerRoot();
    }

    /**
     * 某个client路由信息
     */
    protected static class ServerRootRouting {

        private Map<ServerRootType, URI> mainServers;
        private Map<ServerRootType, List<URI>> backupServers;
        private Map<ServerRootType, List<URI>> allServers;

        public ServerRootRouting(Map<ServerRootType, URI> mainServers, Map<ServerRootType, List<URI>> backupServers) {
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


    protected final ServerRootSpace space;

    protected final Set<String> independentApiGroups;

    protected final ServerRootRouting serverRootRouting;

    public AbstractGateWayRouter(ServerRootSpace space) {
        this.space = space;
        this.independentApiGroups = Collections.unmodifiableSet(Sets.newHashSet("bank-encryption"));
        this.serverRootRouting = SERVER_ROOT_ROUTING.computeIfAbsent(space, p -> {
            collectServerRootTypes(space);
            return initServerRootRouting(space);
        });
    }

    protected ServerRootRouting initServerRootRouting(ServerRootSpace space) {
        final Map<ServerRootType, URI> mainServers = Maps.newConcurrentMap();
        final Map<ServerRootType, List<URI>> backupServers = Maps.newConcurrentMap();

        // 随机选主:common
        final List<URI> randomCommonList = RouteUtils.randomList(space.getPreferredEndPoint());
        if (recordMainServer(randomCommonList.remove(0), ServerRootType.COMMON, mainServers)) {
            backupServers.put(ServerRootType.COMMON, randomCommonList);
        }
        // yos
        final List<URI> randomYosList = RouteUtils.randomList(CollectionUtils.isEmpty(space.getPreferredYosEndPoint())
                ? Lists.newArrayList(space.getYosServerRoot()) : space.getPreferredYosEndPoint());
        if (recordMainServer(randomYosList.remove(0), ServerRootType.YOS, mainServers)) {
            backupServers.put(ServerRootType.YOS, randomYosList);
        }
        // sandbox 兼容老沙箱
        final List<URI> randomSandboxList = RouteUtils.randomList(Lists.newArrayList(space.getSandboxServerRoot()));
        if (recordMainServer(randomSandboxList.remove(0), ServerRootType.SANDBOX, mainServers)) {
            backupServers.put(ServerRootType.SANDBOX, randomSandboxList);
        }
        return new ServerRootRouting(mainServers, backupServers);
    };

    protected boolean recordMainServer(URI serverRoot, ServerRootType serverRootType, Map<ServerRootType, URI> mainServers) {
        return recordMainServer(serverRoot, serverRootType, mainServers, false);
    }

    protected boolean recordMainServer(URI serverRoot, ServerRootType serverRootType, Map<ServerRootType, URI> mainServers, boolean force) {
        if (null == serverRoot) {
            throw new YopClientException("Config Error, No ServerRoot Found, type:" + serverRootType);
        }
        final URI oldMain = mainServers.putIfAbsent(serverRootType, serverRoot);
        LOGGER.info("Main ServerRoot Set, value:{}, type:{}", serverRoot, serverRootType);
        if (null != oldMain) {
            if (force) {
                mainServers.put(serverRootType, serverRoot);
                LOGGER.info("Main ServerRoot Switched, old:{}, new:{}", oldMain, serverRoot);
                return true;
            }
            return false;
        }
        return true;
    }

    protected void collectServerRootTypes(ServerRootSpace space) {
        if (CollectionUtils.isNotEmpty(space.getPreferredEndPoint())) {
            for (URI uri : space.getPreferredEndPoint()) {
                collectServerRootType(space.getProvider(), space.getEnv(), uri, ServerRootType.COMMON);
            }
        }

        if (CollectionUtils.isNotEmpty(space.getPreferredYosEndPoint())) {
            for (URI uri : space.getPreferredYosEndPoint()) {
                collectServerRootType(space.getProvider(), space.getEnv(), uri, ServerRootType.YOS);
            }
        }
        collectServerRootType(space.getProvider(), space.getEnv(), space.getYosServerRoot(), ServerRootType.YOS);
    }

    protected void collectServerRootType(String provider, String env, URI serverRoot, ServerRootType serverRootType) {
        if (null != serverRoot) {
            ALL_SERVER_INFOS.computeIfAbsent(serverRoot,
                    p -> Sets.newHashSet()).add(new ServerRootInfo(provider, env, serverRootType));
        }
    }

    /**
     * 监控sentinel的serverRoot状态变化
     */
    protected static void monitorServerRoot() {
        // sentinel监控
        EventObserverRegistry.getInstance().addStateChangeObserver("BLOCKED_SERVERS_CHANGED",
                (prevState, newState, rule, snapshotValue) -> {
                    try {
                        final UriResource uriResource = UriResource.parseResourceKey(rule.getResource());
                        final URI serverRoot = uriResource.getResource();
                        LOGGER.info("ServerRoot Block State Changed, serverRoot:{}, old:{}, new:{}, rule:{}",
                                serverRoot, prevState, newState, rule);
                        Set<ServerRootInfo> serverRootInfos = ALL_SERVER_INFOS.get(serverRoot);
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
            return new UriResource(space.getSandboxServerRoot());
        }

        final YopRequestConfig requestConfig = request.getOriginalRequestObject().getRequestConfig();
        final ServerRootType serverRootType = request.isYosRequest() ? ServerRootType.YOS : ServerRootType.COMMON;

        if (StringUtils.isNotBlank(requestConfig.getServerRoot())) {
            URI serverRoot = CheckUtils.checkServerRoot(requestConfig.getServerRoot());
            if (isExcludeServerRoots(serverRoot, excludeServerRoots)) {
                throw new YopClientException("RequestConfig Error, serverRoot excluded:" + serverRoot);
            }
            collectServerRootType(space.getProvider(), space.getEnv(), serverRoot, serverRootType);
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

            // 主域名准备
            URI mainServer = this.serverRootRouting.getMainServers().get(serverRootType);
            if (null == mainServer) {
                throw new YopClientException("Config Error, Main ServerRoot NotFound" + serverRootType);
            }

            // 主域名正常
            if (!isExcludeServerRoots(mainServer, excludeServerRoots)) {
                return new UriResource(mainServer);
            }

            // 主域名故障，临时启用备选域名
            final List<URI> backupServers = this.serverRootRouting.getBackupServers().get(serverRootType);
            if (CollectionUtils.isNotEmpty(backupServers)) {
                for (URI backup : backupServers) {
                    if (!isExcludeServerRoots(backup, excludeServerRoots)) {
                        return new UriResource(backup);
                    }
                }
            }

            // 备用域名故障，选用最早故障的域名
            return BLOCK_SERVER_POOL.select(serverRootType.name(), mainServer,
                    this.serverRootRouting.getAllServers().get(serverRootType));
        }
    }

    protected boolean isExcludeServerRoots(URI serverRoot, List<URI> excludeServerRoots) {
        return null != excludeServerRoots && null != serverRoot && excludeServerRoots.contains(serverRoot);
    }

    protected URI independentServerRoot(String apiGroup, Request<?> request) {
        try {
            URI serverRoot = request.isYosRequest() ? space.getYosServerRoot() : space.getServerRoot();
            return new URI(serverRoot.getScheme(), serverRoot.getUserInfo(),
                    getIndependentApiGroupHost(apiGroup, serverRoot.getHost(), request.isYosRequest()),
                    serverRoot.getPort(), serverRoot.getPath(), serverRoot.getQuery(), serverRoot.getFragment());
        } catch (Exception ex) {
            throw new YopClientException("Route Request Failure, ex:", ex);
        }
    }

    protected String getIndependentApiGroupHost(String apiGroup, String originHost, boolean isYosRequest) {
        //目前只有普通api的请求才需要路由到独立网关
        if (isYosRequest) {
            return originHost;
        }
        int index = StringUtils.indexOf(originHost, CharacterConstants.DOT);
        return StringUtils.substring(originHost, 0, index) + CharacterConstants.DASH_LINE + apiGroup + StringUtils.substring(originHost, index);
    }

    public enum ServerRootType {
        COMMON,
        YOS,
        @Deprecated
        SANDBOX
    }

    protected static class ServerRootInfo {
        public static ServerRootInfo DEFAULT_INFO = new ServerRootInfo(YopConstants.YOP_DEFAULT_PROVIDER,
                YopConstants.YOP_DEFAULT_ENV, ServerRootType.COMMON);
        private String provider;
        private String env;
        private ServerRootType serverRootType;

        public ServerRootInfo(String provider, String env, ServerRootType serverRootType) {
            this.provider = provider;
            this.env = env;
            this.serverRootType = serverRootType;
        }

        public String getProvider() {
            return provider;
        }

        public String getEnv() {
            return env;
        }

        public ServerRootType getServerRootType() {
            return serverRootType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(provider, env, serverRootType);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ServerRootInfo) {
                ServerRootInfo that = (ServerRootInfo) obj;
                return Objects.equals(this.provider, that.provider) &&
                        Objects.equals(this.env, that.env) &&
                        Objects.equals(this.serverRootType, that.serverRootType);
            }
            return false;
        }
    }

}
