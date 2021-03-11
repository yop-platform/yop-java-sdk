/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.config.YopAppConfig;
import com.yeepay.yop.sdk.config.provider.YopFixedSdkConfigProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * title: 固定的应用sdk配置提供方基类<br>
 * description: 一旦加载不允许修改，另当缓存量大时请使用 YopCachedCredentialsProvider<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2020/11/24 上午11:57
 */
public abstract class YopFixedCredentialsProvider extends YopBaseCredentialsProvider {

    private YopAppConfig appConfig = null;

    private final Map<String, YopCredentials> yopCredentialsMap = new ConcurrentHashMap<>();

    @Override
    public final YopCredentials getCredentials(String appKey, String credentialType) {
        if (null == appConfig) {
            synchronized (YopFixedSdkConfigProvider.class) {
                if (null == appConfig) {
                    // TODO 这里可以异步初始化
                    appConfig = loadAppConfig(appKey);
                }
            }
        }

        String key = appKey + ":" + credentialType;
        YopCredentials found = yopCredentialsMap.get(key);
        if (null == found) {
            yopCredentialsMap.put(key, buildCredentials(appConfig, credentialType));
        }
        return yopCredentialsMap.get(key);
    }

    /**
     * 加载用户密钥配置RSAKeyUtils
     *
     * @param appKey appKey
     * @return 用户密钥配置
     */
    protected abstract YopAppConfig loadAppConfig(String appKey);

}
