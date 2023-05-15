package com.yeepay.yop.sdk.auth.support;


import com.yeepay.g3.core.yop.sdk.sample.auth.YopCredentials;
import com.yeepay.g3.core.yop.sdk.sample.auth.YopCredentialsProvider;
import com.yeepay.g3.core.yop.sdk.sample.auth.credentials.YopAESCredentials;
import com.yeepay.g3.core.yop.sdk.sample.auth.credentials.YopRSACredentials;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfig;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfigProvider;
import com.yeepay.g3.core.yop.sdk.sample.security.CertTypeEnum;
import org.apache.commons.lang3.ObjectUtils;

import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/4/3 15:42
 */
public class YopCredentialsProviderSupport {

    public static YopCredentialsProvider getCredentialsProvider(AppSdkConfigProvider appSdkConfigProvider) {
        return new DefaultYopCredentialsProvider(appSdkConfigProvider);
    }

    private static class DefaultYopCredentialsProvider implements YopCredentialsProvider {

        private final AppSdkConfigProvider appSdkConfigProvider;

        DefaultYopCredentialsProvider(AppSdkConfigProvider appSdkConfigProvider) {
            this.appSdkConfigProvider = appSdkConfigProvider;
        }

        @Override
        public YopCredentials getCredentials(String appKey, String credentialType) {
            return getCredentials(appSdkConfigProvider.getConfig(appKey), credentialType);
        }

        @Override
        public YopCredentials getDefaultAppCredentials(String credentialType) {
            return getCredentials(appSdkConfigProvider.getDefaultConfig(), credentialType);
        }

        @Override
        public PublicKey getYopPublicKey(String credentialType) {
            CertTypeEnum certType = CertTypeEnum.parse(credentialType.toUpperCase());
            certType = ObjectUtils.defaultIfNull(certType, CertTypeEnum.RSA2048);
            return appSdkConfigProvider.getDefaultConfig().loadYopPublicKey(certType);
        }

        private YopCredentials getCredentials(AppSdkConfig appSdkConfig, String credentialType) {
            if (appSdkConfig == null) {
                return null;
            }
            CertTypeEnum certType = CertTypeEnum.parse(credentialType);
            if (certType.isSymmetric()) {
                return new YopAESCredentials(appSdkConfig.getAppKey(), appSdkConfig.getAesSecretKey());
            } else {
                return new YopRSACredentials(appSdkConfig.getAppKey(), (RSAPrivateKey) appSdkConfig.loadPrivateKey(certType), appSdkConfig.getEncryptKey());
            }
        }
    }

}
