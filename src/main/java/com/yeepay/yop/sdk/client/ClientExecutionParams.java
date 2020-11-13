package com.yeepay.yop.sdk.client;

import com.yeepay.yop.sdk.http.HttpResponseHandler;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.transform.RequestMarshaller;

/**
 * title: 客户端执行参数<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/14 11:33
 */
public class ClientExecutionParams<Input extends BaseRequest, Output extends BaseResponse> {

    private Input input;

    private RequestMarshaller<Input> requestMarshaller;

    private HttpResponseHandler<Output> responseHandler;


    public Input getInput() {
        return input;
    }

    public ClientExecutionParams<Input, Output> withInput(Input input) {
        this.input = input;
        return this;
    }

    public RequestMarshaller<Input> getRequestMarshaller() {
        return requestMarshaller;
    }

    public ClientExecutionParams<Input, Output> withRequestMarshaller(RequestMarshaller<Input> requestMarshaller) {
        this.requestMarshaller = requestMarshaller;
        return this;
    }

    public HttpResponseHandler<Output> getResponseHandler() {
        return responseHandler;
    }

    public ClientExecutionParams<Input, Output> withResponseHandler(
            HttpResponseHandler<Output> responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

}