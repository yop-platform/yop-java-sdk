package com.yeepay.yop.sdk.client.router;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.utils.RandomUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.yeepay.yop.sdk.YopConstants.DEFAULT_PREFERRED_SERVER_ROOT;

/**
 * title: serverRoot空间<br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-03-12 17:22
 */
public class ServerRootSpace implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRootSpace.class);

    private static final long serialVersionUID = -1L;

    private final String provider;

    private final String env;

    private final URI serverRoot;

    private final URI yosServerRoot;

    private final List<URI> preferredEndPoint;

    private final List<URI> preferredYosEndPoint;

    private final URI sandboxServerRoot;

    private final String serverGroup;

    private final Map<ServerRootType, URI> mainServers;

    private final Map<ServerRootType, List<URI>> backupServers;

    public ServerRootSpace(String provider, String env, String serverGroup,
                           URI serverRoot, URI yosServerRoot,
                           List<URI> preferredEndPoint, List<URI> preferredYosEndPoint,
                           URI sandboxServerRoot) {
        this.provider = provider;
        this.env = env;
        this.serverRoot = serverRoot;
        this.yosServerRoot = yosServerRoot;
        this.preferredEndPoint = CollectionUtils.isEmpty(preferredEndPoint) ? DEFAULT_PREFERRED_SERVER_ROOT : preferredEndPoint;
        this.preferredYosEndPoint = preferredYosEndPoint;
        this.sandboxServerRoot = sandboxServerRoot;
        this.serverGroup = serverGroup;

        this.mainServers = Maps.newConcurrentMap();
        this.backupServers = Maps.newConcurrentMap();

        // 随机选主:common
        final List<URI> randomCommonList = RandomUtils.randomList(getPreferredEndPoint());
        if (recordMainServer(randomCommonList.remove(0), ServerRootType.COMMON, mainServers)) {
            backupServers.put(ServerRootType.COMMON, randomCommonList);
        }
        // yos
        final List<URI> randomYosList = RandomUtils.randomList(CollectionUtils.isEmpty(getPreferredYosEndPoint())
                ? Lists.newArrayList(getYosServerRoot()) : getPreferredYosEndPoint());
        if (recordMainServer(randomYosList.remove(0), ServerRootType.YOS, mainServers)) {
            backupServers.put(ServerRootType.YOS, randomYosList);
        }
        // sandbox 兼容老沙箱
        final List<URI> randomSandboxList = RandomUtils.randomList(Lists.newArrayList(getSandboxServerRoot()));
        if (recordMainServer(randomSandboxList.remove(0), ServerRootType.SANDBOX, mainServers)) {
            backupServers.put(ServerRootType.SANDBOX, randomSandboxList);
        }
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


    public String getProvider() {
        return provider;
    }

    public String getEnv() {
        return env;
    }

    public URI getServerRoot() {
        return serverRoot;
    }

    public URI getYosServerRoot() {
        return yosServerRoot;
    }

    public List<URI> getPreferredEndPoint() {
        return preferredEndPoint;
    }

    public List<URI> getPreferredYosEndPoint() {
        return preferredYosEndPoint;
    }

    public URI getSandboxServerRoot() {
        return sandboxServerRoot;
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

    @Override
    public int hashCode() {
        return Objects.hash(this.provider, this.env, this.serverGroup, this.serverRoot,
                this.yosServerRoot, this.sandboxServerRoot, this.preferredEndPoint, this.preferredYosEndPoint);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServerRootSpace) {
            final ServerRootSpace that = (ServerRootSpace) obj;
            return Objects.equals(this.provider, that.provider) &&
                    Objects.equals(this.env, that.env) &&
                    Objects.equals(this.serverGroup, that.serverGroup) &&
                    Objects.equals(this.serverRoot, that.serverRoot) &&
                    Objects.equals(this.yosServerRoot, that.yosServerRoot) &&
                    Objects.equals(this.sandboxServerRoot, that.sandboxServerRoot) &&
                    Objects.equals(this.preferredEndPoint, that.preferredEndPoint) &&
                    Objects.equals(this.preferredYosEndPoint, that.preferredYosEndPoint);
        }
        return false;
    }

    public enum ServerRootType {
        COMMON,
        YOS,
        @Deprecated
        SANDBOX
    }
}
