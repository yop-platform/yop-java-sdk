/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2021/5/6 17:46
 */
public final class RandomUtils {

    private RandomUtils() {
        // do nothing
    }

    /**
     * 返回无锁的ThreadLocalRandom
     */
    public static Random threadLocalRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 使用性能更好的SHA1PRNG, Tomcat的sessionId生成也用此算法.
     * 但JDK7中，需要在启动参数加入 -Djava.security=file:/dev/./urandom
     */
    public static SecureRandom secureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {// NOSONAR
            return new SecureRandom();
        }
    }

}
