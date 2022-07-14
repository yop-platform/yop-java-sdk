/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.internal;

import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.req.AuthorizationReq;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptorFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_ENCRYPT_ALG;

/**
 * title: 请求分析器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/6/26
 */
public class RequestAnalyzer {

    /**
     * 获取YOP凭证
     *
     * @param requestConfig    请求配置
     * @param authorizationReq 安全需求
     * @return YOP凭证
     */
    public static YopCredentials<?> getCredentials(YopRequestConfig requestConfig, AuthorizationReq authorizationReq) {
        YopCredentials<?> credential = requestConfig.getCredentials();
        if (null == credential) {
            credential = YopCredentialsProviderRegistry.getProvider().getCredentials(requestConfig.getAppKey()
                    , authorizationReq.getCredentialType());
        }
        if (null == credential) {
            throw new YopClientException("No credentials specified");
        }
        return credential;
    }

    /**
     * 判断是否支持加解密
     *
     * @param credential    YOP凭证
     * @param requestConfig 请求配置
     * @return true:支持，false:不支持
     */
    public static boolean isEncryptSupported(YopCredentials<?> credential, YopRequestConfig requestConfig) {
        // 指定不加密、或者不支持加密，或者没有有效证书，则不进行加密
        return !BooleanUtils.isFalse(requestConfig.getNeedEncrypt())
                && !(credential instanceof YopPKICredentials &&
                CertTypeEnum.RSA2048.equals(((YopPKICredentials) credential).getCredential().getCertType()))
                && !(null == YopPlatformCredentialsProviderRegistry.getProvider()
                .getLatestCredentials(credential.getAppKey(), CertTypeEnum.SM2.getValue()));
    }

    /**
     * 获取默认加密器
     *
     * @param requestConfig 请求配置
     * @return 加密器
     */
    public static YopEncryptor getEncryptor(YopRequestConfig requestConfig) {
        if (StringUtils.isBlank(requestConfig.getEncryptAlg())) {
            requestConfig.setEncryptAlg(YOP_DEFAULT_ENCRYPT_ALG);
        }
        YopEncryptor encryptor = YopEncryptorFactory.getEncryptor(requestConfig.getEncryptAlg());
        if (null == encryptor) {
            throw new YopClientException("not supported the encryptAlg: " + requestConfig.getEncryptAlg());
        }
        return encryptor;
    }
}
