package com.yeepay.yop.sdk.client.router;

import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;

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

    private final URI serverRoot;

    private final URI yosServerRoot;

    private final URI sandboxServerRoot;

    private final List<URI> preferredEndPoint;

    private final List<URI> preferredYosEndPoint;

    public ServerRootSpace(URI serverRoot, URI yosServerRoot, URI sandboxServerRoot) {
        this.serverRoot = serverRoot;
        this.yosServerRoot = yosServerRoot;
        this.preferredEndPoint = DEFAULT_PREFERRED_SERVER_ROOT;
        this.preferredYosEndPoint = Collections.emptyList();
        this.sandboxServerRoot = sandboxServerRoot;
    }

    public ServerRootSpace(URI serverRoot, URI yosServerRoot, List<URI> preferredEndPoint, List<URI> preferredYosEndPoint, URI sandboxServerRoot) {
        this.serverRoot = serverRoot;
        this.yosServerRoot = yosServerRoot;
        this.preferredEndPoint = CollectionUtils.isEmpty(preferredEndPoint) ? DEFAULT_PREFERRED_SERVER_ROOT : preferredEndPoint;
        this.preferredYosEndPoint = preferredYosEndPoint;
        this.sandboxServerRoot = sandboxServerRoot;
    }

    public URI getServerRoot() {
        return serverRoot;
    }

    public URI getYosServerRoot() {
        return yosServerRoot;
    }

    public URI getSandboxServerRoot() {
        return sandboxServerRoot;
    }

    public List<URI> getPreferredEndPoint() {
        return preferredEndPoint;
    }

    public List<URI> getPreferredYosEndPoint() {
        return preferredYosEndPoint;
    }
}
