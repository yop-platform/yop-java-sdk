package com.yeepay.yop.sdk.exception.config;

/**
 * title: 配置非法异常<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/8/17 16:14
 */
public abstract class AbstractIllegalConfigException extends AbstractSDKConfigException {

    private static final long serialVersionUID = -1L;

    public AbstractIllegalConfigException(String errorCode, String field, String message) {
        super(errorCode, field, message);
    }

    public AbstractIllegalConfigException(String errorCode, String field, String message, Throwable cause) {
        super(errorCode, field, message, cause);
    }
}
