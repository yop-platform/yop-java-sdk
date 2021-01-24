package com.yeepay.yop.sdk.http.analyzer;

import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.http.HttpResponseAnalyzer;
import com.yeepay.yop.sdk.http.HttpResponseHandleContext;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopResponseMetadata;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;
import com.yeepay.yop.sdk.utils.Sm2Utils;
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
            PKICredentialsItem pkiCredentialsItem = getCredentialItem(context);
            YopPKICredentials credentials = new YopPKICredentials(null, null, pkiCredentialsItem);
            context.getSigner().checkSignature(context.getResponse(), metadata.getYopSign(), credentials, context.getSignOptions());
        }
        return false;
    }

    private PKICredentialsItem getCredentialItem(HttpResponseHandleContext context) {
        SignOptions signOptions = context.getSignOptions();
        if (SM2_PROTOCOL_PREFIX.equals(signOptions.getProtocolPrefix())) {
            return new PKICredentialsItem(null, Sm2Utils.key2String(context.getYopPublicKey()), CertTypeEnum.SM2);
        } else {
            return new PKICredentialsItem(null, RSAKeyUtils.key2String(context.getYopPublicKey()), CertTypeEnum.RSA2048);
        }
    }

}
