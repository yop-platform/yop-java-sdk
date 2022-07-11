package com.yeepay.yop.sdk.client;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqRegistry;
import com.yeepay.yop.sdk.client.support.ClientConfigurationSupport;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import com.yeepay.yop.sdk.base.config.provider.YopSdkConfigProviderRegistry;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;

/**
 * title: 服务客户端builder<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/1 18:23
 */
public abstract class AbstractServiceClientBuilder<SubClass extends AbstractServiceClientBuilder, ServiceInterfaceToBuild> {

    private YopCredentialsProvider credentialsProvider;

    private YopSdkConfigProvider yopSdkConfigProvider;

    private String endpoint;

    private String yosEndPoint;

    private String sandboxEndPoint;

    private ClientConfiguration clientConfiguration;

    public final ServiceInterfaceToBuild build() {
        if (null == yopSdkConfigProvider) {
            yopSdkConfigProvider = YopSdkConfigProviderRegistry.getProvider();
        }
        YopSdkConfig yopSdkConfig = yopSdkConfigProvider.getConfig();
        ClientParams clientParams = ClientParams.Builder.builder()
                .withCredentialsProvider(credentialsProvider == null ? YopCredentialsProviderRegistry.getProvider() : credentialsProvider)
                .withYopSdkConfigProvider(yopSdkConfigProvider)
                .withClientConfiguration(clientConfiguration == null ? ClientConfigurationSupport.getClientConfiguration(yopSdkConfig) : clientConfiguration)
                .withEndPoint(endpoint == null ? URI.create(StringUtils.defaultIfBlank(yopSdkConfig.getServerRoot(), YopConstants.DEFAULT_SERVER_ROOT)) : URI.create(endpoint))
                .withYosEndPoint(yosEndPoint == null ? URI.create(StringUtils.defaultIfBlank(yopSdkConfig.getYosServerRoot(), YopConstants.DEFAULT_YOS_SERVER_ROOT)) : URI.create(yosEndPoint))
                .withSandboxEndPoint(sandboxEndPoint == null ? URI.create(StringUtils.defaultIfBlank(yopSdkConfig.getSandboxServerRoot(), YopConstants.DEFAULT_SANDBOX_SERVER_ROOT)) : URI.create(sandboxEndPoint))
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

    public SubClass withYopSdkConfigProvider(YopSdkConfigProvider yopSdkConfigProvider) {
        this.yopSdkConfigProvider = yopSdkConfigProvider;
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
