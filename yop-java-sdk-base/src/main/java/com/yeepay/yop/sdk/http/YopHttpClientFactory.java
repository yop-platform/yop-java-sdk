package com.yeepay.yop.sdk.http;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.client.ClientConfiguration;
import com.yeepay.yop.sdk.client.support.ClientConfigurationSupport;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import com.yeepay.yop.sdk.base.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.exception.YopClientException;

import java.util.Map;
import java.util.ServiceLoader;

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
    private static final Map<String, YopHttpClientProvider> httpClientProviderMap;

    static {
        httpClientProviderMap = Maps.newHashMap();
        ServiceLoader<YopHttpClientProvider> serviceLoader = ServiceLoader.load(YopHttpClientProvider.class);
        for (YopHttpClientProvider yopHttpClientProvider : serviceLoader) {
            httpClientProviderMap.put(yopHttpClientProvider.name(), yopHttpClientProvider);
        }
        if (httpClientProviderMap.isEmpty()) {
            throw new YopClientException("No YopHttpClientProvider found!");
        }
    }

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

    public static YopHttpClient getClient(ClientConfiguration clientConfig) {
        final YopHttpClientProvider yopHttpClientProvider = httpClientProviderMap.get(clientConfig.getClientImpl());
        if (null == yopHttpClientProvider) {
            throw new YopClientException("YopHttpClientProvider Not Found, name:" + clientConfig.getClientImpl());
        }
        return yopHttpClientProvider.get(clientConfig);
    }

}
