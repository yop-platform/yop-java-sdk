package com.yeepay.g3.sdk.yop.utils;

import com.google.common.base.Charsets;
import com.yeepay.g3.sdk.yop.encrypt.*;
import com.yeepay.g3.sdk.yop.exception.VerifySignFailedException;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import org.apache.commons.lang3.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * title: 数字信封 Util<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 14/12/20 23:10
 */
public final class DigitalEnvelopeUtils {

    public static final String SEPERATOR = "$";

    /**
     * 封装数字信封
     *
     * @param digitalEnvelopeDTO 待加密内容
     * @param privateKey         自己生成的私钥，用于签名
     * @param publicKey          对方给的公钥，用于加密
     * @return DigitalEnvelopeDTO
     */
    public static DigitalEnvelopeDTO encrypt(DigitalEnvelopeDTO digitalEnvelopeDTO, PrivateKey privateKey, PublicKey publicKey) {
        String source = digitalEnvelopeDTO.getPlainText();
        byte[] data = source.getBytes(Charsets.UTF_8);

        SymmetricEncryptAlgEnum symmetricEncryptAlg = digitalEnvelopeDTO.getSymmetricEncryptAlg();
        SymmetricEncryption symmetricEncryption = SymmetricEncryptionFactory.getSymmetricEncryption(symmetricEncryptAlg);
        //生成随机密钥
        byte[] randomKey = symmetricEncryption.generateRandomKey();

        DigestAlgEnum digestAlg = digitalEnvelopeDTO.getDigestAlg();
        //对数据进行签名
        byte[] sign = RSA.sign(data, privateKey, digestAlg);
        String signToBase64 = Encodes.encodeUrlSafeBase64(sign);

        //使用随机密钥对数据和签名进行加密
        data = (source + SEPERATOR + signToBase64).getBytes(Charsets.UTF_8);
        byte[] encryptedData = symmetricEncryption.encrypt(data, randomKey);
        String encryptedDataToBase64 = Encodes.encodeUrlSafeBase64(encryptedData);

        //对密钥加密
        byte[] encryptedRandomKey = RSA.encrypt(randomKey, publicKey);
        String encryptedRandomKeyToBase64 = Encodes.encodeUrlSafeBase64(encryptedRandomKey);

        //把密文和签名进行打包
        String cipherText = encryptedRandomKeyToBase64 +
                SEPERATOR +
                encryptedDataToBase64 +
                SEPERATOR +
                symmetricEncryptAlg.getValue() +
                SEPERATOR +
                digestAlg.getValue();
        digitalEnvelopeDTO.setCipherText(cipherText);
        return digitalEnvelopeDTO;
    }

    /**
     * 拆开数字信封
     *
     * @param digitalEnvelopeDTO 待解密内容
     * @param privateKey         自己生成的私钥，用于解密
     * @param publicKey          对方给的公钥，用于签名
     * @return DigitalEnvelopeDTO
     */
    public static DigitalEnvelopeDTO decrypt(DigitalEnvelopeDTO digitalEnvelopeDTO, PrivateKey privateKey, PublicKey publicKey) {
        String source = digitalEnvelopeDTO.getCipherText();
        //分解参数
        String[] args = source.split("\\" + SEPERATOR);
        if (args.length != 4) {
            throw new YopClientException("source invalid : " + source);
        }
        String encryptedRandomKeyToBase64 = args[0];
        String encryptedDataToBase64 = args[1];
        SymmetricEncryptAlgEnum symmetricEncryptAlg = SymmetricEncryptAlgEnum.parse(args[2]);
        DigestAlgEnum digestAlg = DigestAlgEnum.parse(args[3]);

        digitalEnvelopeDTO.setSymmetricEncryptAlg(symmetricEncryptAlg);
        SymmetricEncryption symmetricEncryption = SymmetricEncryptionFactory.getSymmetricEncryption(symmetricEncryptAlg);
        digitalEnvelopeDTO.setDigestAlg(digestAlg);

        //用私钥对随机密钥进行解密
        byte[] randomKey = RSA.decrypt(Encodes.decodeBase64(encryptedRandomKeyToBase64), privateKey);

        //解密得到源数据
        byte[] encryptedData = symmetricEncryption.decrypt(Encodes.decodeBase64(encryptedDataToBase64), randomKey);

        //分解参数
        String data = new String(encryptedData, Charsets.UTF_8);
        String sourceData = StringUtils.substringBeforeLast(data, "$");
        String signToBase64 = StringUtils.substringAfterLast(data, "$");

        //验证签名
        boolean verifySign = RSA.verifySign(sourceData, signToBase64, publicKey, digestAlg);
        if (!verifySign) {
            throw new VerifySignFailedException("verifySign fail!");
        }

        digitalEnvelopeDTO.setPlainText(sourceData);
        //返回源数据
        return digitalEnvelopeDTO;
    }


    public static DigitalSignatureDTO sign(DigitalSignatureDTO digitalSignatureDTO, PrivateKey privateKey) {
        digitalSignatureDTO.setSignature(sign0(digitalSignatureDTO, privateKey));
        return digitalSignatureDTO;
    }

    public static DigitalSignatureDTO verify(DigitalSignatureDTO digitalSignatureDTO, PublicKey publicKey) {
        verify0(digitalSignatureDTO, publicKey);
        return digitalSignatureDTO;
    }

    public static String sign0(DigitalSignatureDTO digitalSignatureDTO, PrivateKey privateKey) {
        String source = digitalSignatureDTO.getPlainText();
        byte[] data = source.getBytes(Charsets.UTF_8);

        DigestAlgEnum digestAlg = digitalSignatureDTO.getDigestAlg();
        //对数据进行签名
        byte[] sign = RSA.sign(data, privateKey, digestAlg);
        String signToBase64 = Encodes.encodeUrlSafeBase64(sign);

        //把密文和签名进行打包
        return signToBase64 +
                SEPERATOR +
                digestAlg.getValue();
    }

    public static void verify0(DigitalSignatureDTO digitalSignatureDTO, PublicKey publicKey) {
        String signature = digitalSignatureDTO.getSignature();
        //分解参数
        String[] args = signature.split("\\" + SEPERATOR);
        if (args.length != 2) {
            throw new YopClientException("signature invalid : " + signature);
        }
        String signToBase64 = args[0];
        DigestAlgEnum digestAlg = DigestAlgEnum.parse(args[1]);
        digitalSignatureDTO.setDigestAlg(digestAlg);

        //验证签名
        boolean verifySign = RSA.verifySign(digitalSignatureDTO.getPlainText(), signToBase64, publicKey, digestAlg);
        if (!verifySign) {
            throw new VerifySignFailedException("verifySign fail!");
        }
    }

}
