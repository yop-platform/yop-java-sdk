/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.encryptor.signer.process;

import com.google.common.collect.ImmutableMap;
import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessorAdaptor;
import com.yeepay.yop.sdk.encryptor.auth.credentials.MockEncryptorCredentialsItem;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.gm.auth.signer.process.YopSm2SignProcessor;
import com.yeepay.yop.sdk.inter.auth.signer.process.YopRsaSignProcessor;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.DigestAlgEnum;

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
public class MockEncryptorSignProcessor extends YopSignProcessorAdaptor {

    private static final Map<CertTypeEnum, YopSignProcessor> softSignProcessors = ImmutableMap
            .of(CertTypeEnum.SM2, new YopSm2SignProcessor(), CertTypeEnum.RSA2048, new YopRsaSignProcessor());

    private static final DigestAlgEnum DIGEST_ALG = DigestAlgEnum.SM3;

    @Override
    public String doSign(String content, CredentialsItem credentialsItem, SignOptions options) {
        MockEncryptorCredentialsItem mockEncryptorCredentialsItem = (MockEncryptorCredentialsItem) credentialsItem;
        CertTypeEnum certType = credentialsItem.getCertType();
        if (certType == CertTypeEnum.SM2) {
            return "mock sign, to be impl by yourself";
        }
        if (certType == CertTypeEnum.RSA2048) {
            return "mock sign, to be impl by yourself";
        }
        throw new YopClientException("UnSupported cert type:" + certType);
    }

    @Override
    public boolean doVerify(String content, String signature, CredentialsItem credentialsItem, SignOptions options) {
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
