/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.exception;

import static com.yeepay.yop.sdk.constants.CharacterConstants.COMMA;
import static com.yeepay.yop.sdk.constants.CharacterConstants.EMPTY;
import static com.yeepay.yop.sdk.constants.ExceptionConstants.SDK_INVOKE_HOST_BLOCKED_EXCEPTION;

/**
 * title: Yop域名熔断异常<br>
 * description: 该异常会触发当笔切换dns重试<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/27
 */
public class YopHostBlockException extends YopBlockException implements YopTracedException {
    private static final long serialVersionUID = -1L;

    public YopHostBlockException(String message) {
        super(message);
    }

    public YopHostBlockException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getErrorCode() {
        return SDK_INVOKE_HOST_BLOCKED_EXCEPTION;
    }

    @Override
    public String getRequestId() {
        // 熔断异常，请求标识无意义
        return EMPTY;
    }

    @Override
    public String getMessage() {
        return getErrorCode() + COMMA + super.getMessage();
    }
}
