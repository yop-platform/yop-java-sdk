/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.security.cert.YopCertCategory;
import com.yeepay.yop.sdk.base.security.cert.parser.YopCertParserFactory;
import com.yeepay.yop.sdk.security.cert.YopPublicKey;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.security.PrivateKey;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/7/6 下午7:25
 */
public class YopSignUtilsTest {
    @Before
    public void setUp() {
        System.setProperty("yop.sdk.http", "true");
    }

    String data = "这是待签名原文a=123";

    @Test
    public void testSm2Sign() {
        System.setProperty("yop.sdk.config.env", "qa");
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_test_sm.json");
        String certType = "SM2";
        String priKey = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgB0DK/uNgBEVHoM4QSbVW5rZ6Jkni6XEZfqVE33hiq2SgCgYIKoEcz1UBgi2hRANCAAQQnyX/PqhRYBDdw2uK07m5GNyaBeXbxIPwaA4kxTj6tCSFsTkh2x2tJ4OyVzbJThSmUsvu8do2dWvSRml9tziw";
        final YopCertConfig yopCertConfig = new YopCertConfig();
        yopCertConfig.setCertType(CertTypeEnum.SM2);
        yopCertConfig.setValue(priKey);
        yopCertConfig.setStoreType(CertStoreType.STRING);
        String signatureWithPrikey = YopSignUtils.sign(data, certType, (PrivateKey) YopCertParserFactory.getCertParser(YopCertCategory.PRIVATE,
                CertTypeEnum.SM2).parse(yopCertConfig));
        Assert.assertTrue(StringUtils.isNotEmpty(signatureWithPrikey));
        String signatureWithAppKey = YopSignUtils.sign(data, certType, "app_100800095600038");
        Assert.assertTrue(StringUtils.isNotEmpty(signatureWithAppKey));
    }

    @Test
    public void testRsaSign() {
        System.setProperty("yop.sdk.config.env", "qa");
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_app_10085525305.json");
        String certType = "RSA2048";
        String priKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDFtjrUYU2t+WWOOm6CK3L6nikNKY1gty2x2cmIaAF0IWlBQ33ljJubzNODWGs4kL2nqfQnmMqetLatlMdgvquuftNL3kpZvOX8E746REHPH+MP+dyalvbitwP6vUbFbiKiO555hM1AhtdY17TVZthNtklqDmkXZ7U9Lb5+OIQrxK8uvy+0hupZehpHs0lXBx8Hh2kXFO6BomUWAuc/GVWSBhL1ziq9zx+M37LP4yYEogsqZNq70vi77IaD06r0MgYJ1foyyHz1b5J5joYrkPb9+w8hoU5iK62aitX8bsfRbGh93BbrYBGVwpZGmo8xmxmnEqDyU0HCj50P0GbJ77EtAgMBAAECggEADR318TkzaiNttW3y/vfa/P2ZQ6JKGuyMP4xvbnlX/1hoH0hXBe+6My/3qHMpSetwabtA04+zgawDoqiIQcbkpQMNCa+Jx0JdD8hPipYUt6Up71loZWk2n/v8a5o7I8YWziSecvl4lJtdlitZd/8GwsEhRcQG/OKIh7KNPNqLCkw4TzJkYsL02QogF7fL/NVCw/AakIX1MjDFXCm+e7ifylztRNdm2oUuyw66C+nkORVL3GcabHOixsxUsqhMGBcpIJ4Y+Naypb79nRH255LsexE2N2y39pi9niZJNmfprUJMGhvA1NFcaN363IADfkcoYF0InQA7mMtzszPSWbTTgQKBgQD0PEmzxJxUhpXOKJVcZJhn+1wSyPqclHDRC+uYhyJhut/lSp7gIhoI2+wvIok7bYH3zrbCBregY4hlLiQjTH5zRoF9KvWf6O6OhtZT9RTVFQ+i9yjn/+7t4alWkYaA+oaNG/YXxKLXHOG8KLH7OdBjQpqVYmPyCoqJYNrjQS9axQKBgQDPPEAMPLtFR4esztB1vdcR4lCizyHBUHab/eWxyER22AyHh5VJFP5BTfKB34TFcKXKP/Nn3X5OEzQdoEivbvy+0QkIC2AzODZlcifUY5hj+JljpmldAO4TZM8qW4hrRkvAvu8CLekyVWk6h7gQLr7H2TL7qcqJW8F0ywNRR8yDSQKBgAs3oaLyCsQPEl5PmtyNejp8XvQ16ty6LJxNUdrFihy2+oWLcdSVfGCfyS85BNiH5Qo+okIzEMf7Ck6rWdmNL9mXiWb4TCO2DQ7avolInlZTC07Oz0Aojw656I8jS+wslXVxrVHWJCyBFRURQWtqclm8u2DVDgYV2dsJacQ6QDSJAoGAIKE8+HBLkFH98+CwhAl7Jq66wZfZmcWgl1k35HFDDm9gMarQf6xViFTMnVRjZG8jO6AsJCuE6qgtaYjGSRExrJ3fTSv1Xrs4HWsHCHMSGJOZG06lgmZWFimmUOYOqc1suhGWMoKmGC3IntWlzq59jZwOYf/PCyeuY0Rf5llmwzkCgYEA7NViHZgcukk1iwy4QAu4q4IyzckKIT2pTiKFlVhb4cO48w99HSvfmqJSPUZVN3I15UwVs0lqnzLanU/+aSTYJDArpgEhEwloDAkhfy8NQzM9Ji+XXA3itDdR7A7+Wy5XULqK0FVjXMXNK3sV/O0qGWey7aOiH1QUwMyhT4qlenc=";
        final YopCertConfig yopCertConfig = new YopCertConfig();
        yopCertConfig.setCertType(CertTypeEnum.RSA2048);
        yopCertConfig.setValue(priKey);
        yopCertConfig.setStoreType(CertStoreType.STRING);
        String signatureWithPrikey = YopSignUtils.sign(data, certType, (PrivateKey) YopCertParserFactory.getCertParser(YopCertCategory.PRIVATE,
                CertTypeEnum.RSA2048).parse(yopCertConfig));
        Assert.assertTrue(StringUtils.isNotEmpty(signatureWithPrikey));
        String signatureWithAppKey = YopSignUtils.sign(data, certType, "app_10085525305");
        Assert.assertTrue(StringUtils.isNotEmpty(signatureWithAppKey));
    }

