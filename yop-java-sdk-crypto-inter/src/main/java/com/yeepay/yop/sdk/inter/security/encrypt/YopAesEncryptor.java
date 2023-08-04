package com.yeepay.yop.sdk.inter.security.encrypt;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.encrypt.BigParamEncryptMode;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptorAdaptor;
import com.yeepay.yop.sdk.utils.Encodes;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

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

    private static final String AES_ALG = "AES";

    /**
     * AES算法
     */
    private static final String AES_CBC_PCK_ALG = "AES/CBC/PKCS5Padding";

    private static final byte[] AES_IV = initIv(AES_CBC_PCK_ALG);

    private static final ThreadLocal<Map<String, Cipher>> cipherThreadLocal = new ThreadLocal<Map<String, Cipher>>() {
        @Override
        protected Map<String, Cipher> initialValue() {
            Map<String, Cipher> map = Maps.newHashMap();
            try {
                map.put(AES_CBC_PCK_ALG, Cipher.getInstance(AES_CBC_PCK_ALG));
                map.put(AES_ALG, Cipher.getInstance(AES_ALG));
            } catch (Exception e) {
                throw new YopClientException("SystemError, InitCipher Fail, ex:", e);
            }
            return map;
        }
    };

    @Override
    public List<String> supportedAlgs() {
        return Lists.newArrayList(AES_ALG, AES_CBC_PCK_ALG);
    }

    @Override
    public EncryptOptions doInitEncryptOptions(String encryptAlg) throws Exception {
        return new EncryptOptions(
                Encodes.encodeUrlSafeBase64(generateRandomKey()),
                "RSA",
                encryptAlg,
                Encodes.encodeUrlSafeBase64(AES_IV),
                Encodes.encodeUrlSafeBase64("yop".getBytes(YopConstants.DEFAULT_ENCODING)));
    }

    private byte[] generateRandomKey() throws NoSuchAlgorithmException {
        //实例化
        KeyGenerator generator = KeyGenerator.getInstance(AES_ALG);
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
            throw new YopClientException("SystemError, Encrypt Fail, options:" + options + ", ex:", t);
        }
    }

    @Override
    public InputStream encrypt(InputStream plain, EncryptOptions options) {
        // TODO 支持chunked加密
        if (BigParamEncryptMode.chunked.equals(options.getBigParamEncryptMode())) {
            throw new YopClientException("SystemError, Encrypt Chunked NotSupport, options:" + options);
        }
        return new CipherInputStream(plain, getInitializedCipher(Cipher.ENCRYPT_MODE, options, false));
    }

    @Override
    public byte[] decrypt(byte[] cipher, EncryptOptions options) {
        try {
            Cipher initializedCipher = getInitializedCipher(Cipher.DECRYPT_MODE, options);
            return initializedCipher.doFinal(cipher);
        } catch (Throwable t) {
            throw new YopClientException("SystemError, Decrypt Fail, options:" + options + ", ex:", t);
        }
    }

    @Override
    public InputStream decrypt(InputStream cipher, EncryptOptions options) {
        // TODO 支持chunked加密
        if (BigParamEncryptMode.chunked.equals(options.getBigParamEncryptMode())) {
            throw new YopClientException("SystemError, Decrypt Chunked NotSupport, options:" + options);
        }
        return new CipherInputStream(cipher, getInitializedCipher(Cipher.DECRYPT_MODE, options, false));
    }

    /**
     * 初始向量的方法, 全部为0. 这里的写法适合于其它算法,针对AES算法的话,IV值一定是128位的(16字节).
     *
     * @param fullAlg 算法
     * @return byte[]
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

    private Cipher getInitializedCipher(int mode, EncryptOptions encryptOptions) {
        return getInitializedCipher(mode, encryptOptions, true);
    }

    private Cipher getInitializedCipher(int mode, EncryptOptions encryptOptions, boolean shareMode) {
        try {
            byte[] key = Encodes.decodeBase64(((YopSymmetricCredentials) encryptOptions.getCredentials()).getCredential());
            Cipher cipher = shareMode ? cipherThreadLocal.get().get(encryptOptions.getAlg()) :
                    Cipher.getInstance(encryptOptions.getAlg());
            Key secretKey = new SecretKeySpec(key, AES_ALG);
            if (StringUtils.isNotEmpty(encryptOptions.getIv())) {
                byte[] ivBytes = Encodes.decodeBase64(encryptOptions.getIv());
                IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
                cipher.init(mode, secretKey, ivParameterSpec);
                return cipher;
            }
            cipher.init(mode, secretKey);
            return cipher;
        } catch (Throwable throwable) {
            throw new YopClientException("error happened when initialize cipher", throwable);
        }
    }
}
