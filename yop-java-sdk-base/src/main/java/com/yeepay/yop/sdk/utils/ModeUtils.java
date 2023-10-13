/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.client.router.enums.ModeEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: 运行模式工具类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/10/11
 */
public class ModeUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModeUtils.class);

    private static final String SYSTEM_SDK_MODE_KEY = "yop.sdk.mode";
    private static final String SANDBOX_APP_ID_PREFIX = "sandbox_";

    private static final ModeEnum SYSTEM_MODE = parse(System.getProperty(SYSTEM_SDK_MODE_KEY));

    public static boolean isInSandbox() {
        return SYSTEM_MODE == ModeEnum.sandbox;
    }

    public static boolean isAppInSandbox(String appKey) {
        return isAppInSandbox(appKey, null);
    }

    public static boolean isAppInSandbox(String appKey, String requestConfigMode) {
        return (ModeEnum.sandbox == parse(requestConfigMode))
                || isInSandbox()
                || StringUtils.startsWith(appKey, SANDBOX_APP_ID_PREFIX);
    }

    public static ModeEnum parse(String mode) {
        try {
            return StringUtils.isNotBlank(mode) ? ModeEnum.valueOf(mode) : null;
        } catch (Exception e) {
            LOGGER.warn("Mode NotSupported, value:{}", mode);
            return null;
        }
    }
}
