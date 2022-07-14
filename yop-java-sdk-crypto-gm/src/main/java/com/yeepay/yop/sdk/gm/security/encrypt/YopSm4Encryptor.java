/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.gm.security.encrypt;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.encrypt.BigParamEncryptMode;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptorAdaptor;
import com.yeepay.yop.sdk.utils.Encodes;
import com.yeepay.yop.sdk.utils.RandomUtils;
import com.yeepay.yop.sdk.gm.utils.Sm4Utils;
import com.yeepay.yop.sdk.gm.base.utils.SmUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.io.CipherInputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    // 暂时保留 商户通知用
    public static final String ALGORITHM_NAME_GCM_NOPADDING = "SM4/GCM/NoPadding";

    public static final int ENCRYPT_MODE = 1;
    public static final int DECRYPT_MODE = 2;

    static {
        SmUtils.init();
    }

    private static final ThreadLocal<Map<String, Cipher>> cipherThreadLocal = new ThreadLocal<Map<String, Cipher>>() {
        @Override
        protected Map<String, Cipher> initialValue() {
            Map<String, Cipher> map = Maps.newHashMap();
            try {
                map.put(SM4_CBC_PKCS5PADDING, Cipher.getInstance(SM4_CBC_PKCS5PADDING, BouncyCastleProvider.PROVIDER_NAME));
                map.put(ALGORITHM_NAME_GCM_NOPADDING, Cipher.getInstance(ALGORITHM_NAME_GCM_NOPADDING, BouncyCastleProvider.PROVIDER_NAME));
            } catch (Exception e) {
                throw new YopClientException("error happened when initial with SM4 alg", e);
            }
            return map;
        }
    };

    @Override
    public List<String> supportedAlgs() {
        return Arrays.asList(SM4_CBC_PKCS5PADDING, ALGORITHM_NAME_GCM_NOPADDING);
    }

    @Override
    public EncryptOptions doInitEncryptOptions(String encryptAlg) throws Exception {
        return new EncryptOptions(
                new YopSymmetricCredentials(Encodes.encodeUrlSafeBase64(Sm4Utils.generateKey())),
                YopConstants.YOP_CREDENTIALS_DEFAULT_ENCRYPT_ALG,
                encryptAlg,
                Encodes.encodeUrlSafeBase64(RandomUtils.secureRandom().generateSeed(16)),
                Encodes.encodeUrlSafeBase64("yop".getBytes(YopConstants.DEFAULT_ENCODING)));
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
            byte[] key = Encodes.decodeBase64(((YopSymmetricCredentials) encryptOptions.getCredentials()).getCredential());
            Cipher cipher = shareMode ? cipherThreadLocal.get().get(encryptOptions.getAlg()) :
                    Cipher.getInstance(encryptOptions.getAlg(), BouncyCastleProvider.PROVIDER_NAME);
            Key sm4Key = new SecretKeySpec(key, SECRET_KEY_TYPE);
            if (ALGORITHM_NAME_GCM_NOPADDING.equals(encryptOptions.getAlg())) {
                String nonce = encryptOptions.getIv();
                byte[] nonceBytes = null != nonce ? Encodes.decodeBase64(nonce) : new byte[12];
                GCMParameterSpec spec = new GCMParameterSpec(128, nonceBytes);
                cipher.init(mode, sm4Key, spec);
                if (StringUtils.isNotBlank(encryptOptions.getAad())) {
                    cipher.updateAAD(encryptOptions.getAad().getBytes(YopConstants.DEFAULT_ENCODING));
                }
                return cipher;
            }
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
}
