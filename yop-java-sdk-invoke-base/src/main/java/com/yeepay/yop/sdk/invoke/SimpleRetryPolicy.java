/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

import com.yeepay.yop.sdk.invoke.model.RetryContext;
import com.yeepay.yop.sdk.invoke.model.RetryPolicy;

/**
 * title: 重试策略<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/7
 */
public class SimpleRetryPolicy implements RetryPolicy {

    private static final SimpleRetryPolicy INSTANCE = new SimpleRetryPolicy();

    public SimpleRetryPolicy() {
    }

    public SimpleRetryPolicy(int maxRetryCount) {
        if (maxRetryCount > 0) {
            this.maxRetryCount = maxRetryCount;
        }
    }

    private int maxRetryCount = 3;

    @Override
    public boolean allowRetry(Object ...args) {
        if (null != args && args.length > 0 && args[0] instanceof Invoker) {
            Invoker<?, ?, ?, ?> invoker = (Invoker<?, ?, ?, ?>) args[0];
            if (invoker.getContext() instanceof RetryContext) {
                return ((RetryContext)invoker.getContext()).retryCount() < this.maxRetryCount;
            }
        }
        return true;
    }

    public static RetryPolicy singleton() {
        return INSTANCE;
    }
}
