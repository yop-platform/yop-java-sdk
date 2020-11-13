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
public class RequestConfig {

    private String appKey;

    private String securityReq;

    private YopCredentials credentials;

    private Map<String, String> customRequestHeaders;

    private Map<String, List<String>> customQueryParameters;

    private Integer requestTimeout;

    private Integer clientExecutionTimeout;

    private Boolean needEncrypt;

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

    public Integer getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public Integer getClientExecutionTimeout() {
        return clientExecutionTimeout;
    }

    public void setClientExecutionTimeout(Integer clientExecutionTimeout) {
        this.clientExecutionTimeout = clientExecutionTimeout;
    }

    public Boolean getNeedEncrypt() {
        return needEncrypt;
    }

    public void setNeedEncrypt(Boolean needEncrypt) {
        this.needEncrypt = needEncrypt;
    }

    public static final class Builder {
        private String appKey;
        private String securityReq;
        private YopCredentials credentials;
        private Map<String, String> customRequestHeaders;
        private Map<String, List<String>> customQueryParameters;
        private Integer requestTimeout;
        private Integer clientExecutionTimeout;
        private Boolean needEncrypt;

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
            this.requestTimeout = requestTimeout;
            return this;
        }

        public Builder withClientExecutionTimeout(Integer clientExecutionTimeout) {
            this.clientExecutionTimeout = clientExecutionTimeout;
            return this;
        }

        public Builder withNeedEncrypt(Boolean needEncrypt) {
            this.needEncrypt = needEncrypt;
            return this;
        }

        public RequestConfig build() {
            RequestConfig requestConfig = new RequestConfig();
            requestConfig.setAppKey(appKey);
            requestConfig.setSecurityReq(securityReq);
            requestConfig.setCredentials(credentials);
            requestConfig.setCustomRequestHeaders(customRequestHeaders);
            requestConfig.setCustomQueryParameters(customQueryParameters);
            requestConfig.setRequestTimeout(requestTimeout);
            requestConfig.setClientExecutionTimeout(clientExecutionTimeout);
            requestConfig.setNeedEncrypt(needEncrypt);
            return requestConfig;
        }
    }
}
