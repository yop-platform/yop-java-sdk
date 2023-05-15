package com.yeepay.yop.sdk.http.analyzer;

import com.yeepay.g3.core.yop.sdk.sample.http.HttpResponseAnalyzer;
import com.yeepay.g3.core.yop.sdk.sample.http.HttpResponseHandleContext;
import com.yeepay.g3.core.yop.sdk.sample.model.BaseResponse;
import com.yeepay.g3.core.yop.sdk.sample.utils.JsonUtils;

/**
 * title: YopJsonResponseAnalyzer<br/>
 * description: HTTP body json response handler for YOP responses.<br/>
 * Copyright: Copyright (c) 2017<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/29 17:54
 */
public class YopJsonResponseAnalyzer implements HttpResponseAnalyzer {

    private static final YopJsonResponseAnalyzer INSTANCE = new YopJsonResponseAnalyzer();

    public static YopJsonResponseAnalyzer getInstance() {
        return INSTANCE;
    }

    protected YopJsonResponseAnalyzer() {
    }

    @Override
    public <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception {
        String content = context.getResponse().readContent();
        if (content != null) {
            if (response.getMetadata().getContentLength() > 0
                    || "chunked".equalsIgnoreCase(response.getMetadata().getTransferEncoding())) {
                JsonUtils.load(content, response);
            }
        }
        return true;
    }
}
