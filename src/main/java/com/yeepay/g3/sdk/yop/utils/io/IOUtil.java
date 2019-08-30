package com.yeepay.g3.sdk.yop.utils.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * title: IOUtil<br/>
 * description: 自定义IOUtil工具类<br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author genyou.yue
 * @version 1.0
 * @since 2019/8/23 下午5:03
 */
public class IOUtil {

    public static byte[] toByteArray(InputStream input, long size) throws IOException {
        if (size > 2147483647L) {
            throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
        } else {
            return toByteArray(input, (int) size);
        }
    }

    public static byte[] toByteArray(InputStream input, int size) throws IOException {
        if (size < 0) {
            throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
        } else if (size == 0) {
            return new byte[0];
        } else {
            byte[] data = new byte[size];
            int offset;
            int readed;
            for (offset = 0; offset < size && (readed = input.read(data, offset, size - offset)) != -1; offset += readed) {

            }
            return subBytes(data, 0, offset);
        }
    }

    /**
     * 从一个byte[]数组中截取一部分
     *
     * @param src
     * @param begin
     * @param count
     * @return
     */
    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i = begin; i < begin + count; i++) bs[i - begin] = src[i];
        return bs;
    }
}
