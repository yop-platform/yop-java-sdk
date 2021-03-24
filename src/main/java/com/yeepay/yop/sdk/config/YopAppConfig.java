package com.yeepay.yop.sdk.config;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfig;
import com.yeepay.yop.sdk.config.provider.file.support.YopCertConfigUtils;
import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.io.Serializable;
import java.util.Map;

/**
 * title: 应用SDKConfig<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/2/8 15:32
 */
public class YopAppConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    private String appKey;

    private String aesSecretKey;

    private String encryptKey;

    private Map<CertTypeEnum, String> isvPrivateKeys;

    private YopCertConfig[] isvEncryptKey;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public YopAppConfig withAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public String getAesSecretKey() {
        return aesSecretKey;
    }

    public void setAesSecretKey(String aesSecretKey) {
        this.aesSecretKey = aesSecretKey;
    }

    public YopAppConfig withAesSecretKey(String aesSecretKey) {
        this.aesSecretKey = aesSecretKey;
        return this;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public YopAppConfig withEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
        return this;
    }

    public void storeIsvPrivateKey(YopCertConfig[] isvPrivateKeys) {
        this.isvPrivateKeys = Maps.newHashMap();
        for (int i = 0; i < isvPrivateKeys.length; i++) {
            this.isvPrivateKeys.put(isvPrivateKeys[i].getCertType(), YopCertConfigUtils.loadPrivateKey(isvPrivateKeys[i]));
        }
    }

    public Map<CertTypeEnum, String> getIsvPrivateKeys() {
        return this.isvPrivateKeys;
    }

    public String loadPrivateKey(CertTypeEnum certType) {
        return this.isvPrivateKeys.get(certType);
    }

    public YopCertConfig[] getIsvEncryptKey() {
        return isvEncryptKey;
    }

    public void setIsvEncryptKey(YopCertConfig[] isvEncryptKey) {
        this.isvEncryptKey = isvEncryptKey;
    }

    public YopAppConfig withIsvEncryptKey(YopCertConfig[] isvEncryptKey) {
        this.isvEncryptKey = isvEncryptKey;
        return this;
    }

    public static final class Builder {

        private YopFileSdkConfig yopFileSdkConfig;

        public Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withSDKConfig(YopFileSdkConfig yopFileSdkConfig) {
            this.yopFileSdkConfig = yopFileSdkConfig;
            return this;
        }

        public YopAppConfig build() {
            YopAppConfig yopAppConfig = new YopAppConfig()
                    .withAppKey(yopFileSdkConfig.getAppKey())
                    .withEncryptKey(yopFileSdkConfig.getEncryptKey())
                    .withIsvEncryptKey(yopFileSdkConfig.getIsvEncryptKey());
            if (yopFileSdkConfig.getIsvPrivateKey() != null && yopFileSdkConfig.getIsvPrivateKey().length >= 1) {
                yopAppConfig.storeIsvPrivateKey(yopFileSdkConfig.getIsvPrivateKey());
            }
            return yopAppConfig;
        }
    }
}
