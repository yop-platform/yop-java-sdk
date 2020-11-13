package com.yeepay.yop.sdk.client;

import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqRegistry;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;

import java.net.URI;

/**
 * title: client参数<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/23 17:24
 */
public class ClientParams {

    private final URI endPoint;

    private final URI yosEndPoint;

    private final URI sandboxEndPoint;

    private final ClientConfiguration clientConfiguration;

    private final AuthorizationReqRegistry authorizationReqRegistry;

    private final YopCredentialsProvider credentialsProvider;

    private final YopSdkConfigProvider yopSdkConfigProvider;

    private ClientParams(URI endPoint, URI yosEndPoint, URI sandboxEndPoint,
                         ClientConfiguration clientConfiguration,
                         AuthorizationReqRegistry authorizationReqRegistry,
                         YopCredentialsProvider credentialsProvider, YopSdkConfigProvider yopSdkConfigProvider) {
        this.endPoint = endPoint;
        this.yosEndPoint = yosEndPoint;
        this.sandboxEndPoint = sandboxEndPoint;
        this.clientConfiguration = clientConfiguration;
        this.authorizationReqRegistry = authorizationReqRegistry;
        this.credentialsProvider = credentialsProvider;
        this.yopSdkConfigProvider = yopSdkConfigProvider;
    }

    public URI getEndPoint() {
        return endPoint;
    }

    public URI getYosEndPoint() {
        return yosEndPoint;
    }

    public URI getSandboxEndPoint() {
        return sandboxEndPoint;
    }

    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    public AuthorizationReqRegistry getAuthorizationReqRegistry() {
        return authorizationReqRegistry;
    }

    public YopCredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public YopSdkConfigProvider getYopSdkConfigProvider() {
        return yopSdkConfigProvider;
    }

    public static final class Builder {
        private URI endPoint;
        private URI yosEndPoint;
        private URI sandboxEndPoint;
        private ClientConfiguration clientConfiguration;
        private AuthorizationReqRegistry authorizationReqRegistry;
        private YopCredentialsProvider credentialsProvider;
        private YopSdkConfigProvider yopSdkConfigProvider;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withEndPoint(URI endPoint) {
            this.endPoint = endPoint;
            return this;
        }

        public Builder withYosEndPoint(URI yosEndPoint) {
            this.yosEndPoint = yosEndPoint;
            return this;
        }

        public Builder withSandboxEndPoint(URI sandboxEndPoint) {
            this.sandboxEndPoint = sandboxEndPoint;
            return this;
        }

        public Builder withClientConfiguration(ClientConfiguration clientConfiguration) {
            this.clientConfiguration = clientConfiguration;
            return this;
        }

        public Builder withAuthorizationReqRegistry(AuthorizationReqRegistry authorizationReqRegistry) {
            this.authorizationReqRegistry = authorizationReqRegistry;
            return this;
        }

        public Builder withCredentialsProvider(YopCredentialsProvider credentialsProvider) {
            this.credentialsProvider = credentialsProvider;
            return this;
        }

        public Builder withYopSdkConfigProvider(YopSdkConfigProvider yopSdkConfigProvider) {
            this.yopSdkConfigProvider = yopSdkConfigProvider;
            return this;
        }

        public ClientParams build() {
            return new ClientParams(endPoint, yosEndPoint, sandboxEndPoint,
                    clientConfiguration, authorizationReqRegistry,
                    credentialsProvider, yopSdkConfigProvider);
        }
    }
}
