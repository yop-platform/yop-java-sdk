/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.exception;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2025/2/24
 */
public interface YopTracedException {

    /**
     * 获取错误码
     *
     * @return String
     */
    String getErrorCode();

    /**
     * 获取请求标识
     *
     * @return String
     */
    String getRequestId();


}
