/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopAESCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.config.YopAppConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;
import com.yeepay.yop.sdk.utils.Sm2Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;

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
            PKICredentialsItem pkiCredentialsItem = new PKICredentialsItem(string2PrivateKey(appConfig.loadPrivateKey(certType), certType), null, certType);
            return new YopPKICredentials(appConfig.getAppKey(), pkiCredentialsItem);
        }
    }

    private PrivateKey string2PrivateKey(String privateKey, CertTypeEnum certType) {
        switch (certType) {
            case RSA2048:
                return RSAKeyUtils.string2PrivateKey(privateKey);
            case SM2:
                return Sm2Utils.string2PrivateKey(privateKey);
        }
        throw new YopClientException("unsupported certType");
    }

    protected String useDefaultIfBlank(String appKey) {
        return StringUtils.defaultIfBlank(appKey, getDefaultAppKey());
    }

}
