/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security.rsa;

import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.security.Encryption;
import com.yeepay.yop.sdk.security.Signer;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/2/1 4:31 下午
 */
public class RSA2048 implements Encryption<KeyPair>, Signer {
    @Override
    public KeyPair generateRandomKey() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            return kpg.genKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new YopClientException("unsupported algorithm");
        }

    }

    @Override
    public byte[] encrypt(byte[] plainText, byte[] key) {
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(key));
            return RSA.encrypt(plainText, publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new YopClientException("No such algorithm.", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] cipherText, byte[] key) {
        try {
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(
                    new PKCS8EncodedKeySpec(key));
            return RSA.decrypt(cipherText, privateKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new YopClientException("No such algorithm.", e);
        }

    }

    @Override
    public byte[] sign(PrivateKey privateKey, byte[] plaintText) {
        return RSA.sign(plaintText, privateKey, DigestAlgEnum.SHA256);
    }

    @Override
    public boolean verifySign(PublicKey publicKey, byte[] plaintText, byte[] signature) {
        return RSA.verifySign(plaintText, signature, publicKey, DigestAlgEnum.SHA256);
    }
}
