/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.inter.security.encrypt;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.*;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptorAdaptor;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.inter.utils.RSA;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.utils.Encodes;
import com.yeepay.yop.sdk.utils.RandomUtils;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

/**
 * title: 非对称加解密器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/12
 */
public class YopRsaEncryptor extends YopEncryptorAdaptor {

    private static final String ENCRYPT_ALG = "RSA";

    @Override
    public List<String> supportedAlgs() {
        return Collections.singletonList(ENCRYPT_ALG);
    }

    @Override
    public EncryptOptions doInitEncryptOptions(String encryptAlg) throws Exception {
        final KeyPair keyPair = generateRandomKey();
        return new EncryptOptions(
                keyPair.getPublic(),
                ENCRYPT_ALG,
                encryptAlg,
                Encodes.encodeUrlSafeBase64(RandomUtils.secureRandom().generateSeed(16)),
                Encodes.encodeUrlSafeBase64("yop".getBytes(YopConstants.DEFAULT_ENCODING)));
    }

    private KeyPair generateRandomKey() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(ENCRYPT_ALG);
        kpg.initialize(2048);
        return kpg.genKeyPair();
    }

    @Override
    public byte[] encrypt(byte[] plain, EncryptOptions options) {
        try {
            final CredentialsItem credential = ((YopPlatformCredentials) options.getCredentials()).getCredential();
            if (credential instanceof CredentialsCollection) {
                // 约定取第一个
                return RSA.encrypt(plain, ((PKICredentialsItem) ((CredentialsCollection) credential).getItems().get(0)).getPublicKey());
            }
            return RSA.encrypt(plain, ((PKICredentialsItem) credential) .getPublicKey());
        } catch (Throwable e) {
            throw new YopClientException("error happened when encrypt with RSA alg", e);
        }
    }

    @Override
    public InputStream encrypt(InputStream plain, EncryptOptions options) {
        // 暂无需求
        return null;
    }

    @Override
    public byte[] decrypt(byte[] cipher, EncryptOptions options) {
        try {
            return RSA.decrypt(cipher, ((YopPKICredentials) options.getCredentials()).getCredential().getPrivateKey());
        } catch (Throwable e) {
            throw new YopClientException("error happened when decrypt with RSA alg", e);
        }
    }

    @Override
    public InputStream decrypt(InputStream cipher, EncryptOptions options) {
        // 暂无需求
        return null;
    }
}
