/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;

/**
 * title: 国密初始化类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class SmInitUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmInitUtils.class);

    static {
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            LOGGER.debug("BouncyCastleProvider added");
        } catch (Exception e) {
            LOGGER.warn("error when add BouncyCastleProvider", e);
        }
    }

    /**
     * 国密初始化
     */
    public static void init() {
        // already done when class loaded;
    }

}
