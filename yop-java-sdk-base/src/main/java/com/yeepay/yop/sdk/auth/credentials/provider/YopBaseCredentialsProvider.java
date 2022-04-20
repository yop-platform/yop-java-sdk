/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
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

    protected YopCredentials<?> buildCredentials(YopAppConfig appConfig, String credentialType) {
        CertTypeEnum certType;
        if (null == appConfig || StringUtils.isEmpty(credentialType) ||
                (null == (certType = CertTypeEnum.parse(credentialType)))) {
            throw new YopClientException("Illegal params when buildCredentials, credentialType:" + credentialType);
        }

        String privateKeyStr = appConfig.loadPrivateKey(certType);
        if (StringUtils.isEmpty(privateKeyStr)) {
            throw new YopClientException("No cert config found when buildCredentials, certType:" + certType);
        }

        PrivateKey privateKey;
        switch (certType) {
            case RSA2048:
                privateKey = RSAKeyUtils.string2PrivateKey(privateKeyStr);
                break;
            case SM2:
                privateKey = Sm2Utils.string2PrivateKey(privateKeyStr);
                break;
            default:
                throw new YopClientException("CertType is illegal for YopCredentials, certType:" + certType);
        }

        PKICredentialsItem pkiCredentialsItem = new PKICredentialsItem(privateKey, null, certType);
        return new YopPKICredentials(appConfig.getAppKey(), pkiCredentialsItem);
    }

    protected String useDefaultIfBlank(String appKey) {
        return StringUtils.defaultIfBlank(appKey, getDefaultAppKey());
    }

}
