/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

import com.yeepay.yop.sdk.invoke.model.AnalyzedException;

import java.net.URI;

/**
 * title: 基于URI的调用<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/7
 */
public interface UriInvoker<Input, Output, Context, Exception extends AnalyzedException>
        extends Invoker<Input, Output, Context, Exception> {

    URI getUri();

    void setUri(URI uri);

}
