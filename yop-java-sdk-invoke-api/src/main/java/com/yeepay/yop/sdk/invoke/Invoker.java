/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

import com.yeepay.yop.sdk.invoke.model.ExceptionAnalyzeResult;

import java.util.List;

/**
 * title: 调用器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/6
 */
public interface Invoker<Input, Output, Context, Exception extends ExceptionAnalyzeResult> {

    Output invoke();

    Input getInput();

    void setInput(Input input);

    void setContext(Context context);

    Context getContext();

    List<Exception> getExceptions();

    void addException(Exception exception);

    Exception getLastException();

}
