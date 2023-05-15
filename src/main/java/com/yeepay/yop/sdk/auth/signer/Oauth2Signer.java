/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer;

import com.yeepay.g3.core.yop.sdk.sample.auth.SignOptions;
import com.yeepay.g3.core.yop.sdk.sample.auth.Signer;
import com.yeepay.g3.core.yop.sdk.sample.auth.YopCredentials;
import com.yeepay.g3.core.yop.sdk.sample.auth.YopCredentialsWithoutSign;
import com.yeepay.g3.core.yop.sdk.sample.auth.credentials.YopOauth2Credentials;
import com.yeepay.g3.core.yop.sdk.sample.exception.VerifySignFailedException;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopClientException;
import com.yeepay.g3.core.yop.sdk.sample.http.Headers;
import com.yeepay.g3.core.yop.sdk.sample.http.YopHttpResponse;
import com.yeepay.g3.core.yop.sdk.sample.internal.Request;
import com.yeepay.g3.core.yop.sdk.sample.model.BaseRequest;
import com.yeepay.g3.core.yop.sdk.sample.security.rsa.RSA;
import com.yeepay.g3.core.yop.sdk.sample.utils.CharacterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author qi.zhang-4
 * @version 1.0.0
 * @since 2020/1/15 上午11:19
 */
public class Oauth2Signer implements Signer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Oauth2Signer.class);

    private static final String AUTHORIZATION_PREFIX = "Bearer ";

    @Override
    public void sign(Request<? extends BaseRequest> request, YopCredentials credentials, SignOptions options) {
        checkNotNull(request, "request should not be null.");
        if (credentials == null || credentials instanceof YopCredentialsWithoutSign) {
            return;
        }
        if (!(credentials instanceof YopOauth2Credentials)) {
            throw new YopClientException("UnSupported credentials type:" + credentials.getClass().getSimpleName());
        }
        String authorizationHeader = AUTHORIZATION_PREFIX + credentials.getSecretKey();
        request.addHeader(Headers.AUTHORIZATION, authorizationHeader);
    }

    @Override
    public void checkSignature(YopHttpResponse httpResponse, String signature, PublicKey publicKey, SignOptions options) {
        String content = httpResponse.readContent();
        content = content.replaceAll("[ \t\n]", CharacterConstants.EMPTY);
        if (!RSA.verifySign(content, signature, publicKey, options.getDigestAlg())) {
            throw new VerifySignFailedException("response sign verify failure");
        }
    }
}
