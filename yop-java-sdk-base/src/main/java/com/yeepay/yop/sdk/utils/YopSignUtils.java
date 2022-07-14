/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.google.common.collect.ImmutableMap;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.base.auth.signer.process.YopSignProcessorFactory;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/7/5 下午7:06
 */
public class YopSignUtils {
    private static final String SPLIT_CHAR = "$";
    private static Map<String, CertTypeEnum> digestAlgANdCertTypeMap = new ImmutableMap.Builder<String, CertTypeEnum>()
            .put("SHA256", CertTypeEnum.RSA2048)
            .put("SM3", CertTypeEnum.SM2)
            .build();

    /**
     * 验签：验签失败则抛出异常
     *
     * @param data
     * @param signature
     * @param appKey
     */
    public static void verify(String data, String signature, String appKey) {
        validSignature(signature);
        String args[] = StringUtils.split(signature, "$");
        CertTypeEnum certType = digestAlgANdCertTypeMap.get(args[1]);
        String serialNo = args.length == 4 ? args[3] : (CertTypeEnum.SM2.equals(certType) ? YopConstants.YOP_SM_PLATFORM_CERT_DEFAULT_SERIAL_NO : YopConstants.YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO);
        final YopPlatformCredentials yopPlatformCredentials = YopPlatformCredentialsProviderRegistry.getProvider().getCredentials(appKey, serialNo);
        if (null != yopPlatformCredentials) {
            verify(data, signature, yopPlatformCredentials.getCredential());
        } else {
            throw new YopClientException("can not load platform cert");
        }

    }

    /**
     * 验证签名：验签失败则抛出异常
     *
     * @param data
     * @param signature
     * @param publicKey
     */
    public static void verify(String data, String signature, PublicKey publicKey) {
        validSignature(signature);
        String args[] = signature.split("\\$");
        final CertTypeEnum certType = digestAlgANdCertTypeMap.get(args[1]);
        verify(data, signature, new PKICredentialsItem(publicKey, certType));
    }

    /**
     * 验证签名：验签失败则抛出异常
     *
     * @param data
     * @param signature
     * @param credentialsItem
     */
    public static void verify(String data, String signature, CredentialsItem credentialsItem) {
        validSignature(signature);
        YopSignProcessor yopSignProcessor = YopSignProcessorFactory.getSignProcessor(credentialsItem.getCertType().name());
        if (null == yopSignProcessor) {
            throw new YopClientException("unsupported certType");
        }
        String args[] = signature.split("\\$");
        if (!yopSignProcessor.verify(data, args[0], credentialsItem)) {
            throw new YopClientException("verify fail!");
        }
    }

    /**
     * 签名:无需传入私钥，私钥将从配置文件中读取
     *
     * @param data     待签名数据
     * @param certType 密钥类型
     * @param appKey   应用标识
     * @return
     */
    public static String sign(String data, String certType, String appKey) {
        YopPKICredentials yopCredentials = (YopPKICredentials) YopCredentialsProviderRegistry.getProvider()
                .getCredentials(appKey, certType);
        PKICredentialsItem pkiCredentialsItem = yopCredentials.getCredential();
        return sign(data, certType, pkiCredentialsItem.getPrivateKey());
    }

    /**
     * 签名
     *
     * @param data       待签名数据
     * @param certType   密钥类型:SM2或RSA2048
     * @param privateKey 私钥
     * @return
     */
    public static String sign(String data, String certType, PrivateKey privateKey) {
        YopSignProcessor yopSignProcessor = YopSignProcessorFactory.getSignProcessor(certType);
        if (null == yopSignProcessor) {
            throw new YopClientException("unsupported certType");
        }
        PKICredentialsItem pkiCredentialsItem = new PKICredentialsItem(privateKey, CertTypeEnum.parse(certType));
        return yopSignProcessor.sign(data, pkiCredentialsItem) + SPLIT_CHAR + yopSignProcessor.getDigestAlg();
    }

    private static void validSignature(String signature) {
        String args[] = signature.split("\\$");
        if ((args.length != 2 && args.length != 4)) {
            throw new YopClientException("illegal signature");
        }
        CertTypeEnum certType = digestAlgANdCertTypeMap.get(args[1]);
        if (certType == null) {
            throw new YopClientException("illegal signature");
        }
    }
}
