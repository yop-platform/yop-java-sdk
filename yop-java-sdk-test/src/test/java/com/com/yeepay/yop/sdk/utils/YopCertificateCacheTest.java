/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.cache.YopCertificateCache;
import org.junit.Assert;
import org.junit.Test;

import java.security.cert.X509Certificate;

/**
 * title: Yop平台证书工具类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/26
 */
public class YopCertificateCacheTest {

    @Test
    public void testLoadPlatformSm2Certs() {
        System.setProperty("yop.sdk.config.env", "qa");
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_test_sm.json");
        final YopCredentialsProvider provider = YopCredentialsProviderRegistry.getProvider();
        final X509Certificate byAppKey = YopCertificateCache.loadPlatformSm2Certs("app_15958159879157110002", "").get(0);
        final X509Certificate byDefault = YopCertificateCache.loadPlatformSm2Certs(provider.getDefaultAppKey(), "").get(0);
        Assert.assertNotNull(byAppKey);
        Assert.assertNotNull(byDefault);
        Assert.assertEquals(byAppKey, byDefault);
    }
}
