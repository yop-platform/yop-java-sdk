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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;

import static com.yeepay.yop.sdk.YopConstants.SM2_PROTOCOL_PREFIX;
import static com.yeepay.yop.sdk.YopConstants.YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(YopSignatureCheckAnalyzer.class);

    private static final YopSignatureCheckAnalyzer INSTANCE = new YopSignatureCheckAnalyzer();

    public static YopSignatureCheckAnalyzer getInstance() {
        return INSTANCE;
    }

    private YopSignatureCheckAnalyzer() {
    }

    @Override
    public <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception {
        YopResponseMetadata metadata = response.getMetadata();
        if (BooleanUtils.isTrue(context.isSkipVerifySign()) || StringUtils.isBlank(metadata.getYopSign())) {
            return false;
        } else {
            PKICredentialsItem pkiCredentialsItem = getCredentialItem(context.getSignOptions(), context.getAppKey(), metadata.getYopCertSerialNo());
            if (null != pkiCredentialsItem) {
                YopPKICredentials credentials = new YopPKICredentials(context.getAppKey(), pkiCredentialsItem);
                context.getSigner().checkSignature(context.getResponse(), metadata.getYopSign(), credentials, context.getSignOptions());
            } else {
                throw new YopClientException("yop platform credentials not found");
            }
        }
        return false;
    }

    private PKICredentialsItem getCredentialItem(SignOptions signOptions, String appKey, String serialNo) {
        CertTypeEnum certType = SM2_PROTOCOL_PREFIX.equals(signOptions.getProtocolPrefix()) ? CertTypeEnum.SM2 : CertTypeEnum.RSA2048;
        if (certType == CertTypeEnum.RSA2048) {
            if (StringUtils.isNotBlank(serialNo)) {
                LOGGER.warn("rsa signed request not need serialNo:{}.", serialNo);
            }
            serialNo = YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO;
        }

        final YopPlatformCredentials yopPlatformCredentials = YopPlatformCredentialsProviderRegistry.getProvider().getYopPlatformCredentials(appKey, serialNo);
        if (null != yopPlatformCredentials) {
            PublicKey publicKey = yopPlatformCredentials.getPublicKey(certType);
            if (null != publicKey) {
                return new PKICredentialsItem(null, yopPlatformCredentials.getPublicKey(certType), certType);
            }
        }
        return null;
    }

}
