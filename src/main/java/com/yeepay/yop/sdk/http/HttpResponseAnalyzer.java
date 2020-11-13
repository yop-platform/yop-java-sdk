package com.yeepay.yop.sdk.http;

import com.yeepay.yop.sdk.model.BaseResponse;

/**
 * title: httpResponse分析器<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/29 17:38
 */
public interface HttpResponseAnalyzer {

    /**
     * 解析httpResponse
     *
     * @param context  http返回结果处理上下文
     * @param response response
     * @param <T>      范型
     * @return true结束，false继续
     * @throws Exception exception
     */
    <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception;

}
