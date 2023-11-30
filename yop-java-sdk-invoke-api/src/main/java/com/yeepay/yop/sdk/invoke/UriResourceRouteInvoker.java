/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

import com.yeepay.yop.sdk.invoke.model.AnalyzedException;
import com.yeepay.yop.sdk.invoke.model.ExceptionAnalyzer;
import com.yeepay.yop.sdk.invoke.model.UriResource;

/**
 * title: 基于Uri资源的路由调用器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/8
 */
public interface UriResourceRouteInvoker<Input, Output, Context, Exception extends AnalyzedException>
        extends Invoker<Input, Output, Context, Exception> {

    UriResource getUriResource();

    void setUriResource(UriResource uriResource);

    boolean isCircuitBreakerEnable();

    void enableCircuitBreaker();

    void disableCircuitBreaker();

    ExceptionAnalyzer<Exception> getExceptionAnalyzer();

    void setExceptionAnalyzer(ExceptionAnalyzer<Exception> analyzer);

}
