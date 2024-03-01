/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.encrypt;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProvider;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.utils.Encodes;
import org.apache.commons.lang3.StringUtils;

import static com.yeepay.yop.sdk.YopConstants.YOP_CREDENTIALS_ENCRYPT_ALG_SM2;
import static com.yeepay.yop.sdk.YopConstants.YOP_ENCRYPT_OPTIONS_YOP_PLATFORM_CERT_SERIAL_NO;
import static com.yeepay.yop.sdk.utils.ClientUtils.getCurrentPlatformCredentialsProvider;

/**
 * title: sm2增强会话密钥<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/27
 */
public class Sm2Enhancer extends AbstractEncryptOptionsEnhancer {
    private final String provider;
    private final String env;
    private final String appKey;
    private final String serialNo;
    private final String serverRoot;

    public Sm2Enhancer(String appKey) {
        this.appKey = appKey;
        this.serialNo = "";
        this.serverRoot = "";
        this.provider = YopConstants.YOP_DEFAULT_PROVIDER;
        this.env = YopConstants.YOP_DEFAULT_ENV;
    }

    public Sm2Enhancer(String appKey, String serialNo) {
        this.appKey = appKey;
        this.serialNo = serialNo;
        this.serverRoot = "";
        this.provider = YopConstants.YOP_DEFAULT_PROVIDER;
        this.env = YopConstants.YOP_DEFAULT_ENV;
    }

    public Sm2Enhancer(String appKey, String serialNo, String serverRoot) {
        this.appKey = appKey;
        this.serialNo = serialNo;
        this.serverRoot = serverRoot;
        this.provider = YopConstants.YOP_DEFAULT_PROVIDER;
        this.env = YopConstants.YOP_DEFAULT_ENV;
    }

    public Sm2Enhancer(String provider, String env, String appKey, String serialNo, String serverRoot) {
        this.appKey = appKey;
        this.serialNo = serialNo;
        this.serverRoot = serverRoot;
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
        final YopPlatformCredentialsProvider platformCredentialsProvider = getCurrentPlatformCredentialsProvider();
        final YopPlatformCredentials platformCredentials;
        if (StringUtils.isNotBlank(serialNo)) {
            platformCredentials = platformCredentialsProvider.getCredentials(provider, env, appKey, serialNo, serverRoot);
        } else {
            platformCredentials = platformCredentialsProvider
                    .getLatestCredentials(provider, env, appKey, CertTypeEnum.SM2.getValue(), serverRoot);
        }
        if (null == platformCredentials) {
            throw new YopClientException("ConfigProblem, YopPlatformCredentials NotFound to Enhance EncryptOptions, appKey:"
                    + appKey + ", serialNo:" + serialNo);
        }
        source.setEncryptedCredentials(YopEncryptorFactory.getEncryptor(YOP_CREDENTIALS_ENCRYPT_ALG_SM2).encryptToBase64(credentialBytes,
                new EncryptOptions(platformCredentials)));
        source.setCredentialsAlg(YOP_CREDENTIALS_ENCRYPT_ALG_SM2);
        source.setCredentials(new YopSymmetricCredentials(appKey, credentialStr));
        source.enhance(YOP_ENCRYPT_OPTIONS_YOP_PLATFORM_CERT_SERIAL_NO, platformCredentials.getSerialNo());
        return source;
    }
}
