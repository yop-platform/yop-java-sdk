/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common;

import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfig;
import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfigProvider;
import com.yeepay.yop.sdk.config.provider.file.support.YopCertConfigUtils;
import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;
import org.junit.Assert;
import org.junit.Test;

import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_APPKEY;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/4/22 4:21 下午
 */
public class RsaPlatformCertTest {
    private static final String PRO_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6p0XWjscY+gsyqKRhw9MeLsEmhFdBRhT2emOck/F1Omw38ZWhJxh9kDfs5HzFJMrVozgU+SJFDONxs8UB0wMILKRmqfLcfClG9MyCNuJkkfm0HFQv1hRGdOvZPXj3Bckuwa7FrEXBRYUhK7vJ40afumspthmse6bs6mZxNn/mALZ2X07uznOrrc2rk41Y2HftduxZw6T4EmtWuN2x4CZ8gwSyPAW5ZzZJLQ6tZDojBK4GZTAGhnn3bg5bBsBlw2+FLkCQBuDsJVsFPiGh/b6K/+zGTvWyUcu+LUj2MejYQELDO3i2vQXVDk7lVi2/TcUYefvIcssnzsfCfjaorxsuwIDAQAB";
    private static final String QA_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4g7dPL+CBeuzFmARI2GFjZpKODUROaMG+E6wdNfv5lhPqC3jjTIeljWU8AiruZLGRhl92QWcTjb3XonjaV6k9rf9adQtyv2FLS7bl2Vz2WgjJ0FJ5/qMaoXaT+oAgWFk2GypyvoIZsscsGpUStm6BxpWZpbPrGJR0N95un/130cQI9VCmfvgkkCaXt7TU1BbiYzkc8MDpLScGm/GUCB2wB5PclvOxvf5BR/zNVYywTEFmw2Jo0hIPPSWB5Yyf2mx950Fx8da56co/FxLdMwkDOO51Qg3fbaExQDVzTm8Odi++wVJEP1y34tlmpwFUVbAKIEbyyELmi/2S6GG0j9vNwIDAQAB";

    @Test
    public void testRsaPlatformCert() {
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_test_rsa_platform_cert.json");
        YopFileSdkConfigProvider yopFileSdkConfigProvider = (YopFileSdkConfigProvider) YopSdkConfigProviderRegistry.getProvider();
        YopFileSdkConfig yopFileSdkConfig = yopFileSdkConfigProvider.loadSdkConfig(YOP_DEFAULT_APPKEY);
        YopCertConfig[] yopCertConfigs = yopFileSdkConfig.getYopPublicKey();
        Assert.assertTrue(yopCertConfigs.length == 2);
        String proPubkeyStr = RSAKeyUtils.key2String(YopCertConfigUtils.loadPublicKey(yopCertConfigs[0]));
        String qaPubkeyStr = RSAKeyUtils.key2String(YopCertConfigUtils.loadPublicKey(yopCertConfigs[1]));
        Assert.assertTrue(PRO_PUBKEY.equals(proPubkeyStr));
        Assert.assertTrue(QA_PUBKEY.equals(qaPubkeyStr));
    }
}
