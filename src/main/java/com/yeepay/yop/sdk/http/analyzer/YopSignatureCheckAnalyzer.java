package com.yeepay.yop.sdk.http.analyzer;

import com.yeepay.g3.core.yop.sdk.sample.http.HttpResponseAnalyzer;
import com.yeepay.g3.core.yop.sdk.sample.http.HttpResponseHandleContext;
import com.yeepay.g3.core.yop.sdk.sample.model.BaseResponse;
import com.yeepay.g3.core.yop.sdk.sample.model.YopResponseMetadata;
import org.apache.commons.lang3.StringUtils;

/**
 * title: 签名校验<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2017<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/1 10:43
 */
public class YopSignatureCheckAnalyzer implements HttpResponseAnalyzer {

    private static final YopSignatureCheckAnalyzer INSTANCE = new YopSignatureCheckAnalyzer();

    public static YopSignatureCheckAnalyzer getInstance() {
        return INSTANCE;
    }

    private YopSignatureCheckAnalyzer() {
    }

    @Override
    public <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception {
        YopResponseMetadata metadata = response.getMetadata();
        if (StringUtils.isNotEmpty(metadata.getYopSign())) {
            context.getSigner().checkSignature(context.getResponse(), metadata.getYopSign(), context.getYopPublicKey(), context.getSignOptions());
        }
        return false;
    }

}
