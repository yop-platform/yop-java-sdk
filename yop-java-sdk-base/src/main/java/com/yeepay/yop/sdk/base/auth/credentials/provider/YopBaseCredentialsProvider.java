/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.base.auth.credentials.provider;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.base.config.YopAppConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
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
    @Override
    public String getDefaultAppKey() {
        return YopConstants.YOP_DEFAULT_APPKEY;
    }

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected YopCredentials<?> buildCredentials(YopAppConfig appConfig, String credentialType) {
        CertTypeEnum certType;
        if (null == appConfig || StringUtils.isEmpty(credentialType) ||
                (null == (certType = CertTypeEnum.parse(credentialType)))) {
            throw new YopClientException("Illegal params when buildCredentials, credentialType:" + credentialType);
        }

        PrivateKey privateKey = appConfig.loadPrivateKey(certType);
        if (null == privateKey) {
            throw new YopClientException("No cert config found when buildCredentials, appKey:" + appConfig.getAppKey() + ", certType:" + certType);
        }

        PKICredentialsItem pkiCredentialsItem = new PKICredentialsItem(privateKey, certType);
        return new YopPKICredentials(appConfig.getAppKey(), pkiCredentialsItem);
    }

    protected String useDefaultIfBlank(String appKey) {
        return StringUtils.defaultIfBlank(appKey, getDefaultAppKey());
    }

}
