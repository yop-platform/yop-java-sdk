package com.yeepay.yop.sdk.auth;

import com.yeepay.yop.sdk.security.DigestAlgEnum;

/**
 * title: 签名option<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/1 15:11
 */
public class SignOptions {

    public static final int DEFAULT_EXPIRATION_IN_SECONDS = 1800;

    /**
     * 摘要算法
     */
    private DigestAlgEnum digestAlg;

    /**
     * 协议前缀
     */
    private String protocolPrefix;

    /**
     * 过期时间
     */
    private int expirationInSeconds = DEFAULT_EXPIRATION_IN_SECONDS;

    /**
     * 是否做url安全编码
     */
    private boolean urlSafe = true;

    public DigestAlgEnum getDigestAlg() {
        return digestAlg;
    }

    public void setDigestAlg(DigestAlgEnum digestAlg) {
        this.digestAlg = digestAlg;
    }

    public SignOptions withDigestAlg(DigestAlgEnum digestAlg) {
        this.digestAlg = digestAlg;
        return this;
    }

    public String getProtocolPrefix() {
        return protocolPrefix;
    }

    public void setProtocolPrefix(String protocolPrefix) {
        this.protocolPrefix = protocolPrefix;
    }

    public SignOptions withProtocolPrefix(String protocolPrefix) {
        this.protocolPrefix = protocolPrefix;
        return this;
    }

    public void setExpirationInSeconds(int expirationInSeconds) {
        this.expirationInSeconds = expirationInSeconds;
    }

    public int getExpirationInSeconds() {
        return expirationInSeconds;
    }

    public SignOptions withExpirationInSeconds(int expirationInSeconds) {
        this.expirationInSeconds = expirationInSeconds;
        return this;
    }

    public boolean isUrlSafe() {
        return urlSafe;
    }

    public SignOptions withUrlSafe(boolean urlSafe) {
        this.urlSafe = urlSafe;
        return this;
    }
}
