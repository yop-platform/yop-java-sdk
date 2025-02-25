package com.yeepay.yop.sdk.exception;

import com.yeepay.yop.sdk.utils.YopTraceUtils;

import static com.yeepay.yop.sdk.constants.CharacterConstants.COMMA;

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
public class YopClientBizException extends YopClientException implements YopTracedException {

    private static final long serialVersionUID = -1L;

    private final String errorCode;
    private final String requestId;



    public YopClientBizException(String errorCode, String message) {
        super(message);
        this.requestId = YopTraceUtils.getCurrentRequestId();
        this.errorCode = errorCode;
    }

    public YopClientBizException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.requestId = YopTraceUtils.getCurrentRequestId();
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return getErrorCode() + COMMA + getRequestId() + COMMA + super.getMessage();
    }

    @Override
    public String getRequestId() {
        return requestId;
    }
}
