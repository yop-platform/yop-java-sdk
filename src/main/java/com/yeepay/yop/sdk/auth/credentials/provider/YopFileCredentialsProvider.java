/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.config.YopAppConfig;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfig;
import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfigProvider;

/**
 * title: 基于文件的凭证提供方<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2020/11/24 上午11:57
 */
public class YopFileCredentialsProvider extends YopFixedCredentialsProvider {

    @Override
    protected YopAppConfig loadAppConfig(String appKey) {
        YopFileSdkConfigProvider yopFileSdkConfigProvider = (YopFileSdkConfigProvider) (YopSdkConfigProviderRegistry.getProvider());
        YopFileSdkConfig yopFileSdkConfig = yopFileSdkConfigProvider.loadSdkConfig(appKey);

        return YopAppConfig.Builder.builder()
                .withSDKConfig(yopFileSdkConfig)
                .build();
    }

}
