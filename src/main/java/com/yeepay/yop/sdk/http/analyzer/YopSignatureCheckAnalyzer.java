package com.yeepay.yop.sdk.http.analyzer;

import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.HttpResponseAnalyzer;
import com.yeepay.yop.sdk.http.HttpResponseHandleContext;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopResponseMetadata;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * title: 签名校验<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/1 10:43
 */
public class YopSignatureCheckAnalyzer implements HttpResponseAnalyzer {

    private static final YopSignatureCheckAnalyzer INSTANCE = new YopSignatureCheckAnalyzer();

    private String SM2_PROTOCOL_PREFIX = "YOP-SM2-SM3";

    public static YopSignatureCheckAnalyzer getInstance() {
        return INSTANCE;
    }

    private YopSignatureCheckAnalyzer() {
    }

    @Override
    public <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception {
        YopResponseMetadata metadata = response.getMetadata();
        if (StringUtils.isNotEmpty(metadata.getYopSign())) {
            PKICredentialsItem pkiCredentialsItem = getCredentialItem(context.getSignOptions(), context.getAppKey(), metadata.getYopCertSerialNo());
            if (null != pkiCredentialsItem) {
                YopPKICredentials credentials = new YopPKICredentials(context.getAppKey(), null, pkiCredentialsItem);
                context.getSigner().checkSignature(context.getResponse(), metadata.getYopSign(), credentials, context.getSignOptions());
            } else {
                if (context.isForceVerifySign()) {
                    throw new YopClientException("yop platform credentials not found");
                }
            }
        }
        return false;
    }

    private PKICredentialsItem getCredentialItem(SignOptions signOptions, String appKey, String serialNo) {
        final YopPlatformCredentials yopPlatformCredentials = YopPlatformCredentialsProviderRegistry.getProvider().getCredentials(appKey, serialNo);
        if (null == yopPlatformCredentials) {
            return null;
        }
        if (SM2_PROTOCOL_PREFIX.equals(signOptions.getProtocolPrefix())) {
            return new PKICredentialsItem(null, yopPlatformCredentials.getPublicKey(CertTypeEnum.SM2), CertTypeEnum.SM2);
        } else {
            return new PKICredentialsItem(null, yopPlatformCredentials.getPublicKey(CertTypeEnum.RSA2048), CertTypeEnum.RSA2048);
        }
    }

}
