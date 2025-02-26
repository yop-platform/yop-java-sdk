/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.exception.io;

import com.yeepay.yop.sdk.exception.YopHttpException;
import com.yeepay.yop.sdk.exception.YopTracedException;
import com.yeepay.yop.sdk.utils.YopTraceUtils;

import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

import static com.yeepay.yop.sdk.constants.CharacterConstants.*;
import static com.yeepay.yop.sdk.constants.ExceptionConstants.SDK_INVOKE_IO_EXCEPTION_PREFIX;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2025/2/24
 */
public class YopIOException extends YopHttpException implements YopTracedException {

    private static final String CONNECTION_REFUSED = "Connection refused";
    private static final String CONNECTION_TIMEOUT = "Connection timed out";
    private static final String CONNECTION_TIMEOUT2 = "connect timed out";
    private static final String CONNECTION_TIMEOUT3 = "Timeout waiting for connection from pool";
    private static final String READ_TIMEOUT = "Read timed out";
    private static final String CONNECTION_RESET = "Connection reset";
    private static final String NO_ROUTE_TO_HOST = "No route to host";

    private final IOExceptionEnum ioException;
    private final String errorCode;
    private final String errorMsg;
    private final String requestId;
    private final boolean isRetryable;

    public YopIOException(String message) {
        super(message);
        this.requestId = YopTraceUtils.getCurrentRequestId();
        this.ioException = parse(message, null);
        this.errorCode = SDK_INVOKE_IO_EXCEPTION_PREFIX + DOT + ioException.getCode();
        this.errorMsg = EMPTY;
        this.isRetryable = ioException.isRetryable();
    }

    public YopIOException(String message, IOExceptionEnum ioException) {
        super(message);
        this.requestId = YopTraceUtils.getCurrentRequestId();
        this.ioException = ioException;
        this.errorCode = SDK_INVOKE_IO_EXCEPTION_PREFIX + DOT + ioException.getCode();
        this.errorMsg = EMPTY;
        this.isRetryable = ioException.isRetryable();
    }

    public YopIOException(String message, Throwable cause) {
        super(message, cause);
        this.requestId = YopTraceUtils.getCurrentRequestId();
        this.ioException = parse(message, cause);
        this.errorCode = SDK_INVOKE_IO_EXCEPTION_PREFIX + DOT + ioException.getCode();
        this.errorMsg = EMPTY;
        this.isRetryable = ioException.isRetryable();
    }

    public YopIOException(String message, Throwable cause, IOExceptionEnum ioException) {
        super(message, cause);
        this.requestId = YopTraceUtils.getCurrentRequestId();
        this.ioException = ioException;
        this.errorCode = SDK_INVOKE_IO_EXCEPTION_PREFIX + DOT + ioException.getCode();
        this.errorMsg = EMPTY;
        this.isRetryable = ioException.isRetryable();
    }

    public YopIOException(String message, Throwable cause, String errorMsg) {
        super(message, cause);
        this.requestId = YopTraceUtils.getCurrentRequestId();
        this.ioException = parse(message, cause);
        this.errorCode = SDK_INVOKE_IO_EXCEPTION_PREFIX + DOT + ioException.getCode();
        this.errorMsg = errorMsg;
        this.isRetryable = ioException.isRetryable();
    }

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getRequestId() {
        return this.requestId;
    }

    @Override
    public String getMessage() {
        String exMsg = this.errorMsg;
        if (isEmpty(exMsg)) {
            exMsg = super.getMessage();
        } else if (!isEmpty(super.getMessage())) {
            exMsg = exMsg + COMMA + super.getMessage();
        }
        return getErrorCode() + COMMA + getRequestId() + COMMA + exMsg;
    }

    public IOExceptionEnum getIoException() {
        return this.ioException;
    }

    public boolean isRetryable() {
        return this.isRetryable;
    }

    private IOExceptionEnum parse(String message, Throwable cause) {
        IOExceptionEnum ioException = IOExceptionEnum.UNKNOWN;
        if (isEmpty(message) && null == cause) {
            return ioException;
        }
        if (cause instanceof UnknownHostException) {
            return IOExceptionEnum.UNKNOWN_HOST;
        }

        if (cause instanceof NoRouteToHostException) {
            return IOExceptionEnum.NO_ROUTE_TO_HOST;
        }

        if (isEmpty(message) && null != cause.getMessage()) {
            message = cause.getMessage();
        }

        if (isEmpty(message)) {
            return ioException;
        }

        if (message.contains(CONNECTION_REFUSED)) {
            ioException = IOExceptionEnum.CONNECTION_REFUSED;
        } else if (message.contains(CONNECTION_TIMEOUT)
                || message.contains(CONNECTION_TIMEOUT2)
                || message.contains(CONNECTION_TIMEOUT3)) {
            ioException = IOExceptionEnum.CONNECTION_TIMEOUT;
        } else if (message.contains(READ_TIMEOUT)) {
            ioException = IOExceptionEnum.READ_TIMEOUT;
        } else if (message.contains(CONNECTION_RESET)) {
            ioException = IOExceptionEnum.CONNECTION_RESET;
        } else if (message.contains(NO_ROUTE_TO_HOST)) {
            ioException = IOExceptionEnum.NO_ROUTE_TO_HOST;
        }
        return ioException;
    }

    private static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public enum IOExceptionEnum {

        UNKNOWN_HOST("unknown-host", true),
        CONNECTION_TIMEOUT("connection-timeout", true),
        READ_TIMEOUT("read-timeout"),
        CONNECTION_RESET("connection-reset"),
        CONNECTION_REFUSED("connection-refused", true),
        NO_ROUTE_TO_HOST("no-route-to-host", true),
        UNKNOWN("unknown");

        private final String code;
        private final boolean isRetryable;

        private IOExceptionEnum(String code) {
            this.code = code;
            this.isRetryable = false;
        }

        IOExceptionEnum(String code, boolean isRetryable) {
            this.code = code;
            this.isRetryable = isRetryable;
        }

        public String getCode() {
            return code;
        }

        public boolean isRetryable() {
            return isRetryable;
        }
    }

}
