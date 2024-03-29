/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.base.auth.credentials.provider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.base.config.YopAppConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_ENV;
import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_PROVIDER;
import static com.yeepay.yop.sdk.constants.CharacterConstants.COLON;

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

    private final Map<String, YopAppConfig> appConfigs = Maps.newConcurrentMap();
    private final Map<String, YopCredentials> yopCredentialsMap = Maps.newConcurrentMap();

    @Override
    public final YopCredentials<?> getCredentials(String provider, String env, String appKey, String credentialType) {
        String key = StringUtils.defaultString(provider, YOP_DEFAULT_PROVIDER) + COLON +
                StringUtils.defaultString(env, YOP_DEFAULT_ENV) + COLON + appKey + COLON + credentialType;
        return yopCredentialsMap.computeIfAbsent(key, k -> buildCredentials(getAppConfig(provider, env, appKey), credentialType));
    }

    @Override
    public List<CertTypeEnum> getSupportCertTypes(String appKey) {
        return getSupportCertTypes(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey);
    }

    @Override
    public List<CertTypeEnum> getSupportCertTypes(String provider, String env, String appKey) {
        return Lists.newArrayList(getAppConfig(provider, env, appKey).getIsvPrivateKeys().keySet());
    }

    private YopAppConfig getAppConfig(String provider, String env, String appKey) {
        final String theProvider = StringUtils.defaultString(provider, YOP_DEFAULT_PROVIDER);
        final String theEnv = StringUtils.defaultString(env, YOP_DEFAULT_ENV);
        final String theAppKey = useDefaultIfBlank(theProvider, theEnv, appKey);
        return appConfigs.computeIfAbsent(theAppKey, k -> loadAppConfig(theProvider, theEnv, theAppKey));
    }

    /**
     * 加载用户密钥配置
     *
     * @param appKey appKey
     * @return 用户密钥配置
     */
    protected abstract YopAppConfig loadAppConfig(String appKey);

    protected YopAppConfig loadAppConfig(String provider, String env, String appKey) {
        return loadAppConfig(appKey);
    }

    @Override
    public List<YopCertConfig> getIsvEncryptKey(String appKey) {
        return getIsvEncryptKey(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey);
    }

    @Override
    public List<YopCertConfig> getIsvEncryptKey(String provider, String env, String appKey) {
        return getAppConfig(provider, env, appKey).getIsvEncryptKey();
    }

}
