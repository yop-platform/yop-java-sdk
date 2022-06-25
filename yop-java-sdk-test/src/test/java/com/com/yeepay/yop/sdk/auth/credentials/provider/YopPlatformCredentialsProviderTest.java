/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProvider;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * title: Yop平台凭证加载测试<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/25
 */
public class YopPlatformCredentialsProviderTest {

    @Test
    @Ignore
    public void testPro() {
        final YopPlatformCredentialsProvider provider = YopPlatformCredentialsProviderRegistry.getProvider();
        final CredentialsItem rsaPlatformCredentials = provider.getYopPlatformCredentials("", YopConstants.YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO).getCredential();
        Assert.assertNotNull(rsaPlatformCredentials);
        Assert.assertEquals(CertTypeEnum.RSA2048, rsaPlatformCredentials.getCertType());

        final CredentialsItem sm2PlatformCredentials = provider.getYopPlatformCredentials("", "289782695477").getCredential();
        Assert.assertNotNull(sm2PlatformCredentials);
        Assert.assertEquals(CertTypeEnum.SM2, sm2PlatformCredentials.getCertType());
    }

    @Test
    public void testQa() {
        System.setProperty("yop.sdk.config.env", "qa");
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_test_sm.json");
        final YopPlatformCredentialsProvider provider = YopPlatformCredentialsProviderRegistry.getProvider();
        final CredentialsItem rsaPlatformCredentials = provider.getYopPlatformCredentials("",
                YopConstants.YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO).getCredential();
        Assert.assertNotNull(rsaPlatformCredentials);
        Assert.assertEquals(CertTypeEnum.RSA2048, rsaPlatformCredentials.getCertType());

        final CredentialsItem sm2PlatformCredentials = provider.getYopPlatformCredentials("", "275550212193").getCredential();
        Assert.assertNotNull(sm2PlatformCredentials);
        Assert.assertEquals(CertTypeEnum.SM2, sm2PlatformCredentials.getCertType());
    }
}
