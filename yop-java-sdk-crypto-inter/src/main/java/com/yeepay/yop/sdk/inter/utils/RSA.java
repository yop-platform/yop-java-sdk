package com.yeepay.yop.sdk.inter.utils;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.constants.ExceptionConstants;
import com.yeepay.yop.sdk.exception.YopClientBizException;
import com.yeepay.yop.sdk.exception.param.IllegalParamFormatException;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.utils.Encodes;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 16/11/27 下午9:13
 */
public class RSA {

    static final Map<DigestAlgEnum, String> SIGN_ALG_MAP = Maps.newHashMap();

    static {
        SIGN_ALG_MAP.put(DigestAlgEnum.SHA256, "SHA256withRSA");
    }

    /**
     * 验证签名
     *
     * @param data      数据
     * @param sign      签名
     * @param publicKey 公钥
     * @param digestAlg 签名算法
     * @return boolean
     */
    public static boolean verifySign(byte[] data, byte[] sign, PublicKey publicKey, DigestAlgEnum digestAlg) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALG_MAP.get(digestAlg));
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(sign);
        } catch (Exception e) {
            throw new IllegalParamFormatException("YopPublicKey", "SystemError, Sign Fail, pubKey:"
                    + (null != publicKey ? Base64.getEncoder().encodeToString(publicKey.getEncoded()) : null)
                    + ", digestAlg" + digestAlg + ", cause:" + e.getMessage(), e);
        }
    }

    /**
     * 验证签名
     *
     * @param data      数据
     * @param sign      签名
     * @param pubicKey  公钥
     * @param digestAlg 签名算法
     * @return boolean
     */
    public static boolean verifySign(String data, String sign, PublicKey pubicKey, DigestAlgEnum digestAlg) {
        byte[] dataByte = data.getBytes(Charsets.UTF_8);
        byte[] signByte = Encodes.decodeBase64(sign);
        return verifySign(dataByte, signByte, pubicKey, digestAlg);
    }

    /**
     * 签名
     *
     * @param data      数据
     * @param key       密钥
     * @param digestAlg 签名算法
     * @return byte[]
     */
    public static byte[] sign(byte[] data, PrivateKey key, DigestAlgEnum digestAlg) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALG_MAP.get(digestAlg));
            signature.initSign(key);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new YopClientBizException(ExceptionConstants.SDK_CONFIG_RUNTIME_DEPENDENCY,
                    "SystemError, Sign Fail, key:" + key + ", digestAlg" + digestAlg + ", cause:" + e.getMessage(), e);
        }
    }

    /**
     * 签名
     *
     * @param data      数据
     * @param key       密钥
     * @param digestAlg 签名算法
     * @return String
     */
    public static String sign(String data, PrivateKey key, DigestAlgEnum digestAlg) {
        return sign(data, key, digestAlg, null);
    }

    /**
     * 签名
     *
     * @param data      数据
     * @param key       密钥
     * @param digestAlg 签名算法
     * @return String
     */
    public static String sign(String data, PrivateKey key, DigestAlgEnum digestAlg, SignOptions options) {
        byte[] dataByte = data.getBytes(Charsets.UTF_8);
        if (null != options && !options.isUrlSafe()) {
            return Encodes.encodeBase64(sign(dataByte, key, digestAlg));
        }
        return Encodes.encodeUrlSafeBase64(sign(dataByte, key, digestAlg));
    }


    /**
     * 加密
     *
     * @param data 数据
     * @param key  密钥
     * @return byte[]
     */
    public static byte[] encrypt(byte[] data, Key key) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(YopConstants.RSA_ECB_PKCS1PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new YopClientBizException(ExceptionConstants.SDK_CONFIG_RUNTIME_DEPENDENCY,
                    "SystemError, Encrypt Fail, key:" + key + ", cause:" + e.getMessage(), e);
        }
    }

    /**
     * 加密
     *
     * @param data 数据
     * @param key  密钥
     * @return String
     */
    public static String encryptToBase64(String data, Key key) {
        try {
            return Encodes.encodeUrlSafeBase64(encrypt(data.getBytes(Charsets.UTF_8), key));
        } catch (Exception e) {
            throw new YopClientBizException(ExceptionConstants.SDK_CONFIG_RUNTIME_DEPENDENCY,
                    "SystemError, Encrypt Fail, data:" + data + ", key:" + key + ", cause:" + e.getMessage(), e);
        }
    }

    /**
     * 解密
     *
     * @param data 数据
     * @param key  密钥
     * @return byte[]
     */
    public static byte[] decrypt(byte[] data, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(YopConstants.RSA_ECB_PKCS1PADDING);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new YopClientBizException(ExceptionConstants.SDK_CONFIG_RUNTIME_DEPENDENCY,
                    "SystemError, Decrypt Fail, key:" + key + ", cause:" + e.getMessage(), e);
        }
    }

    /**
     * 解密
     *
     * @param data 数据
     * @param key  密钥
     * @return String
     */
    public static String decryptFromBase64(String data, Key key) {
        try {
            return new String(decrypt(Encodes.decodeBase64(data), key), Charsets.UTF_8);
        } catch (Exception e) {
            throw new YopClientBizException(ExceptionConstants.SDK_CONFIG_RUNTIME_DEPENDENCY,
                    "SystemError, Decrypt Fail, data:" + data + ", key:" + key + ", cause:" + e.getMessage(), e);
        }
    }
}
