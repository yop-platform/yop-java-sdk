package com.yeepay.yop.sdk.client.router;

import java.io.Serializable;
import java.net.URI;

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

    public ServerRootSpace(URI serverRoot, URI yosServerRoot, URI sandboxServerRoot) {
        this.serverRoot = serverRoot;
        this.yosServerRoot = yosServerRoot;
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
}
