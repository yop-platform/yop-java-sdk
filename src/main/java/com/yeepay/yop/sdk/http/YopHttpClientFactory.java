package com.yeepay.yop.sdk.http;

import com.yeepay.yop.sdk.client.ClientConfiguration;
import com.yeepay.yop.sdk.client.support.ClientConfigurationSupport;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;

/**
 * title: YopHttpClient工厂<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/8/6 16:26
 */
public class YopHttpClientFactory {

    private static volatile YopHttpClient defaultClient;

    public static YopHttpClient getDefaultClient() {
        if (defaultClient == null) {
            synchronized (YopHttpClientFactory.class) {
                if (defaultClient == null) {
                    YopSdkConfigProvider yopSdkConfigProvider = YopSdkConfigProviderRegistry.getProvider();
                    ClientConfiguration clientConfiguration = ClientConfigurationSupport.getClientConfiguration(yopSdkConfigProvider.getConfig());
                    defaultClient = getClient(clientConfiguration);
                }
            }
        }
        return defaultClient;
    }

    public static YopHttpClient getClient(ClientConfiguration clientConfiguration) {
        return new YopHttpClient(clientConfiguration);
    }
}
