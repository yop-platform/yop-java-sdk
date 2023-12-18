/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.exception;

/**
 * title: YOP熔断异常<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/9
 */
public class YopBlockException extends YopHostException {
    private static final long serialVersionUID = -1L;

    public YopBlockException(String message) {
        super(message);
    }

    public YopBlockException(String message, Throwable cause) {
        super(message, cause);
    }
}
