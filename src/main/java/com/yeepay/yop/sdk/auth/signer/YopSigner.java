/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer;

import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.signer.process.YopRsaSignProcess;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcess;
import com.yeepay.yop.sdk.auth.signer.process.YopSm2SignProcess;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.security.CertTypeEnum;

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
    Map<CertTypeEnum, YopSignProcess> signerProcessMap = new HashMap() {
        {
            put(CertTypeEnum.SM2, new YopSm2SignProcess());
            put(CertTypeEnum.RSA2048, new YopRsaSignProcess());
        }
    };

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
    void checkSignature(YopHttpResponse httpResponse, String signature, YopCredentials credentials, SignOptions options);

    void registerYopSignProcess(CertTypeEnum certTypeEnum, YopSignProcess yopSignProcess);

    YopSignProcess getSignProcess(CertTypeEnum certType);
}
