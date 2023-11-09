/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

import com.yeepay.yop.sdk.invoke.model.ExceptionAnalyzeResult;

/**
 * title: 基于Uri的路由调用器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/8
 */
public interface UriRouteInvoker<Input, Output, Context, Exception extends ExceptionAnalyzeResult>
        extends UriInvoker<Input, Output, Context, Exception> {

    boolean isCircuitBreakerEnable();

    void enableCircuitBreaker();

    void disableCircuitBreaker();

    ExceptionAnalyzer<Exception> getExceptionAnalyzer();

    void setExceptionAnalyzer(ExceptionAnalyzer<Exception> analyzer);

}
