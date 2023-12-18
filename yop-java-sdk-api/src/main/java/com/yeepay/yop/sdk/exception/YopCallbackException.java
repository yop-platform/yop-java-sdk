/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.exception;

/**
 * title: Yop回调异常<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/27
 */
public class YopCallbackException extends RuntimeException {
    private static final long serialVersionUID = -1L;

    public YopCallbackException(String message) {
        super(message);
    }

    public YopCallbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