    @Test
    @Ignore
    public void testVerifySm2Sign() {
        String pubKey = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAESUkMtofAfGIkj6BD1K20YcFR24zqmrub7IaVJoHsfx3loCTZvKexXPpWnYY/Zb+mPr3wieuLCHbKB0HoimTTCw==";
        String signature = "OdU3vPOIddmzxBZs4m3IguT_X4aIgVN6cPvzQQjW1hgu_oA8oEa_2gdAb_zZIIE7qyTTgtXELqJm0P-TCPOxEA$SM3$SM2$289798445125";
        final YopCertConfig yopCertConfig = new YopCertConfig();
        yopCertConfig.setCertType(CertTypeEnum.SM2);
        yopCertConfig.setValue(pubKey);
        yopCertConfig.setStoreType(CertStoreType.STRING);
        YopSignUtils.verify(data, signature, ((YopPublicKey) YopCertParserFactory.getCertParser(YopCertCategory.PUBLIC,
                CertTypeEnum.SM2).parse(yopCertConfig)).getPublicKey());
        YopSignUtils.verify(data, signature, "");
    }

    @Test
    public void testVerifyRsaSign() {
        String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6p0XWjscY+gsyqKRhw9MeLsEmhFdBRhT2emOck/F1Omw38ZWhJxh9kDfs5HzFJMrVozgU+SJFDONxs8UB0wMILKRmqfLcfClG9MyCNuJkkfm0HFQv1hRGdOvZPXj3Bckuwa7FrEXBRYUhK7vJ40afumspthmse6bs6mZxNn/mALZ2X07uznOrrc2rk41Y2HftduxZw6T4EmtWuN2x4CZ8gwSyPAW5ZzZJLQ6tZDojBK4GZTAGhnn3bg5bBsBlw2+FLkCQBuDsJVsFPiGh/b6K/+zGTvWyUcu+LUj2MejYQELDO3i2vQXVDk7lVi2/TcUYefvIcssnzsfCfjaorxsuwIDAQAB";
        String signature = "jx5r9cwnQPk7MI7lu0z76OOVhmbZcfPF4zagVBbUaODVET3rBoKkmLYGIbq4t-LBZRKr4_TNb22M20bxpPcND4ZzDJtYQg9StBRfJte3fzJnyBYMh-xajXvdbcxBf41CVuvLC6-6U1z6b-_48-Ffl0mKuAeiLHWKya6z7h5QkknMqPdFvCXkENo8DAcBIn6C31I48riEvLKVEc_upSFZctuXI-flhzMrOT-r18_3Dj6V8eK9eLijI8QA59oj1B07IdAfrlrozk0l8aA-PnTvS87evz7dmUfClKiG8qHkfSffQNLTCtl-rVd9Ynxpw6CRuo96f3Os0nKNiEJUPeTDVg$SHA256";
        final YopCertConfig yopCertConfig = new YopCertConfig();
        yopCertConfig.setCertType(CertTypeEnum.RSA2048);
        yopCertConfig.setValue(pubKey);
        yopCertConfig.setStoreType(CertStoreType.STRING);
        YopSignUtils.verify(data, signature, ((YopPublicKey) YopCertParserFactory.getCertParser(YopCertCategory.PUBLIC,
                CertTypeEnum.RSA2048).parse(yopCertConfig)).getPublicKey());
    }
}
