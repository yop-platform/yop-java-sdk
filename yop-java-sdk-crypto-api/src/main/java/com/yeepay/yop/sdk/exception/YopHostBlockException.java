/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.exception;

import com.yeepay.yop.sdk.invoke.exceptions.BLockException;

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
public class YopHostBlockException extends YopHostException implements BLockException {
    private static final long serialVersionUID = -1L;

    public YopHostBlockException(String message) {
        super(message);
    }

    public YopHostBlockException(String message, Throwable cause) {
        super(message, cause);
    }
}
