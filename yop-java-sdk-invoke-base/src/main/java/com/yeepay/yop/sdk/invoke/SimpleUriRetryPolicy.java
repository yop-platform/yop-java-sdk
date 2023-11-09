/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

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
public class SimpleUriRetryPolicy implements RetryPolicy {

    private static final SimpleUriRetryPolicy INSTANCE = new SimpleUriRetryPolicy();

    @Override
    public boolean allowRetry(Object ...args) {
        return true;
    }

    public static RetryPolicy singleton() {
        return INSTANCE;
    }
}
