package com.yeepay.yop.sdk.client;

import com.yeepay.g3.core.yop.sdk.sample.auth.AuthorizationReqRegistry;
import com.yeepay.g3.core.yop.sdk.sample.auth.YopCredentialsProvider;
import com.yeepay.g3.core.yop.sdk.sample.client.support.ClientParamsSupport;

import java.net.URI;

/**
 * title: 服务客户端builder<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2017<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/1 18:23
 */
public abstract class AbstractServiceClientBuilder<SubClass extends AbstractServiceClientBuilder, ServiceInterfaceToBuild> {

    private YopCredentialsProvider credentialsProvider;

    private String endpoint;

    private String yosEndPoint;

    private String sandboxEndPoint;

    private ClientConfiguration clientConfiguration;

    public final ServiceInterfaceToBuild build() {
        ClientParams defaultClientParams = ClientParamsSupport.getDefaultClientParams();
        ClientParams clientParams = ClientParams.Builder.aClientParams()
                .withCredentialsProvider(credentialsProvider == null ? defaultClientParams.getCredentialsProvider() : credentialsProvider)
                .withClientConfiguration(clientConfiguration == null ? defaultClientParams.getClientConfiguration() : clientConfiguration)
                .withEndPoint(endpoint == null ? defaultClientParams.getEndPoint() : URI.create(endpoint))
                .withYosEndPoint(yosEndPoint == null ? defaultClientParams.getYosEndPoint() : URI.create(yosEndPoint))
                .withSandboxEndPoint(sandboxEndPoint == null ? defaultClientParams.getSandboxEndPoint() : URI.create(sandboxEndPoint))
                .withAuthorizationReqRegistry(authorizationReqRegistry())
                .build();
        return build(clientParams);
    }

    @SuppressWarnings("unchecked")
    private SubClass getSubclass() {
        return (SubClass) this;
    }

    public SubClass withCredentialsProvider(YopCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return getSubclass();
    }

    public SubClass withClientConfiguration(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
        return getSubclass();
    }

    public SubClass withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return getSubclass();
    }

    public SubClass withYosEndpoint(String yosEndpoint) {
        this.yosEndPoint = yosEndpoint;
        return getSubclass();
    }

    public SubClass withSandboxEndPoint(String sandboxEndPoint) {
        this.sandboxEndPoint = sandboxEndPoint;
        return getSubclass();
    }

    protected abstract AuthorizationReqRegistry authorizationReqRegistry();

    protected abstract ServiceInterfaceToBuild build(ClientParams params);
}
