package com.yeepay.yop.sdk.security.rsa;


import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.utils.Encodes;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 16/11/24 下午2:11
 */
public class RSAKeyUtils {

    private static final String RSA = "RSA";

    /**
     * string 转 java.security.PublicKey
     *
     * @param pubKey pubKey
     * @return PublicKey
     */
    public static PublicKey string2PublicKey(String pubKey) {
        try {
            return KeyFactory.getInstance(RSA).generatePublic(
                    new X509EncodedKeySpec(Encodes.decodeBase64(pubKey)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new YopClientException("No such algorithm.", e);
        }
    }

    /**
     * string 转java.security.PrivateKey
     *
     * @param priKey 私钥字符串
     * @return 私钥
     */
    public static PrivateKey string2PrivateKey(String priKey) {
        try {
            return KeyFactory.getInstance(RSA).generatePrivate(
                    new PKCS8EncodedKeySpec(Encodes.decodeBase64(priKey)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new YopClientException("No such algorithm.", e);
        }
    }

    public static String key2String(Key key) {
        return Encodes.encodeBase64(key.getEncoded());
    }

}
