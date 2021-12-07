package com.yeepay.yop.sdk.http;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.client.ClientConfiguration;
import com.yeepay.yop.sdk.client.support.ClientConfigurationSupport;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;

import java.util.Map;

import static com.yeepay.yop.sdk.YopConstants.YOP_HTTP_CLIENT_IMPL_APACHE;
import static com.yeepay.yop.sdk.YopConstants.YOP_HTTP_CLIENT_IMPL_OK;

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
    private static final YopHttpClientProvider defaultClientProvider = new YopOkHttpClientProvider();
    private static final Map<String, YopHttpClientProvider> httpClientProviderMap;

    static {
        httpClientProviderMap = Maps.newHashMap();
        httpClientProviderMap.put(YOP_HTTP_CLIENT_IMPL_OK, defaultClientProvider);
        httpClientProviderMap.put(YOP_HTTP_CLIENT_IMPL_APACHE, new YopApacheHttpClientProvider());
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
        final YopHttpClientProvider provider = httpClientProviderMap.get(clientConfig.getClientImpl());
        return null == provider ? defaultClientProvider.get(clientConfig) : provider.get(clientConfig);
    }

    public static void registerHttpClientProvider(String clientImpl, YopHttpClientProvider provider) {
        httpClientProviderMap.put(clientImpl, provider);
    }

    interface YopHttpClientProvider {
        YopHttpClient get(ClientConfiguration clientConfig);
    }

    static class YopOkHttpClientProvider implements YopHttpClientProvider {

        @Override
        public YopHttpClient get(ClientConfiguration clientConfig) {
            return new com.yeepay.yop.sdk.http.impl.ok.YopHttpClient(clientConfig);
        }
    }

    static class YopApacheHttpClientProvider implements YopHttpClientProvider {

        @Override
        public YopHttpClient get(ClientConfiguration clientConfig) {
            return new com.yeepay.yop.sdk.http.impl.apache.YopHttpClient(clientConfig);
        }
    }
}
