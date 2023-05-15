package com.yeepay.yop.sdk.security.aes;

import com.yeepay.g3.core.yop.sdk.sample.YopConstants;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

/**
 * title: AES加密器<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2019<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-04-25 17:22
 */
public class AesEncryptor {

    private static final String AES_ALG = "AES";

    /**
     * AES算法
     */
    private static final String AES_CBC_PCK_ALG = "AES/CBC/PKCS5Padding";

    private static final byte[] AES_IV = initIv(AES_CBC_PCK_ALG);

    public static byte[] encrypt(byte[] data, byte[] key) {
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
            byte[] valueByte = encrypt(data.getBytes(YopConstants.DEFAULT_CHARSET),
                    Base64.decodeBase64(key.getBytes(YopConstants.DEFAULT_CHARSET)));
            return new String(Base64.encodeBase64(valueByte), YopConstants.DEFAULT_CHARSET);
        } catch (Throwable ex) {
            throw new RuntimeException("encrypt fail!", ex);
        }
    }

    public static String decrypt(String data, String key) {
        try {
            byte[] originalData = Base64.decodeBase64(data.getBytes(YopConstants.DEFAULT_CHARSET));
            byte[] valueByte = decrypt(originalData,
                    Base64.decodeBase64(key.getBytes(YopConstants.DEFAULT_CHARSET)));
            return new String(valueByte, YopConstants.DEFAULT_CHARSET);
        } catch (Throwable ex) {
            throw new RuntimeException("decrypt fail!", ex);
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
