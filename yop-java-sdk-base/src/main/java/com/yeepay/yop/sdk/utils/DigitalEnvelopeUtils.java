package com.yeepay.yop.sdk.utils;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.base.auth.signer.process.YopSignProcessorFactory;
import com.yeepay.yop.sdk.exception.VerifySignFailedException;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.security.SymmetricEncryptAlgEnum;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptorFactory;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import org.apache.commons.lang3.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

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

    private static final Map<DigestAlgEnum, CertTypeEnum> CERT_TYPE_ENUM_MAP;
    private static final Map<String, String> ENCRYPTOR_MAP;
    private static final Map<DigestAlgEnum, String> SIGNER_MAP;

    static {
        CERT_TYPE_ENUM_MAP = Maps.newHashMap();
        CERT_TYPE_ENUM_MAP.put(DigestAlgEnum.SM3, CertTypeEnum.SM2);
        CERT_TYPE_ENUM_MAP.put(DigestAlgEnum.SHA256, CertTypeEnum.RSA2048);

        ENCRYPTOR_MAP = Maps.newHashMap();
        ENCRYPTOR_MAP.put(DigestAlgEnum.SHA256.name(), "RSA");
        ENCRYPTOR_MAP.put(DigestAlgEnum.SM3.name(), "SM2");
        ENCRYPTOR_MAP.put(SymmetricEncryptAlgEnum.AES.name(), "AES");
        ENCRYPTOR_MAP.put(SymmetricEncryptAlgEnum.SM4.name(), "SM4/CBC/PKCS5Padding");

        SIGNER_MAP = Maps.newHashMap();
        SIGNER_MAP.put(DigestAlgEnum.SHA256, "RSA2048");
        SIGNER_MAP.put(DigestAlgEnum.SM3, "SM2");
    }



    /**
     * 拆开数字信封
     *
     * @param cipherText 待解密内容
     * @param privateKey 私钥（用于解密）
     * @return 已解密内容
     */
    public static String decrypt(String cipherText, PrivateKey privateKey) {
        return decrypt(cipherText, YopCredentialsProviderRegistry.getProvider().getDefaultAppKey(), privateKey);
    }

    /**
     * 拆开数字信封
     *
     * @param cipherText 待解密内容
     * @param appKey     appKey
     * @param privateKey 私钥（用于解密）
     * @return 已解密内容
     */
    public static String decrypt(String cipherText, String appKey, PrivateKey privateKey) {
        //分解参数
        String[] args = cipherText.split("\\" + SEPARATOR);
        if (args.length != 4) {
            throw new RuntimeException("source invalid : " + cipherText);
        }
        String encryptedRandomKeyToBase64 = args[0];
        String encryptedDataToBase64 = args[1];
        SymmetricEncryptAlgEnum symmetricEncryptAlg = SymmetricEncryptAlgEnum.parse(args[2]);
        DigestAlgEnum digestAlg = DigestAlgEnum.valueOf(args[3]);
        CertTypeEnum certType = CERT_TYPE_ENUM_MAP.get(digestAlg);


        //用私钥对随机密钥进行解密
        YopEncryptor unSymmetric = YopEncryptorFactory.getEncryptor(ENCRYPTOR_MAP.get(digestAlg.name()));
        byte[] randomKey = unSymmetric.decrypt(Encodes.decodeBase64(encryptedRandomKeyToBase64),
                new EncryptOptions(new YopPKICredentials(appKey, new PKICredentialsItem(privateKey, certType))));

        //用随机对称密钥，解密得到源数据
        final String encryptAlg = ENCRYPTOR_MAP.get(symmetricEncryptAlg.name());
        YopEncryptor symmetric = YopEncryptorFactory.getEncryptor(encryptAlg);
        byte[] decryptedData = symmetric.decrypt(Encodes.decodeBase64(encryptedDataToBase64),
                new EncryptOptions(new YopSymmetricCredentials(Encodes.encodeBase64(randomKey)), encryptAlg));

        //分解参数
        String data = new String(decryptedData, Charsets.UTF_8);
        String sourceData = StringUtils.substringBeforeLast(data, "$");
        String signToBase64 = StringUtils.substringAfterLast(data, "$");

        //验证签名
        YopPlatformCredentials platformCredentials = YopPlatformCredentialsProviderRegistry.getProvider().
                getLatestCredentials(appKey, certType.getValue());

        final YopSignProcessor yopSignProcess = YopSignProcessorFactory.getSignProcessor(SIGNER_MAP.get(digestAlg));
        boolean verifySign = yopSignProcess.verify(sourceData, signToBase64, platformCredentials.getCredential());
        if (!verifySign) {
            throw new YopClientException("verifySign fail!");
        }
        //返回源数据
        return sourceData;
    }

    /**
     * 拆开数字信(使用默认sdk配置文件中的私钥）
     *
     * @param cipherText     待解密内容
     * @param credentialType 证书类型（用于解密）"RSA2048"
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
     * @param credentialType 证书类型（用于解密）"RSA2048"
     * @return 已解密内容
     */
    public static String decrypt(String cipherText, String appKey, String credentialType) {
        YopPKICredentials yopCredentials = (YopPKICredentials) YopCredentialsProviderRegistry.getProvider()
                .getCredentials(appKey, credentialType);
        PKICredentialsItem pkiCredentialsItem = yopCredentials.getCredential();
        return decrypt(cipherText, appKey, pkiCredentialsItem.getPrivateKey());
    }

    /**
     * 验证签名(rsa)
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
        //验证签名
        boolean verifySign = YopSignProcessorFactory.getSignProcessor(CertTypeEnum.RSA2048.getValue())
                .verify(content.replaceAll("[ \t\n]", ""), signToBase64,
                        new PKICredentialsItem(publicKey, CertTypeEnum.RSA2048));
        if (!verifySign) {
            throw new VerifySignFailedException("Unexpected signature");
        }
    }
}
