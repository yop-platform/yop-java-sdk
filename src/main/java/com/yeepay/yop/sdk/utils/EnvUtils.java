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
 * @author dreambt
 * @version 1.0.0
 * @since 2021/5/6 19:19
 */
public final class EnvUtils {

    private static final String ENV_PROD = "prod";

    public static boolean isProd() {
        return ENV_PROD.equals(currentEnv());
    }

    public static String currentEnv() {
        return System.getProperty("yop.sdk.config.env", ENV_PROD);
    }

}
