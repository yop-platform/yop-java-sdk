/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.config;

/**
 * title: 路由配置提供者<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/2/2
 */
public interface YopRouteConfigProvider {

    /**
     * 获取默认配置
     *
     * @return YopRouteConfig
     */
    YopRouteConfig getRouteConfig();

    /**
     * 获取指定配置
     *
     * @param configKey 指定标识
     * @return YopRouteConfig
     */
    YopRouteConfig getRouteConfig(String configKey);
}
