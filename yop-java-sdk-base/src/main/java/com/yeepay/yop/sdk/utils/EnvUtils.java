/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.utils;

import org.apache.commons.lang3.StringUtils;

import static com.yeepay.yop.sdk.YopConstants.ENV_PROD;
import static com.yeepay.yop.sdk.YopConstants.ENV_SANDBOX;

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
    private static final String ENV_SANDBOX_APP_PREFIX = "sandbox_";
    private static final String ENV_PROP = "yop.sdk.config.env";
    private static final String MODE_PROP = "yop.sdk.mode";
    private static final String MODE_SANDBOX = "sandbox";

    public static boolean isProd() {
        return ENV_PROD.equals(currentEnv());
    }

    public static boolean isSandBoxEnv(String env) {
        return ENV_SANDBOX.equals(env);
    }

    @Deprecated
    public static boolean isSandBoxMode() {
        return MODE_SANDBOX.equals(System.getProperty(MODE_PROP));
    }

    @Deprecated
    public static boolean isSandboxApp(String appKey) {
        return StringUtils.startsWith(appKey, ENV_SANDBOX_APP_PREFIX);
    }

    public static String currentEnv() {
        return System.getProperty(ENV_PROP, ENV_PROD);
    }

    public static String getCustomEnvProperty() {
        return System.getProperty(ENV_PROP);
    }

}
