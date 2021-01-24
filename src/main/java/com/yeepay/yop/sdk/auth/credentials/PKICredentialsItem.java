/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials;

import com.yeepay.yop.sdk.security.CertTypeEnum;

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

    private String privateKey;

    private String publicKey;

    private CertTypeEnum certType;

    public PKICredentialsItem(String privateKey, String publicKey, CertTypeEnum certType) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.certType = certType;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public CertTypeEnum getCertType() {
        return certType;
    }

}
