package com.yeepay.yop.sdk.http;

import com.yeepay.g3.core.yop.sdk.sample.auth.Encryptor;
import com.yeepay.g3.core.yop.sdk.sample.auth.SignOptions;
import com.yeepay.g3.core.yop.sdk.sample.auth.Signer;
import com.yeepay.g3.core.yop.sdk.sample.internal.Request;
import com.yeepay.g3.core.yop.sdk.sample.model.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.io.Serializable;
import java.security.PublicKey;

/**
 * title: http返回处理上下文<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2019<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-04-28 15:37
 */
public class HttpResponseHandleContext implements Serializable {

    private static final long serialVersionUID = -1L;

    private YopHttpResponse response;

    private Request originRequest;

    private Signer signer;

    private SignOptions signOptions;

    private PublicKey yopPublicKey;

    private Boolean needDecrypt;

    private Encryptor encryptor;

    public HttpResponseHandleContext(CloseableHttpResponse httpResponse, Request originRequest, RequestConfig requestConfig, ExecutionContext executionContext) throws IOException {
        this.response = new YopHttpResponse(httpResponse);
        this.originRequest = originRequest;
        this.signer = executionContext.getSigner();
        this.signOptions = executionContext.getSignOptions();
        this.yopPublicKey = executionContext.getYopPublicKey();
        this.needDecrypt = requestConfig.getNeedEncrypt();
        this.encryptor = executionContext.getEncryptor();
    }

    public YopHttpResponse getResponse() {
        return response;
    }

    public Request getOriginRequest() {
        return originRequest;
    }

    public Signer getSigner() {
        return signer;
    }

    public SignOptions getSignOptions() {
        return signOptions;
    }

    public PublicKey getYopPublicKey() {
        return yopPublicKey;
    }

    public Boolean isNeedDecrypt() {
        return needDecrypt;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }


}