package com.yeepay.yop.sdk.model;

import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Set;

import static com.yeepay.yop.sdk.YopConstants.SM4_CBC_PKCS5PADDING;

/**
 * title: Generic representation of request level configuration<br>
 * description: 用于控制请求的处理细节(包括是否加密、是否验签、走哪种认证策略，连接和读取超时等等)
 * <p>
 * The customer interface for specifying
 * request level configuration is a base request class with configuration methods.
 * </p>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/14 17:24
 */
public class YopRequestConfig {

    private String appKey;

    private String securityReq;

    private YopCredentials<?> credentials;

    /**
     * 获取数据的超时时间, 单位：ms
     */
    private int readTimeout;

    /**
     * 建立连接的超时, 单位：ms
     */
    private int connectTimeout;

    /**
     * 是否对请求参数加密
     */
    private Boolean needEncrypt;

    /**
     * 是否对所有参数都加密
     */
    private Boolean totalEncrypt;

    /**
     * 加密算法
     */
    private String encryptAlg = SM4_CBC_PKCS5PADDING;

    /**
     * 待加密请求头
     */
    private final Set<String> encryptHeaders = Sets.newHashSet();

    /**
     * 待加密请求参数
     */
    private final Set<String> encryptParams = Sets.newHashSet();

    /**
     * 是否对响应结果验证签名
     */
    private Boolean skipVerifySign;

    /**
     * 指定签名有效时间
     */
    private Integer signExpirationInSeconds;

    public String getAppKey() {
        return appKey;
    }

    public YopRequestConfig setAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public String getSecurityReq() {
        return securityReq;
    }

    public YopRequestConfig setSecurityReq(String securityReq) {
        this.securityReq = securityReq;
        return this;
    }

    public YopCredentials<?> getCredentials() {
        return credentials;
    }

    public YopRequestConfig setCredentials(YopCredentials<?> credentials) {
        this.credentials = credentials;
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public YopRequestConfig setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public YopRequestConfig setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public Boolean getNeedEncrypt() {
        return needEncrypt;
    }

    public YopRequestConfig setNeedEncrypt(Boolean needEncrypt) {
        this.needEncrypt = needEncrypt;
        return this;
    }

    public Boolean getTotalEncrypt() {
        return totalEncrypt;
    }

    public YopRequestConfig setTotalEncrypt(Boolean totalEncrypt) {
        if (null != totalEncrypt) {
            this.totalEncrypt = totalEncrypt;
        }
        return this;
    }

    public String getEncryptAlg() {
        return encryptAlg;
    }

    public YopRequestConfig setEncryptAlg(String encryptAlg) {
        if (StringUtils.isNotBlank(encryptAlg)) {
            this.encryptAlg = encryptAlg;
        }
        return this;
    }

    public Set<String> getEncryptHeaders() {
        return Collections.unmodifiableSet(encryptHeaders);
    }

    public Set<String> getEncryptParams() {
        return Collections.unmodifiableSet(encryptParams);
    }

    public Boolean getSkipVerifySign() {
        return skipVerifySign;
    }

    public YopRequestConfig setSkipVerifySign(Boolean skipVerifySign) {
        this.skipVerifySign = skipVerifySign;
        return this;
    }

    public YopRequestConfig addEncryptParam(String name) {
        encryptParams.add(name);
        needEncrypt = true;
        return this;
    }
    public YopRequestConfig addEncryptParams(Set<String> params) {
        encryptParams.addAll(params);
        needEncrypt = true;
        return this;
    }

    public YopRequestConfig addEncryptHeader(String headerName) {
        encryptHeaders.add(StringUtils.lowerCase(headerName));
        needEncrypt = true;
        return this;
    }

    public Integer getSignExpirationInSeconds() {
        return signExpirationInSeconds;
    }

    public YopRequestConfig setSignExpirationInSeconds(Integer signExpirationInSeconds) {
        this.signExpirationInSeconds = signExpirationInSeconds;
        return this;
    }

    public static final class Builder {
        private String appKey;
        private String securityReq;
        private YopCredentials<?> credentials;
        private int readTimeout;
        private int connectTimeout;
        private Boolean needEncrypt;
        private Boolean totalEncrypt;
        private String encryptAlg;
        private Boolean skipVerifySign;
        private int signExpirationInSeconds;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withAppKey(String appKey) {
            this.appKey = appKey;
            return this;
        }

        public Builder withSecurityReq(String securityReq) {
            this.securityReq = securityReq;
            return this;
        }

        public Builder withCredentials(YopCredentials<?> credentials) {
            this.credentials = credentials;
            return this;
        }

        public Builder withRequestTimeout(Integer requestTimeout) {
            this.readTimeout = requestTimeout;
            return this;
        }

        public Builder withConnectTimeout(Integer connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder withNeedEncrypt(Boolean needEncrypt) {
            this.needEncrypt = needEncrypt;
            return this;
        }

        public Builder withTotalEncrypt(Boolean totalEncrypt) {
            this.totalEncrypt = totalEncrypt;
            return this;
        }

        public Builder withEncryptAlg(String encryptAlg) {
            this.encryptAlg = encryptAlg;
            return this;
        }

        public Builder withSkipVerifySign(Boolean skipVerifySign) {
            this.skipVerifySign = skipVerifySign;
            return this;
        }

        public Builder withSignExpirationInSeconds(int signExpirationInSeconds) {
            this.signExpirationInSeconds = signExpirationInSeconds;
            return this;
        }

        public YopRequestConfig build() {
            return new YopRequestConfig().setAppKey(appKey)
                    .setSecurityReq(securityReq)
                    .setCredentials(credentials)
                    .setReadTimeout(readTimeout)
                    .setConnectTimeout(connectTimeout)
                    .setNeedEncrypt(needEncrypt)
                    .setTotalEncrypt(totalEncrypt)
                    .setEncryptAlg(encryptAlg)
                    .setSkipVerifySign(skipVerifySign)
                    .setSignExpirationInSeconds(signExpirationInSeconds);
        }
    }
}
