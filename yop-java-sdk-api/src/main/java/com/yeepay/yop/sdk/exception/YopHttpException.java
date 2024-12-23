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

    /**
     * The HTTP status code that was returned with this error.
     */
    private int statusCode;

    public YopHttpException(String message) {
        super(message);
    }

    public YopHttpException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public YopHttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public YopHttpException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
