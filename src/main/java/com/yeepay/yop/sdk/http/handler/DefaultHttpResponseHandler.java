package com.yeepay.yop.sdk.http.handler;

import com.yeepay.yop.sdk.http.HttpResponseAnalyzer;
import com.yeepay.yop.sdk.http.HttpResponseHandleContext;
import com.yeepay.yop.sdk.http.HttpResponseHandler;
import com.yeepay.yop.sdk.model.BaseResponse;

/**
 * title: 默认HttpResponseHandler<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/29 17:26
 */
public class DefaultHttpResponseHandler<T extends BaseResponse> implements HttpResponseHandler<T> {

    private final Class<T> responseClass;

    private final HttpResponseAnalyzer[] analyzers;

    public DefaultHttpResponseHandler(Class<T> responseClass, HttpResponseAnalyzer[] analyzers) {
        this.responseClass = responseClass;
        this.analyzers = analyzers;
    }

    @Override
    public T handle(HttpResponseHandleContext context) throws Exception {
        T response = responseClass.newInstance();
        for (HttpResponseAnalyzer analyzer : analyzers) {
            if (analyzer.analysis(context, response)) {
                break;
            }
        }
        return response;
    }

}
