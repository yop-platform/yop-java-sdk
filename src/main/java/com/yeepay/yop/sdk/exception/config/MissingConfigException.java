package com.yeepay.yop.sdk.exception.config;

/**
 * title: 配置丢失<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/8/17 16:12
 */
public class MissingConfigException extends AbstractSDKConfigException {

    private static final long serialVersionUID = -1L;


    public MissingConfigException(String field, String message) {
        super("miss", field, message);
    }

    public MissingConfigException(String field, String message, Throwable cause) {
        super("miss", field, message, cause);
    }
}
