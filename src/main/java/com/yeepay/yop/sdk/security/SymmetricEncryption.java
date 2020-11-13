package com.yeepay.yop.sdk.security;

/**
 * title: 对称加密<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 16/11/24 下午2:32
 */
public interface SymmetricEncryption {

    /**
     * 随机生成对称密钥
     *
     * @return byte[]
     */
    byte[] generateRandomKey();

    /**
     * 加密
     *
     * @param plainText 明文
     * @param key       对称密钥
     * @return byte[]
     */
    byte[] encrypt(byte[] plainText, byte[] key);

    /**
     * 解密
     *
     * @param cipherText 密文
     * @param key        对称密钥
     * @return byte[]
     */
    byte[] decrypt(byte[] cipherText, byte[] key);

}
