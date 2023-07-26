/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report;

/**
 * title: 上报异常<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/20
 */
public class YopReportException extends RuntimeException {

    private static final long serialVersionUID = -1L;

    /**
     * Constructs a new YopReportException with the specified detail message.
     *
     * @param message the detail error message.
     */
    public YopReportException(String message) {
        super(message);
    }

    /**
     * Constructs a new YopReportException with the specified detail message and the underlying cause.
     *
     * @param message the detail error message.
     * @param cause   the underlying cause of this exception.
     */
    public YopReportException(String message, Throwable cause) {
        super(message, cause);
    }
}
