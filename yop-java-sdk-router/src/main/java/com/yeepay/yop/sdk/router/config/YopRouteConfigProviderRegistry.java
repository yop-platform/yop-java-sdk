/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.config;

/**
 * title: 路由配置工厂<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/2/2
 */
public class YopRouteConfigProviderRegistry {

    private static final YopRouteConfigProvider DEFAULT_PROVIDER = YopFileRouteConfigProvider.INSTANCE;
    private static volatile YopRouteConfigProvider CUSTOM_PROVIDER = null;

    public static YopRouteConfigProvider getProvider() {
        return null != CUSTOM_PROVIDER ? CUSTOM_PROVIDER : DEFAULT_PROVIDER;
    }

    public static void registerProvider(YopRouteConfigProvider customProvider) {
        if (null == CUSTOM_PROVIDER && null != customProvider) {
            CUSTOM_PROVIDER = customProvider;
        }
    }

    public static YopRouteConfigProvider getDefaultProvider() {
        return DEFAULT_PROVIDER;
    }

}
