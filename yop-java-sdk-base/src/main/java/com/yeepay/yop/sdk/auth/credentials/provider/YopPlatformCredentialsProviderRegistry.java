/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.exception.YopClientException;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2/4/21 5:28 PM
 */
public class YopPlatformCredentialsProviderRegistry {
    private static final YopPlatformCredentialsProvider DEFAULT_PROVIDER = new YopFilePlatformCredentialsProvider();

    private static volatile YopPlatformCredentialsProvider CUSTOM_PROVIDER = null;

    public static void registerProvider(YopPlatformCredentialsProvider yopCredentialsProvider) {
        if (yopCredentialsProvider == null) {
            throw new YopClientException("customProvider can't be null.");
        }
        CUSTOM_PROVIDER = yopCredentialsProvider;
    }

    public static YopPlatformCredentialsProvider getProvider() {
        return CUSTOM_PROVIDER == null ? DEFAULT_PROVIDER : CUSTOM_PROVIDER;
    }
}
