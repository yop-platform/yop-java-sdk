package com.yeepay.g3.sdk.yop.exception;

/**
 * title: 客户端抛出的异常<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 16/2/19 10:10
 */
public class YopClientException extends RuntimeException {

    private static final long serialVersionUID = -1L;

    /**
     * Constructs a new YopClientException with the specified detail message.
     *
     * @param message the detail error message.
     */
    public YopClientException(String message) {
        super(message);
    }

    /**
     * Constructs a new YopClientException with the specified detail message and the underlying cause.
     *
     * @param message the detail error message.
     * @param cause   the underlying cause of this exception.
     */
    public YopClientException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
