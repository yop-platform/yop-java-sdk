/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.exception.YopClientException;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/29 3:43 下午
 */
public class Sm4Utils {
    public static final String ALGORITHM_NAME = "SM4";
    public static final String ALGORITHM_NAME_GCM_NOPADDING = "SM4/GCM/NoPadding";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * SM4算法目前只支持128位（即密钥16字节）
     */
    public static final int DEFAULT_KEY_SIZE = 128;

    public static byte[] generateKey() {
        try {
            return generateKey(DEFAULT_KEY_SIZE);
        } catch (Exception e) {
            throw new YopClientException("error happens when generate sm4 key");
        }

    }

    public static byte[] generateKey(int keySize) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        kg.init(keySize, new SecureRandom());
        return kg.generateKey().getEncoded();
    }

    public static byte[] decrypt_GCM_NoPadding(byte[] key, String associatedData, String nonce, String ciphertext)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM_NAME_GCM_NOPADDING, BouncyCastleProvider.PROVIDER_NAME);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM_NAME);
        byte[] nonceBytes = null != nonce ? Encodes.decodeBase64(nonce) : new byte[12];
        GCMParameterSpec spec = new GCMParameterSpec(DEFAULT_KEY_SIZE, nonceBytes);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec);
        if (StringUtils.isNotBlank(associatedData)) {
            cipher.updateAAD(associatedData.getBytes());
        }
        return cipher.doFinal(Encodes.decodeBase64(ciphertext));
    }
}
