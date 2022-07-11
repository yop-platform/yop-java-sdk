package com.yeepay.yop.sdk.http;

import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.base.auth.signer.YopSigner;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;

/**
 * title: http返回处理上下文<br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-04-28 15:37
 */
public class HttpResponseHandleContext implements Serializable {

    private static final long serialVersionUID = -1L;

    private final String appKey;

    private final YopHttpResponse response;

    private final Request originRequest;

    private final YopSigner signer;

    private final SignOptions signOptions;

    private final Boolean skipVerifySign;

    private final YopCredentials<?> yopCredentials;

    private final boolean encryptSupported;

    private final YopEncryptor encryptor;

    private final EncryptOptions encryptOptions;

    public HttpResponseHandleContext(YopHttpResponse httpResponse,
                                     Request originRequest,
                                     ExecutionContext executionContext) throws IOException, ExecutionException, InterruptedException {
        this.appKey = (String) originRequest.getHeaders().get(Headers.YOP_APPKEY);
        this.response = httpResponse;
        this.originRequest = originRequest;
        this.signer = executionContext.getSigner();
        this.signOptions = executionContext.getSignOptions();
        this.skipVerifySign = originRequest.getOriginalRequestObject().getRequestConfig().getSkipVerifySign();
        this.yopCredentials = executionContext.getYopCredentials();
        this.encryptSupported = executionContext.isEncryptSupported();
        if (executionContext.isEncryptSupported()) {
            this.encryptor = executionContext.getEncryptor();
            this.encryptOptions = executionContext.getEncryptOptions().get();
        } else {
            this.encryptor = null;
            this.encryptOptions = null;
        }
    }

    public String getAppKey() {
        return appKey;
    }

    public YopHttpResponse getResponse() {
        return response;
    }

    public Request getOriginRequest() {
        return originRequest;
    }

    public YopSigner getSigner() {
        return signer;
    }

    public SignOptions getSignOptions() {
        return signOptions;
    }

    public Boolean isSkipVerifySign() {
        return skipVerifySign;
    }

    public YopCredentials<?> getYopCredentials() {
        return yopCredentials;
    }

    public boolean isEncryptSupported() {
        return encryptSupported;
    }

    public YopEncryptor getEncryptor() {
        return encryptor;
    }

    public EncryptOptions getEncryptOptions() {
        return encryptOptions;
    }
}
