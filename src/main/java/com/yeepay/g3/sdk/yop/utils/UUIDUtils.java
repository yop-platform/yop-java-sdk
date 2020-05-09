/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.g3.sdk.yop.utils;

import org.apache.commons.codec.binary.Base64;

/**
 * title: UUID Ext<br>
 * description: 在生成的时候就不包括连接符<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2019/1/2 4:41 PM
 */
public final class UUIDUtils {

    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }

    /**
     * version 4 UUID(128bit)
     *
     * @return UUID
     */
    public static String randomV4UUID() {
        byte[] randomBytes = new byte[16];
        RandomUtils.threadLocalRandom().nextBytes(randomBytes);
        randomBytes[6] &= 0x0f;  /* clear version        */
        randomBytes[6] |= 0x40;  /* set to version 4     */
        randomBytes[8] &= 0x3f;  /* clear variant        */
        randomBytes[8] |= 0x80;  /* set to IETF variant  */

        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (randomBytes[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (randomBytes[i] & 0xff);

        return (digits(msb >> 32, 8) +
                digits(msb >> 16, 4) +
                digits(msb, 4) +
                digits(lsb >> 48, 4) +
                digits(lsb, 12));
    }

    /**
     * version 4 UUID(88bit, compress 31.25%, url safe: -,_)
     *
     * @return UUID
     */
    public static String compressV4UUID() {
        byte[] randomBytes = new byte[16];
        RandomUtils.threadLocalRandom().nextBytes(randomBytes);
        randomBytes[6] &= 0x0f;  /* clear version        */
        randomBytes[6] |= 0x40;  /* set to version 4     */
        randomBytes[8] &= 0x3f;  /* clear variant        */
        randomBytes[8] |= 0x80;  /* set to IETF variant  */

        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (randomBytes[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (randomBytes[i] & 0xff);

        byte[] b = new byte[16];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (msb >>> (8 * (7 - i)) & 0xff);
            b[i + 8] = (byte) (lsb >>> (8 * (7 - i)) & 0xff);
        }

        return Base64.encodeBase64URLSafeString(b);
    }

}
