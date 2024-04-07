/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router;

import com.yeepay.yop.sdk.invoke.model.RetryContext;

import java.util.Map;

/**
 * title: 上下文信息<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/2/2
 */
public class SimpleContext implements RetryContext {

    /**
     * 上下文参数
     */
    private Map<String, Object> context;

    /**
     * 重试次数
     */
    private int retryCount = 0;

    public int getRetryCount() {
        return retryCount;
    }

    public void addRetryCount(int i) {
        this.retryCount += i;
    }

    @Override
    public void markRetried(Object... args) {
        addRetryCount((int) args[0]);
    }

    @Override
    public int retryCount() {
        return this.getRetryCount();
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
}
