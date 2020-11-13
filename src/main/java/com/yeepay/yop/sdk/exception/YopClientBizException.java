package com.yeepay.yop.sdk.exception;

/**
 * title: <br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/8/17 16:45
 */
public class YopClientBizException extends YopClientException {

    private static final long serialVersionUID = -1L;

    private final String errorCode;

    public YopClientBizException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public YopClientBizException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
