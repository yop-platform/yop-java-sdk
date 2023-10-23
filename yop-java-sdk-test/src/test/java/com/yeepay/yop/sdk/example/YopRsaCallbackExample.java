/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.example;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * title: rsa回调处理<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/10/23
 */
public class YopRsaCallbackExample {

    private static final String UTF_8 = "UTF-8";
    private static final String SEPARATOR = "$";
    private static final String AES_ALG = "AES";

    private static final String RSA = "RSA";
    private static final String DIGEST_ALG = "SHA256";

    private static String RSA_ECB_PKCS1PADDING = "RSA/ECB/PKCS1Padding";

    public static void main(String[] args) throws Exception {
        String appKey = "你的应用";
        String cipherText ="通知密文";
        String privateKeyStr = "商户私钥";
        String publicKeyStr = "易宝公钥";

        // 此处借用
        final PrivateKey privateKey = string2PrivateKey(privateKeyStr);
        final PublicKey publicKey = string2PublicKey(publicKeyStr);

        System.out.println(decrypt(appKey, cipherText, privateKey, publicKey));
    }

    private static PrivateKey string2PrivateKey(String priKey) {
        try {
            return KeyFactory.getInstance(RSA).generatePrivate(
                    new PKCS8EncodedKeySpec(decodeBase64(priKey)));
        } catch (Exception e) {
            throw new RuntimeException("ConfigProblem, IsvPrivateKey ParseFail, value:" + priKey + ", ex:", e);
        }
    }

    public static PublicKey string2PublicKey(String pubKey) {
        try {
            return KeyFactory.getInstance(RSA).generatePublic(
                    new X509EncodedKeySpec(decodeBase64(pubKey)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("ConfigProblem, YopPublicKey ParseFail, value:" + pubKey + ", ex:", e);
        }
    }

    private static byte[] decodeBase64(String input) {
        return Base64.decodeBase64(input);
    }

    private static String decrypt(String appKey, String cipherText, PrivateKey privateKey, PublicKey publicKey) throws Exception {
        //分解参数
        String[] args = cipherText.split("\\" + SEPARATOR);
        if (args.length != 4) {
            throw new RuntimeException("source invalid : " + cipherText);
        }
        String encryptedRandomKeyToBase64 = args[0];
        String encryptedDataToBase64 = args[1];
        String symmetricEncryptAlg = args[2];
        String digestAlg = args[3];
        assert AES_ALG.equals(symmetricEncryptAlg);
        assert DIGEST_ALG.equals(digestAlg);

        //用私钥对随机密钥进行解密
        byte[] decryptedRandomKey = rsaDecrypt(decodeBase64(encryptedRandomKeyToBase64), privateKey);

        //用随机对称密钥，解密得到源数据
        byte[] decryptedData = aesDecrypt(decodeBase64(encryptedDataToBase64), decryptedRandomKey);

        //分解参数
        String data = new String(decryptedData, UTF_8);
        String sourceData = StringUtils.substringBeforeLast(data, SEPARATOR);
        String signToBase64 = StringUtils.substringAfterLast(data, SEPARATOR);

        // 验签
        if (verifySign(sourceData.getBytes(UTF_8), decodeBase64(signToBase64), publicKey)) {
            throw new RuntimeException("verify sign fail, key:" + publicKey);
        }

        //返回源数据
        return sourceData;
    }

    private static boolean verifySign(byte[] sourceData, byte[] sign, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(sourceData);
            return signature.verify(sign);
        } catch (Exception e) {
            throw new RuntimeException("verify sign fail, key:" + publicKey);
        }
    }

    /**
     * aes解密
     * @param data 数据
     * @param key 密钥
     */
    private static byte[] aesDecrypt(byte[] data, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALG);
        Key secretKey = new SecretKeySpec(key, AES_ALG);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    /**
     * rsa解密
     *
     * @param data 数据
     * @param key  密钥
     * @return byte[]
     */
    public static byte[] rsaDecrypt(byte[] data, Key key) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1PADDING);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("decrypt fail, key:" + key + "ex:", e);
        }
    }

}
