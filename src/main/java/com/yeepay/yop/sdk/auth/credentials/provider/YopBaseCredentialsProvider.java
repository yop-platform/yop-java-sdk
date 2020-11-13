/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.YopAESCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopRSACredentials;
import com.yeepay.yop.sdk.config.YopAppConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.interfaces.RSAPrivateKey;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2020/11/24 上午11:57
 */
public abstract class YopBaseCredentialsProvider implements YopCredentialsProvider {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final YopCredentials buildCredentials(YopAppConfig appConfig, String credentialType) {
        if (appConfig == null) {
            return null;
        }
        CertTypeEnum certType = CertTypeEnum.parse(credentialType);
        if (certType.isSymmetric()) {
            return new YopAESCredentials(appConfig.getAppKey(), appConfig.getAesSecretKey());
        } else {
            return new YopRSACredentials(appConfig.getAppKey(),
                    (RSAPrivateKey) appConfig.loadPrivateKey(certType),
                    appConfig.getEncryptKey());
        }
    }

}
