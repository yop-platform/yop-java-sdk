/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.encryptor.signer.process;

import com.google.common.collect.ImmutableMap;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.signer.process.YopRsaSignProcessor;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.auth.signer.process.YopSm2SignProcessor;
import com.yeepay.yop.sdk.encryptor.auth.credentials.MockEncryptorCredentialsItem;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.security.rsa.RSA;
import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;
import com.yeepay.yop.sdk.utils.Sm2Utils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;

import java.util.Map;

/**
 * title: 加密机签名执行器，实际调用加密机<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author qi.zhang-4
 * @version 1.0.0
 * @since 2021/12/23 10:23 上午
 */
public class MockEncryptorSignProcessor implements YopSignProcessor {

    private static final Map<CertTypeEnum, YopSignProcessor> softSignProcessors = ImmutableMap
            .of(CertTypeEnum.SM2, new YopSm2SignProcessor(), CertTypeEnum.RSA2048, new YopRsaSignProcessor());

    private static final DigestAlgEnum DIGEST_ALG = DigestAlgEnum.SM3;

    @Override
    public String doSign(String content, CredentialsItem credentialsItem) {
        MockEncryptorCredentialsItem mockEncryptorCredentialsItem = (MockEncryptorCredentialsItem) credentialsItem;
        CertTypeEnum certType = credentialsItem.getCertType();
        if (certType == CertTypeEnum.SM2) {
            return Sm2Utils.sign(content, (BCECPrivateKey) Sm2Utils.string2PrivateKey(mockEncryptorCredentialsItem.getEncryptorCertKey()));
        }
        if (certType == CertTypeEnum.RSA2048) {
            return RSA.sign(content, RSAKeyUtils.string2PrivateKey(mockEncryptorCredentialsItem.getEncryptorCertKey()), DIGEST_ALG);
        }
        throw new YopClientException("UnSupported cert type:" + certType);
    }

    @Override
    public boolean verify(String content, String signature, CredentialsItem credentialsItem) {
        if (credentialsItem instanceof PKICredentialsItem) {
            return doVerify(content, signature, credentialsItem);
        }
        throw new YopClientException("UnSupported credentialsItem type:" + credentialsItem.getClass().getSimpleName());
    }

    @Override
    public boolean doVerify(String content, String signature, CredentialsItem credentialsItem) {
        PKICredentialsItem pkiCredentialsItem = (PKICredentialsItem) credentialsItem;
        YopSignProcessor signProcessor = softSignProcessors.get(pkiCredentialsItem.getCertType());
        return signProcessor.verify(content, signature, credentialsItem);
    }

    @Override
    public boolean isSupport(CredentialsItem credentialsItem) {
        return credentialsItem instanceof MockEncryptorCredentialsItem;
    }

    @Override
    public String name() {
        return CertTypeEnum.SM2.name();
    }

    @Override
    public String getDigestAlg() {
        return DIGEST_ALG.name();
    }
}
