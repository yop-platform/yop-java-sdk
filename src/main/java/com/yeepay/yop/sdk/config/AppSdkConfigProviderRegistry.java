package com.yeepay.yop.sdk.config;


import com.yeepay.g3.core.yop.sdk.sample.config.provider.DefaultFileAppSdkConfigProvider;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopClientException;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/5/17 16:07
 */
public class AppSdkConfigProviderRegistry {

    private static final AppSdkConfigProvider DEFAULT_PROVIDER = new DefaultFileAppSdkConfigProvider();

    private static volatile AppSdkConfigProvider customProvider;

    /**
     * 注册自定义自动移sdk配置提供方
     *
     * @param provider app sdk配置提供方
     */
    public static void registerCustomProvider(AppSdkConfigProvider provider) {
        if (provider == null) {
            throw new YopClientException("customProvider can't be null.");
        }
        customProvider = provider;
    }

    /**
     * 获取Sdk配置提供方（用户自定义的优先）
     *
     * @return appSdkConfigProvider
     */
    public static AppSdkConfigProvider getProvider() {
        return customProvider == null ? DEFAULT_PROVIDER : customProvider;
    }

}
