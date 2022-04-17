/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.config.YopAppConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.util.ArrayList;
import java.util.List;
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

    private final Map<String, YopAppConfig> appConfigs = new ConcurrentHashMap();
    private final Map<String, YopCredentials> yopCredentialsMap = new ConcurrentHashMap();

    @Override
    public final YopCredentials getCredentials(String appKey, String credentialType) {
        String key = appKey + ":" + credentialType;
        if (!yopCredentialsMap.containsKey(key)) {
            yopCredentialsMap.put(key, buildCredentials(getAppConfig(appKey), credentialType));
        }
        return yopCredentialsMap.get(key);
    }

    @Override
    public List<CertTypeEnum> getSupportCertTypes(String appKey) {
        return new ArrayList(getAppConfig(appKey).getIsvPrivateKeys().keySet());
    }

    private YopAppConfig getAppConfig(String appKey) {
        String appKeyHandled = useDefaultIfBlank(appKey);
        if (!appConfigs.containsKey(appKeyHandled)) {
            appConfigs.put(appKeyHandled, loadAppConfig(appKeyHandled));
        }
        return appConfigs.get(appKeyHandled);
    }

    /**
     * 加载用户密钥配置
     *
     * @param appKey appKey
     * @return 用户密钥配置
     */
    protected abstract YopAppConfig loadAppConfig(String appKey);

    @Override
    public List<YopCertConfig> getIsvEncryptKey(String appKey) {
        return getAppConfig(appKey).getIsvEncryptKey();
    }

    @Override
    public void removeConfig(String key) {
        appConfigs.remove(key);
        yopCredentialsMap.clear();
    }

}
