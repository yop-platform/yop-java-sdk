/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.encrypt;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.utils.Encodes;

import static com.yeepay.yop.sdk.YopConstants.YOP_CREDENTIALS_ENCRYPT_ALG_RSA;
import static com.yeepay.yop.sdk.YopConstants.YOP_ENCRYPT_OPTIONS_YOP_PLATFORM_CERT_SERIAL_NO;
import static com.yeepay.yop.sdk.utils.ClientUtils.getCurrentYopPlatformCredentialsProvider;

/**
 * title: RSA增强会话密钥<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/27
 */
public class RsaEnhancer extends AbstractEncryptOptionsEnhancer {
    private final String provider;
    private final String env;
    private final String appKey;

    public RsaEnhancer(String appKey) {
        this.appKey = appKey;
        this.provider = YopConstants.YOP_DEFAULT_PROVIDER;
        this.env = YopConstants.YOP_DEFAULT_ENV;
    }

    public RsaEnhancer(String provider, String env, String appKey) {
        this.appKey = appKey;
        this.provider = provider;
        this.env = env;
    }

    @Override
    public EncryptOptions enhance(EncryptOptions source) {
        if (!checkForEnhance(source)) {
            return source;
        }
        YopSymmetricCredentials sourceCredentials = (YopSymmetricCredentials) source.getCredentials();
        String credentialStr = sourceCredentials.getCredential();
        byte[] credentialBytes = Encodes.decodeBase64(credentialStr);
        final YopPlatformCredentials yopPlatformCredentials = getCurrentYopPlatformCredentialsProvider()
                .getLatestCredentials(provider, env, appKey, CertTypeEnum.RSA2048.getValue(), null);
        source.setEncryptedCredentials(YopEncryptorFactory.getEncryptor(YOP_CREDENTIALS_ENCRYPT_ALG_RSA).encryptToBase64(credentialBytes,
                new EncryptOptions(yopPlatformCredentials)));
        source.setCredentialsAlg(YOP_CREDENTIALS_ENCRYPT_ALG_RSA);
        source.setCredentials(new YopSymmetricCredentials(appKey, credentialStr));
        // ！！！RSA目前不传证书序列号，
        source.enhance(YOP_ENCRYPT_OPTIONS_YOP_PLATFORM_CERT_SERIAL_NO, "");
        return source;
    }
}
