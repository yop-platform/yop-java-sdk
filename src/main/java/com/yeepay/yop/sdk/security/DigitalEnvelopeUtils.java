package com.yeepay.yop.sdk.security;

import com.google.common.base.Charsets;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.exception.VerifySignFailedException;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.rsa.RSA;
import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;
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
        SymmetricEncryptAlgEnum symmetricEncryptAlg = SymmetricEncryptAlgEnum.parse(args[2]);
        DigestAlgEnum digestAlg = DigestAlgEnum.valueOf(args[3]);

        Encryption unsymmetricEncryption = UnsymmetricEncryptionFactory.getUnsymmetricEncryption(digestAlg);

        //用私钥对随机密钥进行解密
        byte[] randomKey = unsymmetricEncryption.decrypt(Encodes.decodeBase64(encryptedRandomKeyToBase64), privateKey.getEncoded());

        Encryption encryption = SymmetricEncryptionFactory.getSymmetricEncryption(symmetricEncryptAlg);

        //解密得到源数据
        byte[] encryptedData = encryption.decrypt(Encodes.decodeBase64(encryptedDataToBase64), randomKey);

        //分解参数
        String data = new String(encryptedData, Charsets.UTF_8);
        String sourceData = StringUtils.substringBeforeLast(data, "$");
        String signToBase64 = StringUtils.substringAfterLast(data, "$");

        //验证签名
        PublicKey publicKey = getYopPublicKey(CertTypeEnum.RSA2048);
        Signer signer = SignerFactory.getSigner(digestAlg);
        boolean verifySign = signer.verifySign(publicKey, sourceData.getBytes(), Encodes.decodeBase64(signToBase64));
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

    public static void main(String[] args) {
        final PrivateKey privateKey = RSAKeyUtils.string2PrivateKey("MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCNXteWiSs7ypGiNkGIW1y4KC//VxdPSba7qcnRauqskvDNqDrJruZDfRkEGAg7GvCv9bkRHLjB4vVLYARaHgT6BIXv3kCNPO3RwhKJa1nN4LCwz4YCiHg0OjLWTD5JibGAUjfWMIhtryFrIku+Z/4hX6eTYmkm7Sh+8zlgNiTS/w4n7zQL6IjLXaValGcp7qOS6c6VH7h4I3Ww7BIIsOAJnuNR3yV0hmR+Fi7RT1AftDlH+xZa6kAQ9a2lCFNuOCWsVLjj+OX74G5Jh2BqvnWVJcoaW1tMHrBjORIj8ocTZindM7BKukiHFFexXDD+578wV1Su6BLebiOCFvmZqW/LAgMBAAECggEAB3ums7wtAqw1Sz3N+DbF0KWn7L8iZ7sCJirVmPUs2NeqUWh/PB+65oWfplzTSrWhWd8K3cIEcZbe3w+FC3QdVVKZ+FluV0uLVxgLvSRmi4RCNgm2ETpCCpmuCTPd9CyKrb0sYXUOM6gaf74//iMpC4ExsJZ79GiHRTkAp3wWNOjg61UAaPMge30hPri1PIPoJ9fbK9eLEMuAWSXfKcV9xOgCEMp716s0wJUZ+oWa12wYzOTwSfWcm+qbT15wnEMiunKtv76321wkdNaDQduHRUnjmn9CGzJOK7cptTB+QdOGVqBsA8UY9TntRKzFzT8e4ZJlD34xEmUlS250rEsjEQKBgQDdO9UZcyfGoyuWPGgRMtjf05DQLUWHRfSWqU7B+1OIPJnIgaOCckGHhjUlcDPalpkqs3gDKZpmOK9u+eKK2moW6VY1Bkm0sbQA/jpZLhIaZZYluoYlPMSp+TNMrdn/40IXYDmuvLdgw+q8+BHaU32KOOH2Zt/j1FeZAt7qGwW2EwKBgQCjliKJJb0JZiOwOf94fEbv+FFRYA0Ldgi1NohnxGfIPMOmOtQNtRME+0jTrutkCzaj8eJCSEUbx6d96feDv0ovHYMvx85dMWDDHvOUdMY/0h96uYF7P1zgQoRNEsA8CibdRQ55YtUGJIIyjXPG6aGLepMPxSwUuXx6ElBU9Kd2aQKBgEMP0gcG6zXBXIMMCJe+DBO9NBaGwp7Ay+WnqOIQZz/S7uLiuUffjpGgUxfoLS/DmuZgxK5gvBbkc/l18gAZOTN8w88K+ui1PVVXuyQxXPd3d84z/lnnU84aZjyGLqDsqKohVxrqxcOlX9Cseezuhg2SNJzWjmOniG4vepcIXfDVAoGAFW8XiVbLhQ7AAFgpFBVcihbEOOBX9Td6/pttlgMTAhaMIBy3KjCelFmEelACAbLCbwrDdaHYhEJNvN7KEpYe1zuLs9f/PxC4N/i3O61tSHF0tHTX9VkhBzVP9nVNgNBntThWJCuB8/MWqB8Gm3qDMy5VVIymMPT4zVnW8rq3snkCgYA2FWR3D7sSCWmJ6kEIrJHW2KhLOSl+QE47PJbTVVCB8LFJFTdM4wOJbgDCIQyrqJiiQkkdapSxzeARFpidAN4afrw6LOSO9QNqQgffGjiZHqp9UX+GTxSa822ciYMBNUoclvnph8ZUxPhXfbgFJlX9BStVXYUFObcQvvwvaxqsQg==");
        String cipherText ="QkV8Fl8OSNLjfAJmfQDqHt4dTc7ZpzX5vlyymuRl7jQxm0yPcKeprM2A7DGYPSL2WkONU9s_D4QKs85RstPjCGR_dpszjUXSaJJD13BxuOusIfU7AzgzDldKpRFZQDLn-hsoSFs6C0GfIE-0m8ASK0l6HBKZkR1TayW0pygS2lBCHIbQGFR81914rh5b0laPZTwlY5g-BOT1f3Z3XV4Ko9eYQ7ggnpbA-d5YOQWdMJM0ulpzIE1DCqZyzxyOaR77h9jvTPo1xa-BJvasn31zTqbFs3dJSzlv_xFrul1CdEnHJh3OMu_7zzJUw6z8LlnieLTrw1tgdfXZbKWMgoOwzw$cScFkYDWGse_rI0_gGiF29SO3dM8mY3SND_qnVo1GNtW4fs5mRpH7IjIisDb7OucHYbREaLzu7vx-iWFpXokoZHdxTI80R4pNDGjNzEnzECTU--3FChYLChVjPGH-ugsFo3qRoiqLajgXkSMKmE17-kYlB9t1Icooi7OIF7ZMdxswCosvg6T5NvGHtB5kPsjkpVHLmT01rUDTnwZedkadzV5EagKFXILi_8gNUT6vz-3EInRGz-R9RjEK9zUMZYJYFVtmzqyuTGIjiECo1__Pyt627MwQe8uPFtwvp9OHQ-fQk_P63gzv_9CanoX1hcE0p7Ay3bimMT27SMKnamaufqC77_0V5in1t7BYBtEFT71KNN8AnDZdYJ3M3SCjI-0qmzNG1L5KJ2lGGxWjAij6xYBRFPqZ6ixz-Dj_RMj3TCYp9Ph5RwfSGwurVT0ER4gRsvSdbISLAmiOWunifrB3AeRbC-TFZmh5GTkFirGy4STf0pO4Y8BpB6t_rYedB5f9n3InarM_F2aqUTEmf95oqmHntX0WUTUDQMQ4x14WuKPIYirZ-QjXxSlkZJxS_gRp6ffB9h06zV-bZeR3J_RciAzppOhGFXteLUypDZfukvHmbnowr-XiY9YbDLMWdTQP9Czh6gYOo217Cu2N9LiKwM2aNgzdkc_lfxkDXiIXcyNsoI3LRYo-dFepNTSzqdVbrgc3mr3lwxTBLPXX65iMfpb1SuXDlrg0naS8YjgT4Rd_07p9B3RNH3IA-RIO7IfM9ou26bfoC4SEPNQ3Qdo_fiAS6M-u26Y0hjNAE0S6g4LOdw3MUHos52ZKOwVkfXUqn0xbMsossR-Y6z50du4vON8-S3teftNlP90qIVUOP_uPYBXOLTzst6TxgNRwAs5Ovylieni64150YWEM9E4_9s848swCKgLF5sPZ13hTStp9wF5j0kiETPMlWUJ-joqvzMMNsFgr9A3mhO4HiB54EBc2lKUCrcwkh7QpEZvTeBJigu20Pxksy541tPuKnHzVXB_NELF-iiq6OFDGnGSY7SX2gaIHDbbnCPBEig7pyCSgbX6Ygk40sLJOMOSbwXb_NNTt9vuNQ4lZRTPen7M1SU2WpaWNahHRJ_czU-_sncY2GlJucGpTWOWSxRIa489R5MY3O83ekOHYh9z4M64Ug$AES$SHA256";
        System.out.println(DigitalEnvelopeUtils.decrypt(cipherText, privateKey));
        System.out.println(1);
    }

}
