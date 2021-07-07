/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcess;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessFactory;
import com.yeepay.yop.sdk.exception.YopClientException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
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
    private static Map<String, CertTypeEnum> digestAlgANdCertTypeMap = new HashMap() {{
        put("SHA256", CertTypeEnum.RSA2048);
        put("SM3", CertTypeEnum.SM2);
    }};

    /**
     * 验签：验签失败则抛出异常
     *
     * @param data
     * @param signature
     * @param appKey
     */
    public static void verify(String data, String signature, String appKey) {
        validSignature(signature);
        String args[] = signature.split("\\$");
        CertTypeEnum certType = digestAlgANdCertTypeMap.get(args[1]);
        String serialNo = args.length == 4 ? args[3] : (CertTypeEnum.SM2.equals(certType) ? YopConstants.YOP_SM_PLATFORM_CERT_DEFAULT_SERIAL_NO : YopConstants.YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO);
        final YopPlatformCredentials yopPlatformCredentials = YopPlatformCredentialsProviderRegistry.getProvider().getYopPlatformCredentials(appKey, serialNo);
        if (null != yopPlatformCredentials) {
            verify(data, signature, yopPlatformCredentials.getPublicKey(certType));
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
        YopSignProcess yopSignProcess = YopSignProcessFactory.getYopSignProcess(digestAlgANdCertTypeMap.get(args[1]).getValue());
        if (null == yopSignProcess) {
            throw new YopClientException("unsupported certType");
        }
        PKICredentialsItem pkiCredentialsItem = new PKICredentialsItem(null, publicKey, digestAlgANdCertTypeMap.get(args[1]));
        if (!yopSignProcess.verify(data, args[0], pkiCredentialsItem)) {
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
        YopSignProcess yopSignProcess = YopSignProcessFactory.getYopSignProcess(certType);
        if (null == yopSignProcess) {
            throw new YopClientException("unsupported certType");
        }
        PKICredentialsItem pkiCredentialsItem = new PKICredentialsItem(privateKey, null, CertTypeEnum.parse(certType));
        return yopSignProcess.sign(data, pkiCredentialsItem) + SPLIT_CHAR + yopSignProcess.getDigestAlg().getValue();
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
