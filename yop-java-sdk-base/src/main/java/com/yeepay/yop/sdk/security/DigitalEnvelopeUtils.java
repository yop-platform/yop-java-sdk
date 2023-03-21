package com.yeepay.yop.sdk.security;

import com.google.common.base.Charsets;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.exception.VerifySignFailedException;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.aes.AES;
import com.yeepay.yop.sdk.security.rsa.RSA;
import com.yeepay.yop.sdk.security.rsa.RSA2048;
import com.yeepay.yop.sdk.utils.CharacterConstants;
import com.yeepay.yop.sdk.utils.Encodes;
import org.apache.commons.lang3.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * title: 数字证书工具类<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
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
        DigestAlgEnum digestAlg = DigestAlgEnum.valueOf(args[3]);

        Encryption unsymmetricEncryption = new RSA2048();

        //用私钥对随机密钥进行解密
        byte[] randomKey = unsymmetricEncryption.decrypt(Encodes.decodeBase64(encryptedRandomKeyToBase64), privateKey.getEncoded());

        Encryption encryption = new AES();

        //解密得到源数据
        byte[] encryptedData = encryption.decrypt(Encodes.decodeBase64(encryptedDataToBase64), randomKey);

        //分解参数
        String data = new String(encryptedData, Charsets.UTF_8);
        String sourceData = StringUtils.substringBeforeLast(data, "$");
        String signToBase64 = StringUtils.substringAfterLast(data, "$");

        //验证签名
        PublicKey publicKey = getYopPublicKey(CertTypeEnum.RSA2048);
        Signer signer = SignerFactory.getSigner(digestAlg);
        boolean verifySign = signer.verifySign(publicKey, sourceData.getBytes(Charsets.UTF_8), Encodes.decodeBase64(signToBase64));
        if (!verifySign) {
            throw new YopClientException("verifySign fail!");
        }
        //返回源数据
        return sourceData;
    }

    private static PublicKey getYopPublicKey(CertTypeEnum certType) {
        YopSdkConfig yopSdkConfig = YopSdkConfigProviderRegistry.getProvider().getConfig();
        return yopSdkConfig.loadYopPublicKey(certType);
    }

    /**
     * 拆开数字信(使用默认sdk配置文件中的私钥）
     *
     * @param cipherText     待解密内容
     * @param credentialType 证书类型（用于解密）"RSA2048"或者"RSA4096"
     * @return 已解密内容
     */
    public static String decrypt(String cipherText, String credentialType) {
        return decrypt(cipherText, YopCredentialsProviderRegistry.getProvider().getDefaultAppKey(), credentialType);
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
        YopPKICredentials yopCredentials = (YopPKICredentials) YopCredentialsProviderRegistry.getProvider()
                .getCredentials(appKey, credentialType);
        PKICredentialsItem pkiCredentialsItem = yopCredentials.getCredential();
        return decrypt(cipherText, pkiCredentialsItem.getPrivateKey());
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
        DigestAlgEnum digestAlg = DigestAlgEnum.valueOf(args[1]);
        //验证签名
        boolean verifySign = RSA.verifySign(content.replaceAll("[ \t\n]", ""), signToBase64, publicKey, digestAlg);
        if (!verifySign) {
            throw new VerifySignFailedException("Unexpected signature");
        }
    }
}
