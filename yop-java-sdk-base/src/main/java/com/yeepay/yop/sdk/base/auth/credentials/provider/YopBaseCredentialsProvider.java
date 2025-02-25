/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.base.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.base.config.YopAppConfig;
import com.yeepay.yop.sdk.exception.config.IllegalConfigFormatException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;

import static com.yeepay.yop.sdk.YopConstants.*;

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
        if (null == appConfig) {
            throw new IllegalConfigFormatException("YopAppConfig", "YopAppConfig is required");
        }
        if (StringUtils.isEmpty(credentialType) ||
                (null == (certType = CertTypeEnum.parse(credentialType)))) {
            throw new IllegalConfigFormatException("credentialType", "illegal value: " + credentialType);
        }

        PrivateKey privateKey = appConfig.loadPrivateKey(certType);
        if (null == privateKey) {
            throw new IllegalConfigFormatException("isv_private_key", "appKey:" + appConfig.getAppKey() + ", certType:" + certType + "not found");
        }

        PKICredentialsItem pkiCredentialsItem = new PKICredentialsItem(privateKey, certType);
        return new YopPKICredentials(appConfig.getAppKey(), pkiCredentialsItem);
    }

    protected String useDefaultIfBlank(String appKey) {
        return StringUtils.defaultIfBlank(appKey, getDefaultAppKey());
    }

    protected String useDefaultIfBlank(String provider, String env, String appKey) {
        return StringUtils.defaultIfBlank(appKey, getDefaultAppKey(provider, env));
    }

    @Override
    public String getDefaultAppKey() {
        return getDefaultAppKey(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV);
    }

    @Override
    public String getDefaultAppKey(String provider, String env) {
        return YOP_DEFAULT_APPKEY;
    }

    @Override
    public YopCredentials<?> getCredentials(String appKey, String credentialType) {
        return getCredentials(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, credentialType);
    }
}
