/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.base.auth.credentials.provider.file;

import com.yeepay.yop.sdk.base.auth.credentials.provider.YopFixedCredentialsProvider;
import com.yeepay.yop.sdk.base.config.YopAppConfig;
import com.yeepay.yop.sdk.base.config.provider.file.YopFileSdkConfigProvider;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfig;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_ENV;
import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_PROVIDER;

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

    private static final YopFileSdkConfigProvider yopFileSdkConfigProvider = YopFileSdkConfigProvider.instance();

    @Override
    protected YopAppConfig loadAppConfig(String appKey) {
        return loadAppConfig(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey);
    }

    @Override
    protected YopAppConfig loadAppConfig(String provider, String env, String appKey) {
        List<YopCertConfig> isvPrivateKeys = null;
        List<YopCertConfig> isvEncryptKeys = null;
        for (Map.Entry<String, YopFileSdkConfig> entry : yopFileSdkConfigProvider.getSdkConfigs().entrySet()) {
            if (CollectionUtils.isEmpty(isvPrivateKeys)) {
                List<YopCertConfig> isvPrivateKeys0 = entry.getValue().getIsvPrivateKey(appKey);
                if (CollectionUtils.isNotEmpty(isvPrivateKeys0)) {
                    isvPrivateKeys = isvPrivateKeys0;
                }
            }

            if (CollectionUtils.isEmpty(isvEncryptKeys)) {
                List<YopCertConfig> isvEncryptKeys0 = entry.getValue().getIsvEncryptKey(appKey);
                if (CollectionUtils.isNotEmpty(isvEncryptKeys0)) {
                    isvEncryptKeys = isvEncryptKeys0;
                }
            }

            if (CollectionUtils.isNotEmpty(isvPrivateKeys) || CollectionUtils.isNotEmpty(isvEncryptKeys)) {
                break;
            }
        }

        YopFileSdkConfig yopFileSdkConfig = yopFileSdkConfigProvider.loadSdkConfig(provider, env, appKey);
        return YopAppConfig.Builder.builder()
                .withAppKey(appKey)
                .withIsvPrivateKeys(isvPrivateKeys)
                .withIsvEncryptKeys(isvEncryptKeys)
                .withSDKConfig(yopFileSdkConfig)
                .build();
    }

}
