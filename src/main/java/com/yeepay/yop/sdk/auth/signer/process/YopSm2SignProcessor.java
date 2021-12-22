/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer.process;

import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.utils.Sm2Utils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/18 3:59 下午
 */
public class YopSm2SignProcessor implements YopSignProcessor {

    @Override
    public String doSign(String content, CredentialsItem credentialsItem) {
        PKICredentialsItem pkiCredentialsItem = (PKICredentialsItem) credentialsItem;
        return Sm2Utils.sign(content, (BCECPrivateKey) pkiCredentialsItem.getPrivateKey());
    }

    @Override
    public boolean doVerify(String content, String signature, CredentialsItem credentialsItem) {
        PKICredentialsItem pkiCredentialsItem = (PKICredentialsItem) credentialsItem;
        return Sm2Utils.verifySign(content, signature, (BCECPublicKey) pkiCredentialsItem.getPublicKey());
    }

    @Override
    public boolean isSupport(CredentialsItem credentialsItem) {
        return credentialsItem instanceof PKICredentialsItem;
    }

    @Override
    public DigestAlgEnum getDigestAlg() {
        return DigestAlgEnum.SM3;
    }
}
