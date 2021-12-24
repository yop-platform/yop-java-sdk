/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.encryptor.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.config.YopAppConfig;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfig;
import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfigProvider;
import com.yeepay.yop.sdk.encryptor.auth.credentials.MockEncryptorCredentials;
import com.yeepay.yop.sdk.encryptor.auth.credentials.MockEncryptorCredentialsItem;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<String, YopAppConfig> appConfigs = new HashMap<>();
    private final Map<String, YopCredentials> yopCredentialsMap = new ConcurrentHashMap<>();
    private final Map<String, List<YopCredentials>> yopEncryptCredentialsMap = new ConcurrentHashMap<>();

    @Override
    public YopCredentials getCredentials(String appKey, String credentialType) {
        String key = appKey + ":" + credentialType;
        return yopCredentialsMap.computeIfAbsent(key, k -> buildCredentials(getAppConfig(appKey), credentialType));
    }

    protected final YopCredentials buildCredentials(YopAppConfig appConfig, String credentialType) {
        if (appConfig == null) {
            return null;
        }
        CertTypeEnum certType = CertTypeEnum.parse(credentialType);
        if (certType.isSymmetric()) {
            MockEncryptorCredentialsItem credentialsItem = new MockEncryptorCredentialsItem(appConfig.getAesSecretKey(), certType);
            return new MockEncryptorCredentials(appConfig.getAppKey(), credentialsItem);
        } else {
            MockEncryptorCredentialsItem credentialsItem = new MockEncryptorCredentialsItem(appConfig.loadPrivateKey(certType), certType);
            return new MockEncryptorCredentials(appConfig.getAppKey(), credentialsItem);
        }
    }

    @Override
    public List<YopCertConfig> getIsvEncryptKey(String appKey) {
        return getAppConfig(appKey).getIsvEncryptKey();
    }

    @Override
    public List<CertTypeEnum> getSupportCertTypes(String appKey) {
        return new ArrayList<>(getAppConfig(appKey).getIsvPrivateKeys().keySet());
    }

    @Override
    public void removeConfig(String key) {
        appConfigs.remove(key);
        yopCredentialsMap.clear();
        if (null != key) {
            yopEncryptCredentialsMap.remove(key);
        }
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
