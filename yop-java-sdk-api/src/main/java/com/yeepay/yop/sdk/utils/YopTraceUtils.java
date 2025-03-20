/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.utils;

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
public class YopTraceUtils {

    // 当前线程上下文中的requestId
    private static ThreadLocal<String> CURRENT_REQUEST_ID = new ThreadLocal<>();

    public static String getCurrentRequestId() {
        return CURRENT_REQUEST_ID.get();
    }

    public static void setCurrentRequestId(String requestId) {
        CURRENT_REQUEST_ID.set(requestId);
    }

    public static void removeCurrentRequestId() {
        CURRENT_REQUEST_ID.remove();
    }
}
