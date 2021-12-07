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
     * 执行请求
     * @param request 请求对象
     * @param yopRequestConfig 请求配置对象
     * @param executionContext 执行上下文
     * @param responseHandler 回调处理器
     * @param <Output> 响应对象泛型
     * @param <Input> 请求对象泛型
     * @return
     */
    <Output extends BaseResponse, Input extends BaseRequest> Output execute(Request<Input> request,
                                                                            YopRequestConfig yopRequestConfig,
                                                                            ExecutionContext executionContext,
                                                                            HttpResponseHandler<Output> responseHandler);

    /**
     * Shutdown and release any underlying resources.
     */
    void shutdown();
}
