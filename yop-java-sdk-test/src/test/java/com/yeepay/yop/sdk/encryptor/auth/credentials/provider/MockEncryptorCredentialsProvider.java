/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.encryptor.auth.credentials.provider;

import com.yeepay.yop.sdk.encryptor.auth.credentials.MockEncryptorCredentials;
import com.yeepay.yop.sdk.encryptor.auth.credentials.MockEncryptorCredentialsItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.base.config.YopAppConfig;
import com.yeepay.yop.sdk.base.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfig;
import com.yeepay.yop.sdk.base.config.provider.file.YopFileSdkConfigProvider;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.Encodes;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author qi.zhang-4
 * @version 1.0.0
 * @since 2021/12/23 10:49 上午
 */
public class MockEncryptorCredentialsProvider implements YopCredentialsProvider {

    private final Map<String, YopAppConfig> appConfigs = Maps.newHashMap();
    private final Map<String, YopCredentials> yopCredentialsMap = Maps.newConcurrentMap();

    @Override
    public YopCredentials<?> getCredentials(String appKey, String credentialType) {
        String key = appKey + ":" + credentialType;
        return yopCredentialsMap.computeIfAbsent(key, k -> buildCredentials(getAppConfig(appKey), credentialType));
    }

    protected final YopCredentials<?> buildCredentials(YopAppConfig appConfig, String credentialType) {
        CertTypeEnum certType;
        if (null == appConfig || StringUtils.isEmpty(credentialType) ||
                (null == (certType = CertTypeEnum.parse(credentialType)))) {
            throw new YopClientException("ConfigProblem, IsvPrivateCert NotFound, certType:" + credentialType + ", config:" + appConfig);
        }
        switch (certType) {
            case RSA2048:
            case SM2:
                MockEncryptorCredentialsItem credentialsItem = new MockEncryptorCredentialsItem(Encodes.encodeKey(appConfig.loadPrivateKey(certType)), certType);
                return new MockEncryptorCredentials(appConfig.getAppKey(), credentialsItem);
            default:
                throw new YopClientException("ConfigProblem, IsvPrivateCert Type NotSupport, certType:" + certType + ", config:" + appConfig);
        }
    }

    @Override
    public List<YopCertConfig> getIsvEncryptKey(String appKey) {
        return getAppConfig(appKey).getIsvEncryptKey();
    }

    @Override
    public List<CertTypeEnum> getSupportCertTypes(String appKey) {
        return Lists.newArrayList(getAppConfig(appKey).getIsvPrivateKeys().keySet());
    }

    @Override
    public String getDefaultAppKey() {
        return YopConstants.YOP_DEFAULT_APPKEY;
    }

    private YopAppConfig getAppConfig(String appId) {
        if (!appConfigs.containsKey(appId)) {
            appConfigs.computeIfAbsent(appId, k -> loadAppConfig(appId));
        }
        return appConfigs.get(appId);
    }

    protected YopAppConfig loadAppConfig(String appKey) {
        YopFileSdkConfigProvider yopFileSdkConfigProvider = (YopFileSdkConfigProvider) (YopSdkConfigProviderRegistry.getProvider());
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

        YopFileSdkConfig yopFileSdkConfig = yopFileSdkConfigProvider.loadSdkConfig(appKey);
        return YopAppConfig.Builder.builder()
                .withAppKey(appKey)
                .withIsvPrivateKeys(isvPrivateKeys)
                .withIsvEncryptKeys(isvEncryptKeys)
                .withSDKConfig(yopFileSdkConfig)
                .build();
    }

}
