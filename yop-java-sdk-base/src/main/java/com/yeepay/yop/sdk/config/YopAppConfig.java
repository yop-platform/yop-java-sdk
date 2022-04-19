package com.yeepay.yop.sdk.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfig;
import com.yeepay.yop.sdk.config.provider.file.support.YopCertConfigUtils;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
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

    private Map<CertTypeEnum, String> isvPrivateKeys = Maps.newHashMap();

    private List<YopCertConfig> isvEncryptKeys = Lists.newLinkedList();

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAesSecretKey() {
        return aesSecretKey;
    }

    public void setAesSecretKey(String aesSecretKey) {
        this.aesSecretKey = aesSecretKey;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public void setIsvPrivateKey(List<YopCertConfig> isvPrivateKeys) {
        if(CollectionUtils.isEmpty(isvPrivateKeys)){
            return;
        }

        for (YopCertConfig isvPrivateKey : isvPrivateKeys) {
            this.isvPrivateKeys.put(isvPrivateKey.getCertType(), YopCertConfigUtils.loadPrivateKey(isvPrivateKey));
        }
    }

    public Map<CertTypeEnum, String> getIsvPrivateKeys() {
        return this.isvPrivateKeys;
    }

    public String loadPrivateKey(CertTypeEnum certType) {
        return this.isvPrivateKeys.get(certType);
    }

    public List<YopCertConfig> getIsvEncryptKey() {
        return isvEncryptKeys;
    }

    public void setIsvEncryptKeyList(List<YopCertConfig> isvEncryptKeyList) {
        this.isvEncryptKeys = isvEncryptKeyList;
    }

    public static final class Builder {

        private String appKey;

        private List<YopCertConfig> isvPrivateKeys;

        private List<YopCertConfig> isvEncryptKeys;

        private YopFileSdkConfig yopFileSdkConfig;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withAppKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public Builder withIsvPrivateKeys(List<YopCertConfig> isvPrivateKeys) {
            this.isvPrivateKeys = isvPrivateKeys;
            return this;
        }

        public Builder withIsvEncryptKeys(List<YopCertConfig> isvEncryptKeys) {
            this.isvEncryptKeys = isvEncryptKeys;
            return this;
        }

        public Builder withSDKConfig(YopFileSdkConfig yopFileSdkConfig) {
            this.yopFileSdkConfig = yopFileSdkConfig;
            return this;
        }

        public YopAppConfig build() {
            if (null == appKey || StringUtils.equals(appKey, YopConstants.YOP_DEFAULT_APPKEY)) {
                appKey = yopFileSdkConfig.getAppKey();
            }
            YopAppConfig yopAppConfig = new YopAppConfig();
            yopAppConfig.setAppKey(appKey);
            yopAppConfig.setEncryptKey(yopFileSdkConfig.getEncryptKey());
            if (CollectionUtils.isEmpty(isvPrivateKeys)) {
                isvPrivateKeys = yopFileSdkConfig.getIsvPrivateKey(appKey);
            }
            yopAppConfig.setIsvPrivateKey(isvPrivateKeys);
            if (CollectionUtils.isEmpty(isvEncryptKeys)) {
                isvEncryptKeys = yopFileSdkConfig.getIsvEncryptKey(appKey);
            }
            yopAppConfig.setIsvEncryptKeyList(isvEncryptKeys);
            return yopAppConfig;
        }
    }
}
