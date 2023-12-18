package com.yeepay.yop.sdk.model;

import com.yeepay.yop.sdk.auth.credentials.YopCredentials;

import java.util.List;
import java.util.Map;

/**
 * title: Generic representation of request level configuration<br>
 * description:
 * <p>
 * The customer interface for specifying
 * request level configuration is a base request class with configuration methods.
 * </p>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/14 17:24
 */
public class YopRequestConfig {

    private String appKey;

    private String securityReq;

    private YopCredentials credentials;

    private Map<String, String> customRequestHeaders;

    private Map<String, List<String>> customQueryParameters;

    /**
     * 获取数据的超时时间, 单位：ms
     */
    private int readTimeout;

    /**
     * 建立连接的超时, 单位：ms
     */
    private int connectTimeout;

    private Boolean needEncrypt;

    private Boolean skipVerifySign;

    /**
     * 指定服务器根路径(请求级别配置)
     *
     * 作为前缀，拼接apiUri，构造最终请求路径
     * 如未指定，会默认从com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider#getConfig()获取全局配置
     */
    private String serverRoot;

    /**
     * 启用断路器
     */
    private Boolean enableCircuitBreaker = true;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getSecurityReq() {
        return securityReq;
    }

    public void setSecurityReq(String securityReq) {
        this.securityReq = securityReq;
    }

    public YopCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(YopCredentials credentials) {
        this.credentials = credentials;
    }

    public Map<String, String> getCustomRequestHeaders() {
        return customRequestHeaders;
    }

    public void setCustomRequestHeaders(Map<String, String> customRequestHeaders) {
        this.customRequestHeaders = customRequestHeaders;
    }

    public Map<String, List<String>> getCustomQueryParameters() {
        return customQueryParameters;
    }

    public void setCustomQueryParameters(Map<String, List<String>> customQueryParameters) {
        this.customQueryParameters = customQueryParameters;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Boolean getNeedEncrypt() {
        return needEncrypt;
    }

    public void setNeedEncrypt(Boolean needEncrypt) {
        this.needEncrypt = needEncrypt;
    }

    public Boolean getSkipVerifySign() {
        return skipVerifySign;
    }

    public void setSkipVerifySign(Boolean skipVerifySign) {
        this.skipVerifySign = skipVerifySign;
    }

    public String getServerRoot() {
        return serverRoot;
    }

    public Boolean getEnableCircuitBreaker() {
        return enableCircuitBreaker;
    }

    public YopRequestConfig setEnableCircuitBreaker(Boolean enableCircuitBreaker) {
        this.enableCircuitBreaker = enableCircuitBreaker;
        return this;
    }

    public YopRequestConfig setServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;
        return this;
    }

    public static final class Builder {
        private String appKey;
        private String securityReq;
        private YopCredentials credentials;
        private Map<String, String> customRequestHeaders;
        private Map<String, List<String>> customQueryParameters;
        private int readTimeout;
        private int connectTimeout;
        private Boolean needEncrypt;
        private Boolean skipVerifySign;
        private String serverRoot;
        private Boolean enableCircuitBreaker = true;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withAppKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public Builder withSecurityReq(String securityReq) {
            this.securityReq = securityReq;
            return this;
        }

        public Builder withCredentials(YopCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public Builder withCustomRequestHeaders(Map<String, String> customRequestHeaders) {
            this.customRequestHeaders = customRequestHeaders;
            return this;
        }

        public Builder withCustomQueryParameters(Map<String, List<String>> customQueryParameters) {
            this.customQueryParameters = customQueryParameters;
            return this;
        }

        public Builder withRequestTimeout(Integer requestTimeout) {
            this.readTimeout = requestTimeout;
            return this;
        }

        public Builder withConnectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder withNeedEncrypt(Boolean needEncrypt) {
            this.needEncrypt = needEncrypt;
            return this;
        }

        public Builder withSkipVerifySign(Boolean skipVerifySign) {
            this.skipVerifySign = skipVerifySign;
            return this;
        }

        public Builder withServerRoot(String serverRoot) {
            this.serverRoot = serverRoot;
            return this;
        }

        public Builder withEnableCircuitBreaker(Boolean enableCircuitBreaker) {
            this.enableCircuitBreaker = enableCircuitBreaker;
            return this;
        }

        public YopRequestConfig build() {
            YopRequestConfig requestConfig = new YopRequestConfig();
            requestConfig.setAppKey(appKey);
            requestConfig.setSecurityReq(securityReq);
            requestConfig.setCredentials(credentials);
            requestConfig.setCustomRequestHeaders(customRequestHeaders);
            requestConfig.setCustomQueryParameters(customQueryParameters);
            requestConfig.setReadTimeout(readTimeout);
            requestConfig.setConnectTimeout(connectTimeout);
            requestConfig.setNeedEncrypt(needEncrypt);
            requestConfig.setSkipVerifySign(skipVerifySign);
            requestConfig.setServerRoot(serverRoot);
            requestConfig.setEnableCircuitBreaker(enableCircuitBreaker);
            return requestConfig;
        }
    }
}
