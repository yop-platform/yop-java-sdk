/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer.process;

import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.security.rsa.RSA;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/18 3:58 下午
 */
public class YopRsaSignProcessor implements YopSignProcessor {

    private static final DigestAlgEnum DIGEST_ALG = DigestAlgEnum.SHA256;

    @Override
    public String doSign(String content, CredentialsItem credentialsItem) {
        PKICredentialsItem pkiCredentialsItem = (PKICredentialsItem) credentialsItem;
        return RSA.sign(content, pkiCredentialsItem.getPrivateKey(), DIGEST_ALG);
    }

    @Override
    public boolean doVerify(String content, String signature, CredentialsItem credentialsItem) {
        PKICredentialsItem pkiCredentialsItem = (PKICredentialsItem) credentialsItem;
        return RSA.verifySign(content, signature, pkiCredentialsItem.getPublicKey(), DIGEST_ALG);
    }

    @Override
    public boolean isSupport(CredentialsItem credentialsItem) {
        return credentialsItem instanceof PKICredentialsItem;
    }

    @Override
    public DigestAlgEnum getDigestAlg() {
        return DigestAlgEnum.SHA256;
    }
}
