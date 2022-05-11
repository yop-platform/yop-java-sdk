/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.crypto.YopCertCategory;
import com.yeepay.yop.sdk.crypto.YopCertParserFactory;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.DigitalEnvelopeUtils;
import com.yeepay.yop.sdk.security.encrypt.BigParamEncryptMode;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptorFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

/**
 * title: 商户通知工具类测试<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021-05-08
 */
public class DigitalEnvelopeUtilsTest {

    @Test
    public void testRsa2048() throws InvalidKeySpecException {
        String cipherText ="加密的报文";
        String privateKeyStr = "商户的私钥";
        String appKey = "商户的appKey";
        PrivateKey isvPrivateKey = getPrivateKey(privateKeyStr, CertTypeEnum.RSA2048);
        //若商户存在多应用，解密方法中可设置appKey
        String plaintTextWithAppKey = DigitalEnvelopeUtils.decrypt(cipherText, appKey,"RSA2048");
        System.out.println(plaintTextWithAppKey);

        //若不想从配置文件中获取商户私钥，可通过此方法设置商户私钥
        String plaintTextWithPrivateKey = DigitalEnvelopeUtils.decrypt(cipherText,isvPrivateKey);
        System.out.println(plaintTextWithPrivateKey);

        //默认从配置文件中获取易宝公钥和商户私钥
        String plaintText = DigitalEnvelopeUtils.decrypt(cipherText,"RSA2048");
        System.out.println(plaintText);
    }

    private PrivateKey getPrivateKey(String priKey, CertTypeEnum certType) {
        final YopCertConfig yopCertConfig = new YopCertConfig();
        yopCertConfig.setCertType(certType);
        yopCertConfig.setValue(priKey);
        yopCertConfig.setStoreType(CertStoreType.STRING);
        return (PrivateKey) YopCertParserFactory.getCertParser(YopCertCategory.PRIVATE, certType).parse(yopCertConfig);
    }

    @Test
    public void testSm4() throws GeneralSecurityException {
        //商户sm4密钥
        String key = "JnhWelxyAaFlszR1ER2Upw==";
        //从通知报文中获取的once字段，可能为空，即使当前接收到的值可能为空为兼容后续变更请务必获取此参数值，传入解密方法中
        String once = null;
        //从通知报文中获取的associatedData字段，可能为空，即使当前接收到的值可能为空为兼容后续变更请务必获取此参数值，传入解密方法中
        String cipherText = "2MV2vk_W8NlpHxB3L8wdiBXVALVF13aNxmbwILLQiSNdWYSXN_aM2YZk1n-oPMdW-heEt9_cIyNb";
        String associatedData = null;
        once = StringUtils.defaultIfEmpty(once,null);//若接收到的once值为""（空字符串）调用sdk中的解密方法需传成null
        associatedData = StringUtils.defaultIfEmpty(associatedData,null);//若接收到的associatedData值为""（空字符串）调用sdk中的解密方法需传成null
        final String encryptAlg = "SM4/GCM/NoPadding";
        String plainText = YopEncryptorFactory.getEncryptor(encryptAlg).decryptFromBase64(cipherText,
                new EncryptOptions(new YopSymmetricCredentials(key), "", encryptAlg, once, associatedData, BigParamEncryptMode.stream));
        System.out.println(plainText);
    }

}
