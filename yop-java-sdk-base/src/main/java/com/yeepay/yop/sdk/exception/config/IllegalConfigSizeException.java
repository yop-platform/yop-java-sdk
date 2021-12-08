package com.yeepay.yop.sdk.exception.config;

/**
 * title: 非法格式异常<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/8/17 17:12
 */
public class IllegalConfigSizeException extends AbstractIllegalConfigException {

    private static final long serialVersionUID = -1L;

    public IllegalConfigSizeException(String field, String message) {
        super("format", field, message);
    }

    public IllegalConfigSizeException(String field, String message, Throwable cause) {
        super("format", field, message, cause);
    }
}
