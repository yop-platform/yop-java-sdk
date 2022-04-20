/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security.encrypt.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopSm4Credentials;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.encrypt.BigParamEncryptMode;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptionsEnhancer;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptorAdaptor;
import com.yeepay.yop.sdk.utils.Encodes;
import com.yeepay.yop.sdk.utils.RandomUtils;
import com.yeepay.yop.sdk.utils.Sm4Utils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.io.CipherInputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.yeepay.yop.sdk.YopConstants.SM4_CBC_PKCS5PADDING;

/**
 * title: 对称加密器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/12
 */
public class YopSm4Encryptor extends YopEncryptorAdaptor {

    public static final String SECRET_KEY_TYPE = "SM4";

    public static final int ENCRYPT_MODE = 1;
    public static final int DECRYPT_MODE = 2;

    private static final ThreadLocal<Map<String, Cipher>> cipherThreadLocal = new ThreadLocal<Map<String, Cipher>>() {
        @Override
        protected Map<String, Cipher> initialValue() {
            Map<String, Cipher> map = Maps.newHashMap();
            try {
                map.put(SM4_CBC_PKCS5PADDING, Cipher.getInstance(SM4_CBC_PKCS5PADDING, BouncyCastleProvider.PROVIDER_NAME));
            } catch (Exception e) {
                throw new YopClientException("error happened when initial with SM4 alg", e);
            }
            return map;
        }
    };

    private static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(2, 20,
            3, TimeUnit.MINUTES, Queues.newLinkedBlockingQueue(200),
            new ThreadFactoryBuilder().setNameFormat("yop-encryptor-task-%d").build(), new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public List<String> supportedAlgs() {
        return Arrays.asList(SM4_CBC_PKCS5PADDING);
    }

    @Override
    public Future<EncryptOptions> initOptions(String encryptAlg, List<EncryptOptionsEnhancer> enhancers) {
        return THREAD_POOL.submit(new InitOptionsTask(encryptAlg, enhancers));
    }

    @Override
    public byte[] encrypt(byte[] plain, EncryptOptions options) {
        try {
            Cipher initializedCipher = getInitializedCipher(ENCRYPT_MODE, options);
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
        return new CipherInputStream(plain, getInitializedCipher(ENCRYPT_MODE, options, false));
    }

    @Override
    public byte[] decrypt(byte[] cipher, EncryptOptions options) {
        try {
            Cipher initializedCipher = getInitializedCipher(DECRYPT_MODE, options);
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
        return new CipherInputStream(cipher, getInitializedCipher(DECRYPT_MODE, options, false));
    }

    private Cipher getInitializedCipher(int mode, EncryptOptions encryptOptions) {
        return getInitializedCipher(mode, encryptOptions, true);
    }

    private Cipher getInitializedCipher(int mode, EncryptOptions encryptOptions, boolean shareMode) {
        try {
            byte[] key = Encodes.decodeBase64(((YopSm4Credentials) encryptOptions.getCredentials()).getCredential());
            Cipher cipher = shareMode ? cipherThreadLocal.get().get(encryptOptions.getAlg()) :
                    Cipher.getInstance(encryptOptions.getAlg(), BouncyCastleProvider.PROVIDER_NAME);
            Key sm4Key = new SecretKeySpec(key, SECRET_KEY_TYPE);
            if (StringUtils.isNotEmpty(encryptOptions.getIv())) {
                byte[] ivBytes = Encodes.decodeBase64(encryptOptions.getIv());
                IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
                cipher.init(mode, sm4Key, ivParameterSpec);
                return cipher;
            }
            cipher.init(mode, sm4Key);
            return cipher;
        } catch (Throwable throwable) {
            throw new YopClientException("error happened when initialize cipher", throwable);
        }
    }

    private static class InitOptionsTask implements Callable<EncryptOptions> {

        private final String encryptAlg;
        private final List<EncryptOptionsEnhancer> enhancers;

        public InitOptionsTask(String encryptAlg, List<EncryptOptionsEnhancer> enhancers) {
            this.encryptAlg = encryptAlg;
            this.enhancers = enhancers;
        }

        @Override
        public EncryptOptions call() throws Exception {
            EncryptOptions inited = new EncryptOptions(
                    new YopSm4Credentials(Encodes.encodeUrlSafeBase64(Sm4Utils.generateKey())),
                    YopConstants.YOP_CREDENTIALS_DEFAULT_ENCRYPT_ALG,
                    encryptAlg,
                    Encodes.encodeUrlSafeBase64(RandomUtils.secureRandom().generateSeed(16)),
                    Encodes.encodeUrlSafeBase64("yop".getBytes(YopConstants.DEFAULT_ENCODING)));
            if (CollectionUtils.isNotEmpty(enhancers)) {
                for (EncryptOptionsEnhancer enhancer : enhancers) {
                    inited = enhancer.enhance(inited);
                }
            }
            return inited;
        }
    }
}
