/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.inter.auth.signer.process;

import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.inter.utils.RSA;

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
    public String doSign(String content, CredentialsItem credentialsItem, SignOptions options) {
        PKICredentialsItem pkiCredentialsItem = (PKICredentialsItem) credentialsItem;
        return RSA.sign(content, pkiCredentialsItem.getPrivateKey(), DIGEST_ALG);
    }

    @Override
    public boolean doVerify(String content, String signature, CredentialsItem credentialsItem, SignOptions options) {
        PKICredentialsItem pkiCredentialsItem = (PKICredentialsItem) credentialsItem;
        return RSA.verifySign(content, signature, pkiCredentialsItem.getPublicKey(), DIGEST_ALG);
    }

    @Override
    public boolean isSupport(CredentialsItem credentialsItem) {
        return credentialsItem instanceof PKICredentialsItem;
    }

    @Override
    public String name() {
        return "RSA2048";
    }

    @Override
    public String getDigestAlg() {
        return DigestAlgEnum.SHA256.name();
    }
}
