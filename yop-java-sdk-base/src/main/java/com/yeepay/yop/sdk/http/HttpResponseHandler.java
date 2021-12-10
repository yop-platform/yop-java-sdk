package com.yeepay.yop.sdk.http;

import com.yeepay.yop.sdk.model.BaseResponse;

/**
 * Responsible for handling an HTTP response and returning an object of type T.
 * For example, a typical response handler might accept a response, and
 * translate it into a concrete typed object.
 *
 * @param <T> The output of this response handler.
 */
public interface HttpResponseHandler<T extends BaseResponse> {


    /**
     * http返回结果处理器
     *
     * @param context 上下文
     * @return 业务返回结果
     * @throws Exception 异常
     */
    T handle(HttpResponseHandleContext context) throws Exception;

}
