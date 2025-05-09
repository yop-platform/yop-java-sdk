package com.yeepay.yop.sdk.config.provider.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Set;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/12/26 下午3:50
 */
public final class YopHttpClientConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 建立连接的超时
     */
    @JsonProperty("connect_timeout")
    private int connectTimeout = 3000;

    /**
     * 从连接池获取到连接的超时时间
     */
    @JsonProperty("connect_request_timeout")
    private int connectRequestTimeout = 3000;

    /**
     * 获取数据的超时时间
     */
    @JsonProperty("read_timeout")
    private int readTimeout = 30000;

    @JsonProperty("max_conn_total")
    private int maxConnTotal = 200;

    @JsonProperty("max_conn_per_route")
    private int maxConnPerRoute = 100;

    @JsonProperty("client_impl")
    private String clientImpl;

    @JsonProperty("retry_exceptions")
    private Set<String> retryExceptions;

    @JsonProperty("max_retry_count")
    private int maxRetryCount = 3;

    @JsonProperty("circuit_breaker")
    private YopCircuitBreakerConfig circuitBreakerConfig;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectRequestTimeout() {
        return connectRequestTimeout;
    }

    public void setConnectRequestTimeout(int connectRequestTimeout) {
        this.connectRequestTimeout = connectRequestTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxConnTotal() {
        return maxConnTotal;
    }

    public void setMaxConnTotal(int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
    }

    public int getMaxConnPerRoute() {
        return maxConnPerRoute;
    }

    public void setMaxConnPerRoute(int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
    }

    public String getClientImpl() {
        return clientImpl;
    }

    public void setClientImpl(String clientImpl) {
        this.clientImpl = clientImpl;
    }

    public Set<String> getRetryExceptions() {
        return retryExceptions;
    }

    public void setRetryExceptions(Set<String> retryExceptions) {
        this.retryExceptions = retryExceptions;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public YopCircuitBreakerConfig getCircuitBreakerConfig() {
        return circuitBreakerConfig;
    }

    public void setCircuitBreakerConfig(YopCircuitBreakerConfig circuitBreakerConfig) {
        this.circuitBreakerConfig = circuitBreakerConfig;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
