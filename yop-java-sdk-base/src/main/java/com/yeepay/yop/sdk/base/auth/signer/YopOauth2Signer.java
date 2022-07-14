/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.auth.signer;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopCredentialsWithoutSign;
import com.yeepay.yop.sdk.auth.credentials.YopOauth2Credentials;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.security.SignerTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * title: YopOauth2Signer<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author qi.zhang-4
 * @version 1.0.0
 * @since 2020/1/15 上午11:19
 */
public class YopOauth2Signer implements YopSigner {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopOauth2Signer.class);

    private static final String AUTHORIZATION_PREFIX = "Bearer ";

    @Override
    public List<String> supportSignerAlg() {
        return Lists.newArrayList(SignerTypeEnum.OAUTH2.name());
    }

    @Override
    public void sign(Request<? extends BaseRequest> request, YopCredentials<?> credentials, SignOptions options) {
        checkNotNull(request, "request should not be null.");
        if (credentials == null || credentials instanceof YopCredentialsWithoutSign) {
            return;
        }
        if (!(credentials instanceof YopOauth2Credentials)) {
            throw new YopClientException("UnSupported credentials type:" + credentials.getClass().getSimpleName());
        }
        String secretKey = (String) credentials.getCredential();
        String authorizationHeader = AUTHORIZATION_PREFIX + secretKey;
        request.addHeader(Headers.AUTHORIZATION, authorizationHeader);
    }
}
