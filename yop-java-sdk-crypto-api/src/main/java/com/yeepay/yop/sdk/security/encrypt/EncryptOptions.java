/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security.encrypt;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collections;
import java.util.Map;

/**
 * title: 加解密参数配置<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/11
 */
public class EncryptOptions {

    /**
     * 加密凭证
     */
    private Object credentials;

    /**
     * 加密凭证：密文，base64编码格式-用于传输
     */
    private String encryptedCredentials;

    /**
     * 加密凭证加密算法：基于sdk-网关的约定，目前为sm2
     */
    private String credentialsAlg;

    /**
     * 加密算法：{密钥类型}/{分组模式}/{填充算法}
     */
    private String alg;

    /**
     * 加密初始向量
     */
    private String iv;

    /**
     * 加密附加信息
     */
    private String aad;

    /**
     * 大参数加密模式(分块／流式)，默认流式
     */
    private BigParamEncryptMode bigParamEncryptMode = BigParamEncryptMode.stream;

    /**
     * 其他增强信息
     */
    private Map<String, Object> enhancerInfo = Maps.newHashMap();

    public EncryptOptions() {
    }

    public EncryptOptions(Object credentials) {
        this(credentials, BigParamEncryptMode.stream);
    }

    public EncryptOptions(Object credentials, String alg) {
        this.credentials = credentials;
        this.alg = alg;
    }

    public EncryptOptions(Object credentials, BigParamEncryptMode bigParamEncryptMode) {
        this.credentials = credentials;
        this.bigParamEncryptMode = bigParamEncryptMode;
    }

    public EncryptOptions(Object credentials, String alg, String iv, String aad) {
        this(credentials);
        this.alg = alg;
        this.iv = iv;
        this.aad = aad;
    }

    public EncryptOptions(Object credentials, String credentialsAlg, String alg, String iv, String aad) {
        this(credentials, alg, iv, aad);
        this.credentialsAlg = credentialsAlg;
    }

    public EncryptOptions(Object credentials, String credentialsAlg, String alg,
                          String iv, String aad, BigParamEncryptMode bigParamEncryptMode) {
        this(credentials, credentialsAlg, alg, iv, aad);
        this.bigParamEncryptMode = bigParamEncryptMode;
    }

    public Object getCredentials() {
        return credentials;
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }

    public String getEncryptedCredentials() {
        return encryptedCredentials;
    }

    public void setEncryptedCredentials(String encryptedCredentials) {
        this.encryptedCredentials = encryptedCredentials;
    }

    public String getCredentialsAlg() {
        return credentialsAlg;
    }

    public void setCredentialsAlg(String credentialsAlg) {
        this.credentialsAlg = credentialsAlg;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getAad() {
        return aad;
    }

    public void setAad(String aad) {
        this.aad = aad;
    }

    public BigParamEncryptMode getBigParamEncryptMode() {
        return bigParamEncryptMode;
    }

    public void setBigParamEncryptMode(BigParamEncryptMode bigParamEncryptMode) {
        this.bigParamEncryptMode = bigParamEncryptMode;
    }

    public Map<String, Object> getEnhancerInfo() {
        return Collections.unmodifiableMap(enhancerInfo);
    }

    public void enhance(String key, Object value) {
        enhancerInfo.put(key, value);
    }

    public EncryptOptions copy() {
        EncryptOptions copy = new EncryptOptions();
        copy.credentials = credentials;
        copy.encryptedCredentials = encryptedCredentials;
        copy.credentialsAlg = credentialsAlg;
        copy.alg = alg;
        copy.iv = iv;
        copy.aad = aad;
        copy.bigParamEncryptMode = bigParamEncryptMode;
        return copy;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
