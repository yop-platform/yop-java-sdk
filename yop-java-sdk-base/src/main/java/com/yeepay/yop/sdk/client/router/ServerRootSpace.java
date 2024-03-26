package com.yeepay.yop.sdk.client.router;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;
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

    private static final long serialVersionUID = -1L;

    private final String provider;

    private final String env;

    private final URI serverRoot;

    private final URI yosServerRoot;

    private final List<URI> preferredEndPoint;

    private final List<URI> preferredYosEndPoint;

    private final URI sandboxServerRoot;

    private final String serverGroup;

    public ServerRootSpace(URI serverRoot, URI yosServerRoot, URI sandboxServerRoot) {
        this.provider = YopConstants.YOP_DEFAULT_PROVIDER;
        this.env = YopConstants.YOP_DEFAULT_ENV;
        this.serverRoot = serverRoot;
        this.yosServerRoot = yosServerRoot;
        this.preferredEndPoint = DEFAULT_PREFERRED_SERVER_ROOT;
        this.preferredYosEndPoint = Collections.emptyList();
        this.sandboxServerRoot = sandboxServerRoot;
        this.serverGroup = CharacterConstants.EMPTY;
    }

    public ServerRootSpace(URI serverRoot, URI yosServerRoot,
                           List<URI> preferredEndPoint, List<URI> preferredYosEndPoint,
                           URI sandboxServerRoot) {
        this.provider = YopConstants.YOP_DEFAULT_PROVIDER;
        this.env = YopConstants.YOP_DEFAULT_ENV;
        this.serverRoot = serverRoot;
        this.yosServerRoot = yosServerRoot;
        this.preferredEndPoint = CollectionUtils.isEmpty(preferredEndPoint) ? DEFAULT_PREFERRED_SERVER_ROOT : preferredEndPoint;
        this.preferredYosEndPoint = preferredYosEndPoint;
        this.sandboxServerRoot = sandboxServerRoot;
        this.serverGroup = CharacterConstants.EMPTY;
    }

    public ServerRootSpace(String provider, String env,
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
        this.serverGroup = CharacterConstants.EMPTY;
    }

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
}
