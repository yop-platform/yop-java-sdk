/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.auth.signer;

import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.base.auth.signer.process.YopSignProcessorFactory;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.exception.VerifySignFailedException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.security.CertTypeEnum;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/10/13
 */
public abstract class YopBaseSigner implements YopSigner {

    /**
     * 验签
     *
     * @param httpResponse
     * @param signature
     */
    @Override
    public void checkSignature(YopHttpResponse httpResponse, String signature, YopCredentials<?> credentials, SignOptions options) {
        String content = httpResponse.readContent();
        final String requestId = httpResponse.getHeader(Headers.YOP_REQUEST_ID);
        YopPlatformCredentials platformCredentials = (YopPlatformCredentials) credentials;
        final CertTypeEnum certType = platformCredentials.getCredential().getCertType();
        content = content.replaceAll("[ \t\n]", CharacterConstants.EMPTY);
        if (!YopSignProcessorFactory.getSignProcessor(certType.name()).doVerify(content, signature, platformCredentials.getCredential(), options)) {
            throw new VerifySignFailedException(String.format("response sign verify failure, content:%s, signature:%s, platformSerialNo:%s, requestId:%s."
                    , content, signature, platformCredentials.getSerialNo(), requestId));
        }
    }
}
