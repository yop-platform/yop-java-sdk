/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials;

import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/20 12:20 上午
 */
public class PKICredentialsItem {

    private PrivateKey privateKey;

    private PublicKey publicKey;

    private CertTypeEnum certType;

    public PKICredentialsItem(PrivateKey privateKey, PublicKey publicKey, CertTypeEnum certType) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.certType = certType;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public CertTypeEnum getCertType() {
        return certType;
    }

}
