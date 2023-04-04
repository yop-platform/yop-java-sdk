package com.yeepay.yop.sdk.client.router;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;

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

    private final List<URI> preferredEndPoint;

    private final List<URI> preferredYosEndPoint;

    private final URI sandboxServerRoot;

    public ServerRootSpace(URI serverRoot, URI yosServerRoot, URI sandboxServerRoot) {
        this.serverRoot = serverRoot;
        this.yosServerRoot = yosServerRoot;
        this.preferredEndPoint = Collections.emptyList();
        this.preferredYosEndPoint = Collections.emptyList();
        this.sandboxServerRoot = sandboxServerRoot;
    }

    public ServerRootSpace(URI serverRoot, URI yosServerRoot, List<URI> preferredEndPoint, List<URI> preferredYosEndPoint, URI sandboxServerRoot) {
        this.serverRoot = serverRoot;
        this.yosServerRoot = yosServerRoot;
        this.preferredEndPoint = preferredEndPoint;
        this.preferredYosEndPoint = preferredYosEndPoint;
        this.sandboxServerRoot = sandboxServerRoot;
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
}
