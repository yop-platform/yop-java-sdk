/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.g3.sdk.yop.utils;

import org.apache.commons.lang3.Validate;

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
 * @since 2018/10/25 2:59 PM
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

    public static Random getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * 返回0到Intger.MAX_VALUE的随机Int, 使用ThreadLocalRandom.
     */
    public static int nextInt() {
        return nextInt(getRandom());
    }

    /**
     * 返回0到Intger.MAX_VALUE的随机Int, 可传入ThreadLocalRandom或SecureRandom
     */
    public static int nextInt(Random random) {
        int n = random.nextInt();
        if (n == Integer.MIN_VALUE) {
            n = 0; // corner case
        } else {
            n = Math.abs(n);
        }

        return n;
    }

    /**
     * 返回0到max的随机Int, 使用ThreadLocalRandom.
     */
    public static int nextInt(int max) {
        return nextInt(getRandom(), max);
    }

    /**
     * 返回0到max的随机Int, 可传入SecureRandom或ThreadLocalRandom
     */
    public static int nextInt(Random random, int max) {
        return random.nextInt(max);
    }

    /**
     * 返回min到max的随机Int, 使用ThreadLocalRandom.
     */
    public static int nextInt(int min, int max) {
        return nextInt(getRandom(), min, max);
    }

    /**
     * 返回min到max的随机Int,可传入SecureRandom或ThreadLocalRandom.
     * <p>
     * JDK本身不具有控制两端范围的nextInt，因此参考Commons Lang RandomUtils的实现, 不直接复用是因为要传入Random实例
     *
     * @see org.apache.commons.lang3.RandomUtils#nextInt(long, long)
     */
    public static int nextInt(Random random, int min, int max) {
        Validate.isTrue(max >= min, "Start value must be smaller or equal to end value.");
        if (min < 0) {
            min = 0;
        }
        if (min == max) {
            return min;
        }

        return min + random.nextInt(max - min);
    }

}
