/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security.sm;

import com.yeepay.yop.sdk.security.Encryption;
import com.yeepay.yop.sdk.utils.Sm4Utils;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/29 3:46 下午
 */
public class SM4 implements Encryption<byte[]> {

    @Override
    public byte[] generateRandomKey() {
        return Sm4Utils.generateKey();
    }

    @Override
    public byte[] encrypt(byte[] plainText, byte[] key) {
        return Sm4Utils.encrypt_ECB_Padding(key, plainText);
    }

    @Override
    public byte[] decrypt(byte[] cipherText, byte[] key) {
        return Sm4Utils.decrypt_ECB_Padding(key, cipherText);
    }
}
