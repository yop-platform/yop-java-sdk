/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

import com.yeepay.yop.sdk.invoke.model.AnalyzedException;
import com.yeepay.yop.sdk.invoke.model.ExceptionAnalyzer;
import com.yeepay.yop.sdk.invoke.model.Resource;

/**
 * title: 资源路由调用器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/8
 */
public interface ResourceRouteInvoker<Input, Output, Context, Exception extends AnalyzedException>
        extends Invoker<Input, Output, Context, Exception> {

    Resource getResource();

    void setResource(Resource resource);

    ExceptionAnalyzer<Exception> getExceptionAnalyzer();

    void setExceptionAnalyzer(ExceptionAnalyzer<Exception> analyzer);

}
