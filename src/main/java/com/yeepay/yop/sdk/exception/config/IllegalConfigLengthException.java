package com.yeepay.yop.sdk.exception.config;

/**
 * title: 非法长度异常<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/8/17 17:12
 */
public class IllegalConfigLengthException extends AbstractIllegalConfigException {

    private static final long serialVersionUID = -1L;

    public IllegalConfigLengthException(String field, String message) {
        super("length", field, message);
    }

    public IllegalConfigLengthException(String field, String message, Throwable cause) {
        super("length", field, message, cause);
    }
}
