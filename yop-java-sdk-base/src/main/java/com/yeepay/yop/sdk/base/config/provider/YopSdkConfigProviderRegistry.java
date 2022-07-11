package com.yeepay.yop.sdk.base.config.provider;

import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import com.yeepay.yop.sdk.base.config.provider.file.YopFileSdkConfigProvider;
import com.yeepay.yop.sdk.exception.YopClientException;

/**
 * title: SDK 配置中心<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/5/17 16:07
 */
public class YopSdkConfigProviderRegistry {

    private static final YopSdkConfigProvider DEFAULT_PROVIDER = new YopFileSdkConfigProvider();

    private static volatile YopSdkConfigProvider customProvider;

    /**
     * 注册自定义自动移sdk配置提供方
     *
     * @param provider app sdk配置提供方
     */
    public static void registerProvider(YopSdkConfigProvider provider) {
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
    public static YopSdkConfigProvider getProvider() {
        return customProvider == null ? DEFAULT_PROVIDER : customProvider;
    }

    public static YopSdkConfigProvider getDefaultProvider() {
        return DEFAULT_PROVIDER;
    }

}
