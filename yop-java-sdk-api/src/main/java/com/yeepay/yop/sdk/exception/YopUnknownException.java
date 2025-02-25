/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.exception;

import static com.yeepay.yop.sdk.constants.CharacterConstants.COMMA;
import static com.yeepay.yop.sdk.constants.ExceptionConstants.SDK_INVOKE_UNEXPECTED_EXCEPTION;

/**
 * title: Yop未知异常<br>
 * description: 该异常不会重试<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/27
 */
public class YopUnknownException extends RuntimeException implements YopTracedException {
    private static final long serialVersionUID = -1L;

    private String requestId;

    public YopUnknownException(String message) {
        super(message);
    }

    public YopUnknownException(String message, Throwable cause) {
        super(message, cause);
    }

    public YopUnknownException(String message, Throwable cause, String requestId) {
        super(message, cause);
        this.requestId = requestId;
    }

    @Override
    public String getErrorCode() {
        return SDK_INVOKE_UNEXPECTED_EXCEPTION;
    }

    @Override
    public String getRequestId() {
        return this.requestId;
    }

    @Override
    public String getMessage() {
        return getErrorCode() + COMMA + getRequestId() + COMMA + super.getMessage();
    }
}
