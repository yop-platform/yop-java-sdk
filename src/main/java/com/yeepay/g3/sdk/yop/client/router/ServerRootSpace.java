package com.yeepay.g3.sdk.yop.client.router;

import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static com.yeepay.g3.sdk.yop.client.YopConstants.DEFAULT_PREFERRED_SERVER_ROOT;
import static com.yeepay.g3.sdk.yop.http.HttpUtils.formatServerRoot;

/**
 * title: serverRoot组空间<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2019<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-03-13 10:42
 */
public class ServerRootSpace implements Serializable {

    private static final long serialVersionUID = -3149208992494791001L;

    private final String serverRoot;

    private final URI serverRootURL;

    private final String yosServerRoot;

    private final URI yosServerRootURL;

    private final String sandboxServerRoot;

    private final URI sandboxServerRootURL;

    private final List<String> preferredEndPoint;

    private final List<String> preferredYosEndPoint;


    public ServerRootSpace(String serverRoot, String yosServerRoot, String sandboxServerRoot) {
        this.serverRoot = formatServerRoot(serverRoot);
        this.yosServerRoot = formatServerRoot(yosServerRoot);
        this.sandboxServerRoot = formatServerRoot(sandboxServerRoot);

        this.serverRootURL = URI.create(this.serverRoot);
        this.yosServerRootURL = URI.create(this.yosServerRoot);
        this.sandboxServerRootURL = URI.create(this.sandboxServerRoot);

        this.preferredEndPoint = DEFAULT_PREFERRED_SERVER_ROOT;
        this.preferredYosEndPoint = Collections.emptyList();
    }

    public ServerRootSpace(String serverRoot, String yosServerRoot, String sandboxServerRoot,
                           List<String> preferredEndPoint, List<String> preferredYosEndPoint) {
        this.serverRoot = formatServerRoot(serverRoot);
        this.yosServerRoot = formatServerRoot(yosServerRoot);
        this.sandboxServerRoot = formatServerRoot(sandboxServerRoot);

        this.serverRootURL = URI.create(this.serverRoot);
        this.yosServerRootURL = URI.create(this.yosServerRoot);
        this.sandboxServerRootURL = URI.create(this.sandboxServerRoot);

        this.preferredEndPoint = CollectionUtils.isEmpty(preferredEndPoint) ? DEFAULT_PREFERRED_SERVER_ROOT : preferredEndPoint;
        this.preferredYosEndPoint = preferredYosEndPoint;
    }

    public String getServerRoot() {
        return serverRoot;
    }

    public URI getServerRootURL() {
        return serverRootURL;
    }

    public String getYosServerRoot() {
        return yosServerRoot;
    }

    public URI getYosServerRootURL() {
        return yosServerRootURL;
    }

    public String getSandboxServerRoot() {
        return sandboxServerRoot;
    }

    public URI getSandboxServerRootURL() {
        return sandboxServerRootURL;
    }

    public List<String> getPreferredEndPoint() {
        return preferredEndPoint;
    }

    public List<String> getPreferredYosEndPoint() {
        return preferredYosEndPoint;
    }
}
