package com.yeepay.yop.sdk.client;

import com.yeepay.g3.core.yop.sdk.sample.auth.AuthorizationReqRegistry;
import com.yeepay.g3.core.yop.sdk.sample.auth.YopCredentialsProvider;

import java.net.URI;

/**
 * title: client参数<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2017<br/>
 * Company: 易宝支付(YeePay)<br/>
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

    private ClientParams(URI endPoint, URI yosEndPoint, URI sandboxEndPoint, ClientConfiguration clientConfiguration,
                         AuthorizationReqRegistry authorizationReqRegistry, YopCredentialsProvider credentialsProvider) {
        this.endPoint = endPoint;
        this.yosEndPoint = yosEndPoint;
        this.sandboxEndPoint = sandboxEndPoint;
        this.clientConfiguration = clientConfiguration;
        this.authorizationReqRegistry = authorizationReqRegistry;
        this.credentialsProvider = credentialsProvider;
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

    public static final class Builder {
        private URI endPoint;
        private URI yosEndPoint;
        private URI sandboxEndPoint;
        private ClientConfiguration clientConfiguration;
        private AuthorizationReqRegistry authorizationReqRegistry;
        private YopCredentialsProvider credentialsProvider;

        private Builder() {
        }

        public static Builder aClientParams() {
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

        public ClientParams build() {
            return new ClientParams(endPoint, yosEndPoint, sandboxEndPoint, clientConfiguration, authorizationReqRegistry, credentialsProvider);
        }
    }
}
