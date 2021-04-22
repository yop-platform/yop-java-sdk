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
import org.junit.Assert;
import org.junit.Test;

import java.security.PublicKey;

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
    @Test
    public void testRsaPlatformCert() {
        YopFileSdkConfigProvider yopFileSdkConfigProvider = (YopFileSdkConfigProvider) YopSdkConfigProviderRegistry.getProvider();
        YopFileSdkConfig yopFileSdkConfig = yopFileSdkConfigProvider.loadYopFileSdkConfig();
        YopCertConfig[] yopCertConfigs = yopFileSdkConfig.getYopPublicKey();
        PublicKey publicKey = YopCertConfigUtils.loadPublicKey(yopCertConfigs[0]);
        Assert.assertNotNull(publicKey);
    }
}
