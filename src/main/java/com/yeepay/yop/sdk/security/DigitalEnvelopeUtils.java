package com.yeepay.yop.sdk.security;

import com.google.common.base.Charsets;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfig;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfigProviderRegistry;
import com.yeepay.g3.core.yop.sdk.sample.config.support.BackUpAppSdkConfigManager;
import com.yeepay.g3.core.yop.sdk.sample.exception.VerifySignFailedException;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopClientException;
import com.yeepay.g3.core.yop.sdk.sample.security.rsa.RSA;
import com.yeepay.g3.core.yop.sdk.sample.utils.CharacterConstants;
import com.yeepay.g3.core.yop.sdk.sample.utils.Encodes;
import org.apache.commons.lang3.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * title: 数字证书工具类<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/1/23 16:40
 */
public class DigitalEnvelopeUtils {

    private static final String SEPARATOR = "$";

    /**
     * 拆开数字信封
     *
     * @param cipherText 待解密内容
     * @param privateKey 私钥（用于解密）
     * @return 已解密内容
     */
    public static String decrypt(String cipherText, PrivateKey privateKey) {
        //分解参数
        String[] args = cipherText.split("\\" + SEPARATOR);
        if (args.length != 4) {
            throw new RuntimeException("source invalid : " + cipherText);
        }
        String encryptedRandomKeyToBase64 = args[0];
        String encryptedDataToBase64 = args[1];
        SymmetricEncryptAlgEnum symmetricEncryptAlg = SymmetricEncryptAlgEnum.parse(args[2]);
        DigestAlgEnum digestAlg = DigestAlgEnum.parse(args[3]);

        SymmetricEncryption symmetricEncryption = SymmetricEncryptionFactory.getSymmetricEncryption(symmetricEncryptAlg);

        //用私钥对随机密钥进行解密
        byte[] randomKey = RSA.decrypt(Encodes.decodeBase64(encryptedRandomKeyToBase64), privateKey);

        //解密得到源数据
        byte[] encryptedData = symmetricEncryption.decrypt(Encodes.decodeBase64(encryptedDataToBase64), randomKey);

        //分解参数
        String data = new String(encryptedData, Charsets.UTF_8);
        String sourceData = StringUtils.substringBeforeLast(data, "$");
        String signToBase64 = StringUtils.substringAfterLast(data, "$");

        //验证签名
        PublicKey publicKey = getDefaultYopPublicKey();
        boolean verifySign = RSA.verifySign(sourceData, signToBase64, publicKey, digestAlg);
        if (!verifySign) {
            throw new YopClientException("verifySign fail!");
        }
        //返回源数据
        return sourceData;
    }

    private static PublicKey getDefaultYopPublicKey() {
        AppSdkConfig appSdkConfig = AppSdkConfigProviderRegistry.getProvider().getDefaultConfig();
        return appSdkConfig == null ? BackUpAppSdkConfigManager.getBackUpConfig().getDefaultYopPublicKey() :
                appSdkConfig.getDefaultYopPublicKey();
    }


    /**
     * 拆开数字信(使用默认sdk配置文件中的私钥）
     *
     * @param cipherText     待解密内容
     * @param credentialType 证书类型（用于解密）"RSA2048"或者"RSA4096"
     * @return 已解密内容
     */
    public static String decrypt(String cipherText, String credentialType) {
        return decrypt(cipherText, CertTypeEnum.parse(credentialType));
    }


    /**
     * 拆开数字信封(使用默认sdk配置文件中的私钥)
     *
     * @param cipherText 待解密内容
     * @param certType   证书类型（用于解密）"RSA2048"或者"RSA4096"
     * @return 已解密内容
     */
    public static String decrypt(String cipherText, CertTypeEnum certType) {
        AppSdkConfig appSdkConfig = AppSdkConfigProviderRegistry.getProvider().getDefaultConfig();
        if (appSdkConfig == null) {
            throw new YopClientException("No default appSdkConfig configured");
        }
        PrivateKey privateKey = appSdkConfig.loadPrivateKey(certType);
        if (privateKey != null) {
            return decrypt(cipherText, privateKey);
        } else {
            throw new YopClientException("No PrivateKey of Type:" + certType + " configured for default appSdkConfig.");
        }
    }


    /**
     * 拆开数字信封(使用指定appKey配置文件中的私钥)
     *
     * @param cipherText     待解密内容
     * @param appKey         appKey
     * @param credentialType 证书类型（用于解密）"RSA2048"或者"RSA4096"
     * @return 已解密内容
     */
    public static String decrypt(String cipherText, String appKey, String credentialType) {
        return decrypt(cipherText, appKey, CertTypeEnum.parse(credentialType));
    }


    /**
     * 拆开数字信封(使用指定appKey配置文件中的私钥)
     *
     * @param cipherText 待解密内容
     * @param appKey     appKey
     * @param certType   证书类型（用于解密）"RSA2048"或者"RSA4096"
     * @return 已解密内容
     */
    public static String decrypt(String cipherText, String appKey, CertTypeEnum certType) {
        AppSdkConfig appSdkConfig = AppSdkConfigProviderRegistry.getProvider().getConfig(appKey);
        if (appSdkConfig == null) {
            throw new YopClientException("No SDKConfig configured for appKey:" + appKey + ".");
        }
        PrivateKey privateKey = appSdkConfig.loadPrivateKey(certType);
        if (privateKey != null) {
            return decrypt(cipherText, privateKey);
        } else {
            throw new YopClientException("No PrivateKey of Type:" + certType + " configured for SDKConfig with appKey:" + appKey + ".");
        }
    }

    /**
     * 验证签名
     *
     * @param content   签名内容
     * @param signature 签名
     * @param publicKey 公钥
     */
    public static void verify(String content, String signature, PublicKey publicKey) {
        //分解参数
        String[] args = signature.split("\\" + CharacterConstants.DOLLAR);
        if (args.length != 2) {
            throw new VerifySignFailedException("Illegal format");
        }
        String signToBase64 = args[0];
        DigestAlgEnum digestAlg = DigestAlgEnum.parse(args[1]);
        //验证签名
        boolean verifySign = RSA.verifySign(content.replaceAll("[ \t\n]", ""), signToBase64, publicKey, digestAlg);
        if (!verifySign) {
            throw new VerifySignFailedException("Unexpected signature");
        }
    }

}
