package com.yeepay.yop.sdk.client;

import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProvider;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqRegistry;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;

import java.net.URI;
import java.util.List;
import java.util.Map;

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

    private final boolean inner;

    private final String provider;

    private final String env;

    private final URI endPoint;

    private final URI yosEndPoint;

    private final List<URI> preferredEndPoint;

    private final List<URI> preferredYosEndPoint;

    private final Map<URI, Integer> endPointWeightMap;

    private final URI sandboxEndPoint;

    private final ClientConfiguration clientConfiguration;

    private final AuthorizationReqRegistry authorizationReqRegistry;

    private final YopCredentialsProvider credentialsProvider;

    private final YopSdkConfigProvider yopSdkConfigProvider;

    private final YopPlatformCredentialsProvider platformCredentialsProvider;

    private String clientId;

    private ClientParams(boolean inner, String provider,String env,
                         URI endPoint, URI yosEndPoint, List<URI> preferredEndPoint, List<URI> preferredYosEndPoint,
                         Map<URI, Integer> endPointWeightMap, URI sandboxEndPoint,
                         ClientConfiguration clientConfiguration,
                         AuthorizationReqRegistry authorizationReqRegistry,
                         YopCredentialsProvider credentialsProvider, YopSdkConfigProvider yopSdkConfigProvider,
                         YopPlatformCredentialsProvider platformCredentialsProvider) {
        this.inner = inner;
        this.endPoint = endPoint;
        this.yosEndPoint = yosEndPoint;
        this.preferredEndPoint = preferredEndPoint;
        this.preferredYosEndPoint = preferredYosEndPoint;
        this.endPointWeightMap = endPointWeightMap;
        this.sandboxEndPoint = sandboxEndPoint;
        this.clientConfiguration = clientConfiguration;
        this.authorizationReqRegistry = authorizationReqRegistry;
        this.credentialsProvider = credentialsProvider;
        this.yopSdkConfigProvider = yopSdkConfigProvider;
        this.platformCredentialsProvider = platformCredentialsProvider;
        this.provider = provider;
        this.env = env;
    }

    public String getProvider() {
        return provider;
    }

    public String getEnv() {
        return env;
    }

    public URI getEndPoint() {
        return endPoint;
    }

    public URI getYosEndPoint() {
        return yosEndPoint;
    }

    public List<URI> getPreferredEndPoint() {
        return preferredEndPoint;
    }

    public List<URI> getPreferredYosEndPoint() {
        return preferredYosEndPoint;
    }

    public Map<URI, Integer> getEndPointWeightMap() {
        return endPointWeightMap;
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

    public YopPlatformCredentialsProvider getPlatformCredentialsProvider() {
        return platformCredentialsProvider;
    }

    void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public static final class Builder {

        private boolean inner;
        private String provider;
        private String env;
        private URI endPoint;
        private URI yosEndPoint;
        private List<URI> preferredEndPoint;
        private List<URI> preferredYosEndPoint;
        private Map<URI, Integer> endPointWeightMap;
        private URI sandboxEndPoint;
        private ClientConfiguration clientConfiguration;
        private AuthorizationReqRegistry authorizationReqRegistry;
        private YopCredentialsProvider credentialsProvider;
        private YopSdkConfigProvider yopSdkConfigProvider;
        private YopPlatformCredentialsProvider platformCredentialsProvider;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withInner(boolean inner) {
            this.inner = inner;
            return this;
        }

        public Builder withProvider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder withEnv(String env) {
            this.env = env;
            return this;
        }

        public Builder withEndPoint(URI endPoint) {
            this.endPoint = endPoint;
            return this;
        }

        public Builder withYosEndPoint(URI yosEndPoint) {
            this.yosEndPoint = yosEndPoint;
            return this;
        }

        public Builder withPreferredEndPoint(List<URI> preferredEndPoint) {
            this.preferredEndPoint = preferredEndPoint;
            return this;
        }

        public Builder withPreferredYosEndPoint(List<URI> preferredYosEndPoint) {
            this.preferredYosEndPoint = preferredYosEndPoint;
            return this;
        }

        public Builder withEndPointWeightMap(Map<URI, Integer> endPointWeightMap) {
            this.endPointWeightMap = endPointWeightMap;
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

        public Builder withPlatformCredentialsProvider(YopPlatformCredentialsProvider platformCredentialsProvider) {
            this.platformCredentialsProvider = platformCredentialsProvider;
            return this;
        }

        public ClientParams build() {
            return new ClientParams(inner, provider, env, endPoint, yosEndPoint, preferredEndPoint, preferredYosEndPoint, this.endPointWeightMap, sandboxEndPoint,
                    clientConfiguration, authorizationReqRegistry,
                    credentialsProvider, yopSdkConfigProvider, platformCredentialsProvider);
        }
    }
}
