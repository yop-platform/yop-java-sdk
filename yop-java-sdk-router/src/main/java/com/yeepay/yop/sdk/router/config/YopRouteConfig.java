/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerConfig;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Set;

/**
 * title: 路由切换策略配置<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/2/2
 */
public class YopRouteConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    public static final YopRouteConfig DEFAULT_CONFIG = new YopRouteConfig();

    /**
     * 配置需要切换域名重试异常
     */
    @JsonProperty("retry_exceptions")
    private Set<String> retryExceptions = Sets.newHashSet("java.net.UnknownHostException",
            "java.net.ConnectException:No route to host (connect failed)",
            "java.net.ConnectException:Connection refused (Connection refused)",
            "java.net.ConnectException:Connection refused: connect",
            "java.net.SocketTimeoutException:connect timed out",
            "java.net.NoRouteToHostException",
            "org.apache.http.conn.ConnectTimeoutException", "com.yeepay.shade.org.apache.http.conn.ConnectTimeoutException",
            "org.apache.http.conn.HttpHostConnectException", "com.yeepay.shade.org.apache.http.conn.HttpHostConnectException",
            "java.net.ConnectException:Connection timed out","java.net.ConnectException:连接超时");

    /**
     * 配置熔断规则
     * 默认规则：5分钟内累计5笔，或者5s内故障率达到20%即熔断域名
     * 可根据需要自行覆盖
     */
    @JsonProperty("circuit_breaker")
    private YopCircuitBreakerConfig circuitBreakerConfig = YopCircuitBreakerConfig.DEFAULT_CONFIG;

    public Set<String> getRetryExceptions() {
        return retryExceptions;
    }

    public void setRetryExceptions(Set<String> retryExceptions) {
        this.retryExceptions = retryExceptions;
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
