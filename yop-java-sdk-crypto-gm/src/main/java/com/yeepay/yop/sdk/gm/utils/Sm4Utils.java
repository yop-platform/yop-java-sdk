/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.gm.utils;

import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.gm.base.utils.SmUtils;
import com.yeepay.yop.sdk.utils.Encodes;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.macs.GMac;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
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
        SmUtils.init();
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

    public static byte[] doCMac(byte[] key, byte[] data) throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidKeyException {
        Key keyObj = new SecretKeySpec(key, ALGORITHM_NAME);
        return doMac("SM4-CMAC", keyObj, data);
    }

    public static byte[] doGMac(byte[] key, byte[] iv, int tagLength, byte[] data) {
        org.bouncycastle.crypto.Mac mac = new GMac(new GCMBlockCipher(new SM4Engine()), tagLength * 8);
        return doMac(mac, key, iv, data);
    }

    /**
     * 默认使用PKCS7Padding/PKCS5Padding填充的CBCMAC
     *
     * @param key
     * @param iv
     * @param data
     * @return
     */
    public static byte[] doCBCMac(byte[] key, byte[] iv, byte[] data) {
        SM4Engine engine = new SM4Engine();
        org.bouncycastle.crypto.Mac mac = new CBCBlockCipherMac(engine, engine.getBlockSize() * 8, new PKCS7Padding());
        return doMac(mac, key, iv, data);
    }

    private static byte[] doMac(org.bouncycastle.crypto.Mac mac, byte[] key, byte[] iv, byte[] data) {
        CipherParameters cipherParameters = new KeyParameter(key);
        mac.init(new ParametersWithIV(cipherParameters, iv));
        mac.update(data, 0, data.length);
        byte[] result = new byte[mac.getMacSize()];
        mac.doFinal(result, 0);
        return result;
    }

    private static byte[] doMac(String algorithmName, Key key, byte[] data) throws NoSuchProviderException,
            NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        mac.init(key);
        mac.update(data);
        return mac.doFinal();
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
