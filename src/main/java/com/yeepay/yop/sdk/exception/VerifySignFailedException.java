package com.yeepay.yop.sdk.exception;

/**
 * title: 验证签名失败<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 16/11/22 下午2:36
 */
public class VerifySignFailedException extends YopClientBizException {

    private static final long serialVersionUID = -1L;

    public VerifySignFailedException() {
        this("");
    }

    public VerifySignFailedException(String message) {
        super("isv.response.sign.verify-failure", message);
    }

    public VerifySignFailedException(String message, Throwable cause) {
        super("isv.response.sign.verify-failure", message, cause);
    }
}
