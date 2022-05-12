/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer;

import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessorFactory;
import com.yeepay.yop.sdk.exception.VerifySignFailedException;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.CharacterConstants;

import java.util.List;

/**
 * title: Yop 签名器<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/18 3:23 下午
 */
public interface YopSigner {

    /**
     * 支持的签名算法
     *
     * @return 支持的签名算法
     */
    List<String> supportSignerAlg();

    /**
     * 签名
     *
     * @param request
     * @param credentials
     */
    void sign(Request<? extends BaseRequest> request, YopCredentials<?> credentials, SignOptions options);

    /**
     * 验签
     *
     * @param httpResponse
     * @param signature
     */
    default void checkSignature(YopHttpResponse httpResponse, String signature, YopCredentials<?> credentials, SignOptions options) {
        String content = httpResponse.readContent();
        YopPlatformCredentials platformCredentials = (YopPlatformCredentials) credentials;
        final CertTypeEnum certType = platformCredentials.getCredential().getCertType();
        content = content.replaceAll("[ \t\n]", CharacterConstants.EMPTY);
        if (!YopSignProcessorFactory.getSignProcessor(certType.name()).verify(content, signature, platformCredentials.getCredential())) {
            throw new VerifySignFailedException("response sign verify failure");
        }
    }

}
