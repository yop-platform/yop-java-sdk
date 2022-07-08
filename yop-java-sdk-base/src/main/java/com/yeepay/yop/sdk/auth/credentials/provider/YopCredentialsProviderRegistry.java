package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.exception.YopClientException;

import java.util.ServiceLoader;

/**
 * title: 凭证提供方注册中心<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 20/11/23 15:42
 */
public class YopCredentialsProviderRegistry {

    private static final YopCredentialsProvider DEFAULT_PROVIDER;

    static {
        ServiceLoader<YopCredentialsProvider> serviceLoader = ServiceLoader.load(YopCredentialsProvider.class);
        DEFAULT_PROVIDER = serviceLoader.iterator().next();
    }

    private static volatile YopCredentialsProvider CUSTOM_PROVIDER = null;

    public static void registerProvider(YopCredentialsProvider yopCredentialsProvider) {
        if (yopCredentialsProvider == null) {
            throw new YopClientException("customProvider can't be null.");
        }
        CUSTOM_PROVIDER = yopCredentialsProvider;
    }

    public static YopCredentialsProvider getProvider() {
        return CUSTOM_PROVIDER == null ? DEFAULT_PROVIDER : CUSTOM_PROVIDER;
    }

}
