package com.yeepay.yop.sdk.http;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.base.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.client.ClientConfiguration;
import com.yeepay.yop.sdk.client.support.ClientConfigurationSupport;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import com.yeepay.yop.sdk.exception.YopClientBizException;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.ServiceLoader;

import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_ENV;
import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_PROVIDER;
import static com.yeepay.yop.sdk.constants.CharacterConstants.COLON;
import static com.yeepay.yop.sdk.constants.ExceptionConstants.SDK_CONFIG_RUNTIME_DEPENDENCY;

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

    private static final Map<String, YopHttpClient> DEFAULT_CLIENT_MAP = Maps.newConcurrentMap();
    private static final Map<String, YopHttpClientProvider> httpClientProviderMap;

    static {
        httpClientProviderMap = Maps.newHashMap();
        ServiceLoader<YopHttpClientProvider> serviceLoader = ServiceLoader.load(YopHttpClientProvider.class);
        for (YopHttpClientProvider yopHttpClientProvider : serviceLoader) {
            httpClientProviderMap.put(yopHttpClientProvider.name(), yopHttpClientProvider);
        }
        if (httpClientProviderMap.isEmpty()) {
            throw new YopClientBizException(SDK_CONFIG_RUNTIME_DEPENDENCY, "SetUpProblem，YopHttpClientProvider NotFound!");
        }
    }

    @Deprecated
    public static YopHttpClient getDefaultClient() {
        return getDefaultClient(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, null);
    }

    public static YopHttpClient getDefaultClient(String provider, String env, YopSdkConfigProvider yopSdkConfigProvider) {
        final String clientKey = StringUtils.defaultString(provider, YOP_DEFAULT_PROVIDER)
                + COLON + StringUtils.defaultString(env, YOP_DEFAULT_ENV);
        return DEFAULT_CLIENT_MAP.computeIfAbsent(clientKey, p -> {
            final YopSdkConfigProvider sdkConfigProvider = null == yopSdkConfigProvider ?
                    YopSdkConfigProviderRegistry.getProvider() : yopSdkConfigProvider;
            ClientConfiguration clientConfiguration = ClientConfigurationSupport
                    .getClientConfiguration(sdkConfigProvider.getConfig(provider, env));
            return getClient(clientConfiguration);
        });
    }

    public static YopHttpClient getClient(ClientConfiguration clientConfig) {
        final YopHttpClientProvider yopHttpClientProvider = httpClientProviderMap.get(clientConfig.getClientImpl());
        if (null == yopHttpClientProvider) {
            throw new YopClientBizException(SDK_CONFIG_RUNTIME_DEPENDENCY, "SetUpProblem, YopHttpClientProvider NotFound, name:" + clientConfig.getClientImpl());
        }
        return yopHttpClientProvider.get(clientConfig);
    }

}
