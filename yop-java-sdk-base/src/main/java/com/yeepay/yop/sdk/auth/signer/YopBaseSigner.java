/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer;

import com.google.common.collect.ImmutableMap;
import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.signer.process.YopRsaSignProcessor;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.auth.signer.process.YopSm2SignProcessor;
import com.yeepay.yop.sdk.exception.VerifySignFailedException;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.CharacterConstants;

import java.util.Map;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/4/27 9:44 上午
 */
public abstract class YopBaseSigner implements YopSigner {

    Map<CertTypeEnum, YopSignProcessor> signerProcessorMap = new ImmutableMap.Builder<CertTypeEnum, YopSignProcessor>()
            .put(CertTypeEnum.SM2, new YopSm2SignProcessor())
            .put(CertTypeEnum.RSA2048, new YopRsaSignProcessor())
            .build();

    @Override
    public void checkSignature(YopHttpResponse httpResponse, String signature, YopCredentials credentials, SignOptions options) {
        String content = httpResponse.readContent();
        PKICredentialsItem pkiCredentialsItem = (PKICredentialsItem) credentials.getCredential();
        content = content.replaceAll("[ \t\n]", CharacterConstants.EMPTY);
        if (!signerProcessorMap.get(pkiCredentialsItem.getCertType()).verify(content, signature, pkiCredentialsItem)) {
            throw new VerifySignFailedException("response sign verify failure");
        }
    }

    @Override
    public void registerYopSignProcessor(CertTypeEnum certTypeEnum, YopSignProcessor yopSignProcessor) {
        signerProcessorMap.put(certTypeEnum, yopSignProcessor);
    }

    @Override
    public YopSignProcessor getSignProcessor(CertTypeEnum certType) {
        return signerProcessorMap.get(certType);
    }
}
