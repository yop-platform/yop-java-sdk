/**
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.g3.sdk.yop.encrypt;

import com.yeepay.g3.sdk.yop.client.YopConstants;
import com.yeepay.g3.sdk.yop.utils.Assert;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

/**
 * AES加解密工具类
 *
 * @author：wang.bao junning.li
 * @since：2015年5月7日 下午4:05:44
 * @version:
 */
public class AESEncrypter {

    public static byte[] encrypt(byte[] data, byte[] key) {
        Assert.notNull(data, "data");
        Assert.notNull(key, "key");
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, YopConstants.ALG_AES);
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, YopConstants.ALG_AES);
            Cipher cipher = Cipher.getInstance(YopConstants.ALG_AES);// 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, seckey);// 初始化
            return cipher.doFinal(data); // 加密
        } catch (Exception e) {
            throw new RuntimeException("encrypt fail!", e);
        }
    }

    public static byte[] decrypt(byte[] data, byte[] key) {
        Assert.notNull(data, "data");
        Assert.notNull(key, "key");
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, YopConstants.ALG_AES);
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, YopConstants.ALG_AES);
            Cipher cipher = Cipher.getInstance(YopConstants.ALG_AES);// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, seckey);// 初始化
            return cipher.doFinal(data); // 加密
        } catch (Exception e) {
            throw new RuntimeException("decrypt fail!", e);
        }
    }

    public static String encrypt(String data, String key) {
        try {
            byte[] valueByte = encrypt(data.getBytes(YopConstants.ENCODING),
                    Base64.decode(key.getBytes(YopConstants.ENCODING)));
            return new String(Base64.encode(valueByte), YopConstants.ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encrypt fail!", e);
        }
    }

    public static String decrypt(String data, String key) {
        try {
            byte[] originalData = Base64.decode(data.getBytes());
            byte[] valueByte = decrypt(originalData,
                    Base64.decode(key.getBytes(YopConstants.ENCODING)));
            return new String(valueByte, YopConstants.ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("decrypt fail!", e);
        }
    }
}
