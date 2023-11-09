package com.yeepay.yop.sdk.exception.config;


import com.yeepay.yop.sdk.exception.YopClientBizException;

/**
 * title:sdk配置异常 <br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/8/17 15:58
 */
public abstract class AbstractSDKConfigException extends YopClientBizException {

    private static final long serialVersionUID = -1L;


    public AbstractSDKConfigException(String code, String field, String message) {
        super("sdk.sdk-config." + code + ":" + field, message);
    }

    public AbstractSDKConfigException(String code, String field, String message, Throwable cause) {
        super("sdk.sdk-config." + code + ":" + field, message, cause);
    }
}
