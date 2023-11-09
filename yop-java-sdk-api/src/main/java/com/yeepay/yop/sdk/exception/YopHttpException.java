/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.exception;

/**
 * title: Yop-http调用异常<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/27
 */
public class YopHttpException extends RuntimeException {
    private static final long serialVersionUID = -1L;

    public YopHttpException(String message) {
        super(message);
    }

    public YopHttpException(String message, Throwable cause) {
        super(message, cause);
    }
}
