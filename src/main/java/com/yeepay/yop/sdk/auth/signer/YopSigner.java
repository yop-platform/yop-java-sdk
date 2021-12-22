/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer;

import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.exception.VerifySignFailedException;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.CharacterConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/18 3:23 下午
 */
public interface YopSigner {
    Map<CertTypeEnum, YopSignProcessor> signerProcessMap = new HashMap<>();

    /**
     * 签名
     *
     * @param request
     * @param credentials
     */
    void sign(Request<? extends BaseRequest> request, YopCredentials credentials, SignOptions options);

    /**
     * 验签
     *
     * @param httpResponse
     * @param signature
     */
    default void checkSignature(YopHttpResponse httpResponse, String signature, YopCredentials credentials, SignOptions options) {
        String content = httpResponse.readContent();
        PKICredentialsItem pkiCredentialsItem = (PKICredentialsItem) credentials.getCredential();
        content = content.replaceAll("[ \t\n]", CharacterConstants.EMPTY);
        if (!signerProcessMap.get(pkiCredentialsItem.getCertType()).verify(content, signature, pkiCredentialsItem)) {
            throw new VerifySignFailedException("response sign verify failure");
        }
    }

    static void registerYopSignProcess(CertTypeEnum certTypeEnum, YopSignProcessor yopSignProcessor) {
        signerProcessMap.put(certTypeEnum, yopSignProcessor);
    }

    static YopSignProcessor getSignProcess(CertTypeEnum certType) {
        return signerProcessMap.get(certType);
    }
}
