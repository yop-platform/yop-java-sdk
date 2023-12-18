/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke.config;

/**
 * title: 熔断配置提供者<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/6
 */
public interface CircuitBreakerConfigProvider {

    /**
     * 获取全局熔断配置
     *
     * @return 全局熔断配置
     */
    CircuitBreakerConfig<?> getConfig();

    /**
     * 获取特定资源熔断配置
     *
     * @param resource 资源
     * @return 熔断配置
     */
    CircuitBreakerConfig<?> getConfig(Object resource);
}
