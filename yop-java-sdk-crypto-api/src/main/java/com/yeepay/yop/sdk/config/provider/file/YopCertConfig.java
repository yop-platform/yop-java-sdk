package com.yeepay.yop.sdk.config.provider.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/12/26 下午3:50
 */
public final class YopCertConfig implements Serializable {

    private static final long serialVersionUID = -6377916283927611130L;

    @JsonProperty("app_key")
    private String appKey;

    @JsonProperty("store_type")
    private CertStoreType storeType;

    @JsonProperty("cert_type")
    private CertTypeEnum certType;

    private String password;

    private String value;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public YopCertConfig withAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public CertStoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(CertStoreType storeType) {
        this.storeType = storeType;
    }

    public YopCertConfig withStoreType(CertStoreType storeType) {
        this.storeType = storeType;
        return this;
    }

    public CertTypeEnum getCertType() {
        return certType;
    }

    public void setCertType(CertTypeEnum certType) {
        this.certType = certType;
    }

    public YopCertConfig withCertType(CertTypeEnum certType) {
        this.certType = certType;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public YopCertConfig withPassword(String password) {
        this.password = password;
        return this;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public YopCertConfig withValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
