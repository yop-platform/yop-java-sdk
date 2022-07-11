/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security.cert;

import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * title: yop公钥<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class YopPublicKey {

    /**
     * 公钥证书
     */
    private X509Certificate cert;

    /**
     * 公钥
     */
    private PublicKey publicKey;

    public YopPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public YopPublicKey(X509Certificate cert) {
        this.cert = cert;
    }

    public X509Certificate getCert() {
        return cert;
    }

    public void setCert(X509Certificate cert) {
        this.cert = cert;
    }

    public PublicKey getPublicKey() {
        if (null != cert) {
            return cert.getPublicKey();
        }
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }
}
