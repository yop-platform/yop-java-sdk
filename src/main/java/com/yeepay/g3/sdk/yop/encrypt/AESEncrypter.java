/**
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.g3.sdk.yop.encrypt;

import com.yeepay.g3.sdk.yop.client.YopConstants;
import com.yeepay.g3.sdk.yop.utils.Assert;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

/**
 * AES加解密工具类
 *
 * @author：wang.bao junning.li
 * @since：2015年5月7日 下午4:05:44
 * @version:
 */
public class AESEncrypter {

    private static final String AES_ALG = "AES";

    /**
     * AES算法
     */
    private static final String AES_CBC_PCK_ALG = "AES/CBC/PKCS5Padding";

    private static final byte[] AES_IV = initIv(AES_CBC_PCK_ALG);

    public static byte[] encrypt(byte[] data, byte[] key) {
        Assert.notNull(data, "data");
        Assert.notNull(key, "key");
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PCK_ALG);
            IvParameterSpec iv = new IvParameterSpec(AES_IV);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key,
                    AES_ALG), iv);

            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("encrypt fail!", e);
        }
    }

    public static byte[] decrypt(byte[] data, byte[] key) {
        Assert.notNull(data, "data");
        Assert.notNull(key, "key");
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PCK_ALG);
            IvParameterSpec iv = new IvParameterSpec(initIv(AES_CBC_PCK_ALG));
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key,
                    AES_ALG), iv);
            return cipher.doFinal(data);
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

    public static String decryptWithDefaultImpl(String data, String key) {
        try {
            byte[] originalData = Base64.decode(data.getBytes());
            byte[] valueByte = decryptWithDefaultImpl(originalData,
                    Base64.decode(key.getBytes(YopConstants.ENCODING)));
            return new String(valueByte, YopConstants.ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("decrypt fail!", e);
        }
    }

    public static byte[] decryptWithDefaultImpl(byte[] data, byte[] key) {
        Assert.notNull(data, "data");
        Assert.notNull(key, "key");
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, AES_ALG);
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec seckey = new SecretKeySpec(enCodeFormat, AES_ALG);
            Cipher cipher = Cipher.getInstance(AES_ALG);// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, seckey);// 初始化
            return cipher.doFinal(data); // 加密
        } catch (Exception e) {
            throw new RuntimeException("decrypt fail!", e);
        }
    }

    /**
     * 初始向量的方法, 全部为0. 这里的写法适合于其它算法,针对AES算法的话,IV值一定是128位的(16字节).
     *
     * @param fullAlg
     * @return
     * @throws GeneralSecurityException
     */
    private static byte[] initIv(String fullAlg) {

        try {
            Cipher cipher = Cipher.getInstance(fullAlg);
            int blockSize = cipher.getBlockSize();
            byte[] iv = new byte[blockSize];
            for (int i = 0; i < blockSize; ++i) {
                iv[i] = 0;
            }
            return iv;
        } catch (Exception e) {

            int blockSize = 16;
            byte[] iv = new byte[blockSize];
            for (int i = 0; i < blockSize; ++i) {
                iv[i] = 0;
            }
            return iv;
        }
    }
}
