package com.yeepay.yop.sdk.client;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProvider;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqRegistry;
import com.yeepay.yop.sdk.base.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.client.support.ClientConfigurationSupport;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import com.yeepay.yop.sdk.invoke.RouterPolicy;
import com.yeepay.yop.sdk.router.config.YopRouteConfigProvider;
import com.yeepay.yop.sdk.router.config.YopRouteConfigProviderRegistry;
import com.yeepay.yop.sdk.router.policy.RouterPolicyFactory;
import com.yeepay.yop.sdk.utils.ClientUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.yeepay.yop.sdk.YopConstants.*;
import static com.yeepay.yop.sdk.constants.CharacterConstants.COLON;
import static com.yeepay.yop.sdk.utils.ClientUtils.computeClientIdSuffix;

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

    private String clientId;

    private boolean inner;

    private String provider;

    private String env;

    private YopCredentialsProvider credentialsProvider;

    private YopSdkConfigProvider yopSdkConfigProvider;

    private YopPlatformCredentialsProvider platformCredentialsProvider;

    private YopRouteConfigProvider routeConfigProvider;

    private RouterPolicy routerPolicy;

    private String endpoint;

    private List<URI> preferredEndPoint;

    private String yosEndPoint;

    private List<URI> preferredYosEndPoint;

    private String sandboxEndPoint;

    private ClientConfiguration clientConfiguration;

    public final ServiceInterfaceToBuild build() {
        provider = StringUtils.defaultString(provider, YOP_DEFAULT_PROVIDER);
        env = StringUtils.defaultString(env, YOP_DEFAULT_ENV);
        if (null == yopSdkConfigProvider) {
            yopSdkConfigProvider = YopSdkConfigProviderRegistry.getProvider();
        }
        if (null == credentialsProvider) {
            credentialsProvider = YopCredentialsProviderRegistry.getProvider();
        }
        if (null == platformCredentialsProvider) {
            platformCredentialsProvider = YopPlatformCredentialsProviderRegistry.getProvider();
        }
        if (null == routeConfigProvider) {
            routeConfigProvider = YopRouteConfigProviderRegistry.getProvider();
        }
        YopSdkConfig yopSdkConfig = yopSdkConfigProvider.getConfig(provider, env);
        if (null == clientConfiguration) {
            clientConfiguration = ClientConfigurationSupport.getClientConfiguration(yopSdkConfig);
        }
        if (CollectionUtils.isEmpty(preferredEndPoint)) {
            preferredEndPoint = CollectionUtils.isNotEmpty(yopSdkConfig.getPreferredServerRoots()) ?
                    yopSdkConfig.getPreferredServerRoots().stream().map(URI::create).collect(Collectors.toList()) : Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(preferredYosEndPoint)) {
            preferredYosEndPoint = CollectionUtils.isNotEmpty(yopSdkConfig.getPreferredYosServerRoots()) ?
                    yopSdkConfig.getPreferredYosServerRoots().stream().map(URI::create).collect(Collectors.toList()) : Collections.emptyList();
        }
        if (null == routerPolicy) {
            routerPolicy = RouterPolicyFactory.get(ROUTER_POLICY_DEFAULT);
        }
        ClientParams clientParams = ClientParams.Builder.builder()
                .withInner(this.inner)
                .withProvider(this.provider)
                .withEnv(this.env)
                .withCredentialsProvider(credentialsProvider)
                .withYopSdkConfigProvider(yopSdkConfigProvider)
                .withPlatformCredentialsProvider(platformCredentialsProvider)
                .withRouteConfigProvider(routeConfigProvider)
                .withRouterPolicy(routerPolicy)
                .withClientConfiguration(clientConfiguration)
                .withEndPoint(endpoint == null ? URI.create(StringUtils.defaultIfBlank(yopSdkConfig.getServerRoot(), YopConstants.DEFAULT_SERVER_ROOT)) : URI.create(endpoint))
                .withYosEndPoint(yosEndPoint == null ? URI.create(StringUtils.defaultIfBlank(yopSdkConfig.getYosServerRoot(), YopConstants.DEFAULT_YOS_SERVER_ROOT)) : URI.create(yosEndPoint))
                .withPreferredEndPoint(preferredEndPoint)
                .withPreferredYosEndPoint(preferredYosEndPoint)
                .withSandboxEndPoint(sandboxEndPoint == null ? URI.create(StringUtils.defaultIfBlank(yopSdkConfig.getSandboxServerRoot(), YopConstants.DEFAULT_SANDBOX_SERVER_ROOT)) : URI.create(sandboxEndPoint))
                .withAuthorizationReqRegistry(authorizationReqRegistry())
                .build();
        if (StringUtils.isBlank(this.clientId)) {
            final String clientIdSuffix = computeClientIdSuffix(clientParams);
            final SubClass subclass = getSubclass();
            final String clientBuilderClass = subclass.getClass().getCanonicalName();
            this.clientId = clientBuilderClass + COLON + clientIdSuffix;
        }
        clientParams.setClientId(this.clientId);
        ClientUtils.cacheClientConfig(this.clientId, clientParams);
        return ClientUtils.getOrBuildClientInst(clientParams, AbstractServiceClientBuilder.this::build);
    }

    @SuppressWarnings("unchecked")
    private SubClass getSubclass() {
        return (SubClass) this;
    }

    public SubClass withCredentialsProvider(YopCredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return getSubclass();
    }

    public SubClass withClientId(String clientId) {
        this.clientId = clientId;
        return getSubclass();
    }

    public SubClass withInner(boolean inner) {
        this.inner = inner;
        return getSubclass();
    }

    public SubClass withProvider(String provider) {
        this.provider = provider;
        return getSubclass();
    }

    public SubClass withEnv(String env) {
        this.env = env;
        return getSubclass();
    }

    public SubClass withYopSdkConfigProvider(YopSdkConfigProvider yopSdkConfigProvider) {
        this.yopSdkConfigProvider = yopSdkConfigProvider;
        return getSubclass();
    }

    public SubClass withPlatformCredentialsProvider(YopPlatformCredentialsProvider platformCredentialsProvider) {
        this.platformCredentialsProvider = platformCredentialsProvider;
        return getSubclass();
    }

    public SubClass withRouteConfigProvider(YopRouteConfigProvider routeConfigProvider) {
        this.routeConfigProvider = routeConfigProvider;
        return getSubclass();
    }

    public SubClass withRouterPolicy(RouterPolicy routerPolicy) {
        this.routerPolicy = routerPolicy;
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

    public SubClass withPreferredEndPoint(List<URI> preferredEndPoint) {
        this.preferredEndPoint = preferredEndPoint;
        return getSubclass();
    }

    public SubClass withYosEndpoint(String yosEndpoint) {
        this.yosEndPoint = yosEndpoint;
        return getSubclass();
    }

    public SubClass withPreferredYosEndPoint(List<URI> preferredYosEndPoint) {
        this.preferredYosEndPoint = preferredYosEndPoint;
        return getSubclass();
    }

    public SubClass withSandboxEndPoint(String sandboxEndPoint) {
        this.sandboxEndPoint = sandboxEndPoint;
        return getSubclass();
    }

    protected abstract AuthorizationReqRegistry authorizationReqRegistry();

    protected abstract ServiceInterfaceToBuild build(ClientParams params);
}
