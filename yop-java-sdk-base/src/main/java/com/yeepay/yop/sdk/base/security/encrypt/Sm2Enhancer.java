/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.encrypt;

import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProvider;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.utils.Encodes;
import org.apache.commons.lang3.StringUtils;

import static com.yeepay.yop.sdk.YopConstants.YOP_CREDENTIALS_ENCRYPT_ALG_SM2;
import static com.yeepay.yop.sdk.YopConstants.YOP_ENCRYPT_OPTIONS_YOP_SM2_CERT_SERIAL_NO;

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
    private final String appKey;
    private final String serialNo;

    public Sm2Enhancer(String appKey) {
        this.appKey = appKey;
        this.serialNo = "";
    }

    public Sm2Enhancer(String appKey, String serialNo) {
        this.appKey = appKey;
        this.serialNo = serialNo;
    }

    @Override
    public EncryptOptions enhance(EncryptOptions source) {
        if (!checkForEnhance(source)) {
            return source;
        }
        YopSymmetricCredentials sourceCredentials = (YopSymmetricCredentials) source.getCredentials();
        String credentialStr = sourceCredentials.getCredential();
        byte[] credentialBytes = Encodes.decodeBase64(credentialStr);
        final YopPlatformCredentialsProvider platformCredentialsProvider = YopPlatformCredentialsProviderRegistry.getProvider();
        final YopPlatformCredentials platformCredentials;
        if (StringUtils.isNotBlank(serialNo)) {
            platformCredentials = platformCredentialsProvider.getCredentials(appKey, serialNo);
        } else {
            platformCredentials = platformCredentialsProvider
                    .getLatestCredentials(appKey, CertTypeEnum.SM2.getValue());
        }
        if (null == platformCredentials) {
            throw new YopClientException("ConfigProblem, YopPlatformCredentials NotFound to Enhance EncryptOptions, appKey:"
                    + appKey + ", serialNo:" + serialNo);
        }
        source.setEncryptedCredentials(YopEncryptorFactory.getEncryptor(YOP_CREDENTIALS_ENCRYPT_ALG_SM2).encryptToBase64(credentialBytes,
                new EncryptOptions(platformCredentials)));
        source.setCredentialsAlg(YOP_CREDENTIALS_ENCRYPT_ALG_SM2);
        source.setCredentials(new YopSymmetricCredentials(appKey, credentialStr));
        source.enhance(YOP_ENCRYPT_OPTIONS_YOP_SM2_CERT_SERIAL_NO, platformCredentials.getSerialNo());
        return source;
    }
}
