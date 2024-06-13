/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    /**
     * 随机散列列表
     *
     * @param origin 原列表
     * @param <T>    元素
     * @return List
     */
    public static <T> List<T> randomList(List<T> origin) {
        List<T> tmp = new ArrayList<>(origin);
        Collections.shuffle(tmp, secureRandom());
        return tmp;
    }

    /**
     * 随机选一个
     *
     * @param origin 原列表
     * @param <T>    列表元素
     * @return T
     */
    public static <T> T randomOne(List<T> origin) {
        return origin.get(secureRandom().nextInt(origin.size()));
    }

    public static boolean randomFailure(int configThreshold) {
        if (configThreshold <= 0) {
            return false;
        }
        if (configThreshold >= 10000) {
            return true;
        }
        return RandomUtils.secureRandom().nextInt(10000) <= configThreshold;
    }

}
