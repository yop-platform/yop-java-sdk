package com.yeepay.yop.sdk.client;

import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;

/**
 * title: 客户端处理器接口<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/14 11:33
 */
public interface ClientHandler {

    /**
     * Execute's a web service request. Handles marshalling and unmarshalling of data and making the
     * underlying HTTP call(s).
     *
     * @param executionParams Parameters specific to this invocation of an API.
     * @param <Input>         Input Request type
     * @param <Output>        Output Response type
     * @return Unmarshalled output Response type.
     */
    <Input extends BaseRequest, Output extends BaseResponse> Output execute(ClientExecutionParams<Input, Output> executionParams);

    /**
     * Shutdown and release any underlying resources.
     */
    void shutdown();
}
