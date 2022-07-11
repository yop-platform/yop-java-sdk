/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.encrypt;

import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.utils.Encodes;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import static com.yeepay.yop.sdk.YopConstants.YOP_CREDENTIALS_ENCRYPT_ALG_SM4;
import static com.yeepay.yop.sdk.YopConstants.YOP_ENCRYPT_OPTIONS_YOP_SM4_MAIN_CREDENTIALS;

/**
 * title: sm4增强会话密钥<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/27
 */
public class Sm4Enhancer extends AbstractEncryptOptionsEnhancer {
    private final String appKey;

    public Sm4Enhancer(String appKey) {
        this.appKey = appKey;
    }

    @Override
    public EncryptOptions enhance(EncryptOptions source) {
        if (!checkForEnhance(source)) {
            return source;
        }
        List<YopCertConfig> mainKeys = YopCredentialsProviderRegistry.getProvider().getIsvEncryptKey(appKey);
        if (CollectionUtils.isEmpty(mainKeys)) {
            throw new YopClientException("isv encrypted key not config");
        }
        EncryptOptions mainKeyOptions = source.copy();
        String mainCredential = mainKeys.get(0).getValue();
        mainKeyOptions.setCredentials(new YopSymmetricCredentials(appKey, mainCredential));

        String credentialStr = ((YopSymmetricCredentials) source.getCredentials()).getCredential();
        byte[] credentialBytes = Encodes.decodeBase64(credentialStr);
        source.setEncryptedCredentials(YopEncryptorFactory.getEncryptor(YOP_CREDENTIALS_ENCRYPT_ALG_SM4)
                .encryptToBase64(credentialBytes, mainKeyOptions));
        source.setCredentials(new YopSymmetricCredentials(appKey, credentialStr));
        source.setCredentialsAlg(YOP_CREDENTIALS_ENCRYPT_ALG_SM4);
        source.enhance(YOP_ENCRYPT_OPTIONS_YOP_SM4_MAIN_CREDENTIALS, mainCredential);
        return source;
    }
}
