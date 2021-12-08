/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http;

import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopRequestConfig;

/**
 * title: http协议实现接口<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021/12/1
 */
public interface YopHttpClient {

    /**
     * execute the http request
     * @param yopRequest a yop request
     * @param yopRequestConfig request config to override the default client config
     * @param executionContext context
     * @param responseHandler the response handler for callback
     * @return the response
     */
    <Output extends BaseResponse, Input extends BaseRequest> Output execute(Request<Input> yopRequest,
                                                                            YopRequestConfig yopRequestConfig,
                                                                            ExecutionContext executionContext,
                                                                            HttpResponseHandler<Output> responseHandler);

    /**
     * Shutdown and release any underlying resources.
     */
    void shutdown();
}
