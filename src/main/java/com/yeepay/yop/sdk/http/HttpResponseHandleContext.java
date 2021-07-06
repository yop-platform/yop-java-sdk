package com.yeepay.yop.sdk.http;

import com.yeepay.yop.sdk.auth.Encryptor;
import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.signer.YopSigner;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.io.Serializable;

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

    private final Boolean needDecrypt;

    private final Encryptor encryptor;

    private final Boolean skipVerifySign;

    public HttpResponseHandleContext(CloseableHttpResponse httpResponse,
                                     Request originRequest,
                                     YopRequestConfig yopRequestConfig,
                                     ExecutionContext executionContext) throws IOException {
        this.appKey = (String) originRequest.getHeaders().get(Headers.YOP_APPKEY);
        this.response = new YopHttpResponse(httpResponse);
        this.originRequest = originRequest;
        this.signer = executionContext.getSigner();
        this.signOptions = executionContext.getSignOptions();
        this.needDecrypt = yopRequestConfig.getNeedEncrypt();
        this.encryptor = executionContext.getEncryptor();
        this.skipVerifySign = yopRequestConfig.getSkipVerifySign();
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

    public Boolean isNeedDecrypt() {
        return needDecrypt;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }

    public Boolean isSkipVerifySign() {
        return skipVerifySign;
    }
}
