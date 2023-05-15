package com.yeepay.yop.sdk.client.support;


import com.yeepay.g3.core.yop.sdk.sample.YopConstants;
import com.yeepay.g3.core.yop.sdk.sample.auth.support.YopCredentialsProviderSupport;
import com.yeepay.g3.core.yop.sdk.sample.client.ClientParams;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfig;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfigProvider;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfigProviderRegistry;
import com.yeepay.g3.core.yop.sdk.sample.config.support.BackUpAppSdkConfigManager;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/4/3 15:10
 */
public final class ClientParamsSupport {

    private static final ClientParams DEFAULT_CLIENT_PARAMS;

    static {
        AppSdkConfigProvider appSdkConfigProvider = AppSdkConfigProviderRegistry.getProvider();
        ClientParams.Builder builder = ClientParams.Builder.aClientParams()
                .withCredentialsProvider(YopCredentialsProviderSupport.getCredentialsProvider(appSdkConfigProvider));

        AppSdkConfig config = ObjectUtils.defaultIfNull(appSdkConfigProvider.getDefaultConfig(),
                BackUpAppSdkConfigManager.getBackUpConfig());
        DEFAULT_CLIENT_PARAMS = builder.withClientConfiguration(ClientConfigurationSupport.getClientConfiguration(config))
                .withEndPoint(URI.create(StringUtils.defaultIfBlank(config.getServerRoot(), YopConstants.DEFAULT_SERVER_ROOT)))
                .withYosEndPoint(URI.create(StringUtils.defaultIfBlank(config.getYosServerRoot(), YopConstants.DEFAULT_YOS_SERVER_ROOT)))
                .withSandboxEndPoint(URI.create(StringUtils.defaultIfBlank(config.getSandboxServerRoot(), YopConstants.DEFAULT_SANDBOX_SERVER_ROOT)))
                .build();
    }

    public static ClientParams getDefaultClientParams() {
        return DEFAULT_CLIENT_PARAMS;
    }

}
