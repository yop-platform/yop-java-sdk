/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security.sm;

import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.Encryption;
import com.yeepay.yop.sdk.security.Signer;
import com.yeepay.yop.sdk.utils.Sm2Utils;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
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
 * @since 2021/1/29 3:58 下午
 */
public class SM2 implements Encryption<KeyPair>, Signer {
    @Override
    public KeyPair generateRandomKey() {
        return Sm2Utils.generateKeyPair();
    }

    @Override
    public byte[] encrypt(byte[] plainText, byte[] key) {
        try {
            X509EncodedKeySpec eks = new X509EncodedKeySpec(key);
            KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            PublicKey publicKey = kf.generatePublic(eks);
            return Sm2Utils.encrypt((BCECPublicKey) publicKey, plainText);
        } catch (Exception e) {
            throw new YopClientException(e.getMessage());
        }

    }

    @Override
    public byte[] decrypt(byte[] cipherText, byte[] key) {
        try {
            PKCS8EncodedKeySpec peks = new PKCS8EncodedKeySpec(key);
            KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            PrivateKey privateKey = kf.generatePrivate(peks);
            return Sm2Utils.decrypt((BCECPrivateKey) privateKey, cipherText);
        } catch (Exception e) {
            throw new YopClientException(e.getMessage());
        }
    }

    @Override
    public byte[] sign(PrivateKey privateKey, byte[] plaintText) {
        try {
            return Sm2Utils.sign((BCECPrivateKey) privateKey, plaintText);
        } catch (CryptoException e) {
            throw new YopClientException(e.getMessage());
        }
    }

    @Override
    public boolean verifySign(PublicKey publicKey, byte[] plaintText, byte[] signature) {
        return Sm2Utils.verify((BCECPublicKey) publicKey, plaintText, signature);
    }
}
