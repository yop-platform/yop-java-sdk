package com.yeepay.yop.sdk.auth;

import com.yeepay.g3.core.yop.sdk.sample.http.YopHttpResponse;
import com.yeepay.g3.core.yop.sdk.sample.internal.Request;
import com.yeepay.g3.core.yop.sdk.sample.model.BaseRequest;

import java.security.PublicKey;

/**
 * title: 签名器<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2017<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/13 16:24
 */
public interface Signer {

    Signer NO_OP_SIGNER = new Signer() {
        @Override
        public void sign(Request<?> request, YopCredentials credentials, SignOptions options) {
            //do nothing
        }

        @Override
        public void checkSignature(YopHttpResponse httpResponse, String signature, PublicKey publicKey, SignOptions options) {
            //do nothing
        }


    };

    /**
     * Sign the given request with the given set of credentials. Modifies the
     * passed-in request to apply the signature.
     *
     * @param request     The request to sign.
     * @param credentials The credentials to sign the request with.
     * @param options     options
     */
    void sign(Request<? extends BaseRequest> request, YopCredentials credentials, SignOptions options);

    /**
     * 校验签名
     *
     * @param httpResponse http返回结果
     * @param signature    签名
     * @param publicKey    公钥
     * @param options      签名选项
     */
    void checkSignature(YopHttpResponse httpResponse, String signature, PublicKey publicKey, SignOptions options);

}