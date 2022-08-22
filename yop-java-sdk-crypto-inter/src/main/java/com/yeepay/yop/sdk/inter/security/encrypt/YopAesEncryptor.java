package com.yeepay.yop.sdk.inter.security.encrypt;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptorAdaptor;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.encrypt.BigParamEncryptMode;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.utils.Encodes;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import static com.yeepay.yop.sdk.YopConstants.AES;
import static com.yeepay.yop.sdk.YopConstants.AES_ECB_PKCS5PADDING;

/**
 * title: AES加密器<br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-04-25 17:22
 */
public class YopAesEncryptor extends YopEncryptorAdaptor {

    private static final ThreadLocal<Map<String, Cipher>> cipherThreadLocal = new ThreadLocal<Map<String, Cipher>>() {
        @Override
        protected Map<String, Cipher> initialValue() {
            Map<String, Cipher> map = Maps.newHashMap();
            try {
                map.put(AES_ECB_PKCS5PADDING, Cipher.getInstance(AES_ECB_PKCS5PADDING));
                map.put(AES, Cipher.getInstance(AES));
            } catch (Exception e) {
                throw new YopClientException("error happened when initial with AES alg", e);
            }
            return map;
        }
    };

    @Override
    public List<String> supportedAlgs() {
        return Lists.newArrayList(AES, AES_ECB_PKCS5PADDING);
    }

    @Override
    public EncryptOptions doInitEncryptOptions(String encryptAlg) throws Exception {
        return new EncryptOptions(
                new YopSymmetricCredentials(Encodes.encodeUrlSafeBase64(generateRandomKey())),
                YopConstants.RSA,
                encryptAlg,
                null,
                null);
    }

    private byte[] generateRandomKey() throws NoSuchAlgorithmException {
        //实例化
        KeyGenerator generator = KeyGenerator.getInstance(AES);
        //设置密钥长度
        generator.init(128);
        //生成密钥
        return generator.generateKey().getEncoded();
    }

    @Override
    public byte[] encrypt(byte[] plain, EncryptOptions options) {
        try {
            Cipher initializedCipher = getInitializedCipher(Cipher.ENCRYPT_MODE, options);
            return initializedCipher.doFinal(plain);
        } catch (Throwable t) {
            throw new YopClientException("error happened when encrypt data", t);
        }
    }

    @Override
    public InputStream encrypt(InputStream plain, EncryptOptions options) {
        // TODO 支持chunked加密
        if (BigParamEncryptMode.chunked.equals(options.getBigParamEncryptMode())) {
            throw new YopClientException("chunked encrypt for files not supported now");
        }
        return new CipherInputStream(plain, getInitializedCipher(Cipher.ENCRYPT_MODE, options, false));
    }

    @Override
    public byte[] decrypt(byte[] cipher, EncryptOptions options) {
        try {
            Cipher initializedCipher = getInitializedCipher(Cipher.DECRYPT_MODE, options);
            return initializedCipher.doFinal(cipher);
        } catch (Throwable t) {
            throw new YopClientException("error happened when decrypt data", t);
        }
    }

    @Override
    public InputStream decrypt(InputStream cipher, EncryptOptions options) {
        // TODO 支持chunked加密
        if (BigParamEncryptMode.chunked.equals(options.getBigParamEncryptMode())) {
            throw new YopClientException("chunked decrypt for files not supported now");
        }
        return new CipherInputStream(cipher, getInitializedCipher(Cipher.DECRYPT_MODE, options, false));
    }

    private Cipher getInitializedCipher(int mode, EncryptOptions encryptOptions) {
        return getInitializedCipher(mode, encryptOptions, true);
    }

    private Cipher getInitializedCipher(int mode, EncryptOptions encryptOptions, boolean shareMode) {
        try {
            byte[] key = Encodes.decodeBase64(((YopSymmetricCredentials) encryptOptions.getCredentials()).getCredential());
            Cipher cipher = shareMode ? cipherThreadLocal.get().get(encryptOptions.getAlg()) :
                    Cipher.getInstance(encryptOptions.getAlg());
            Key secretKey = new SecretKeySpec(key, AES);
            cipher.init(mode, secretKey);
            return cipher;
        } catch (Throwable throwable) {
            throw new YopClientException("error happened when initialize cipher", throwable);
        }
    }
}
