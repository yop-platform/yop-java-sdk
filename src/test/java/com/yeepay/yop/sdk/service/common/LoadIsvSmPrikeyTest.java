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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.Security;
import java.util.List;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/4/23 4:32 下午
 */
public class LoadIsvSmPrikeyTest {
    private static final String priKey = "MIICSwIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBVTCCAVECAQEEIJJjmqos2ap/Hf/qV6/FCRrnRwgZNOfLj3k+T6tLcDejoIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQGhRANCAASlKx2pDQcXfgdCZb8E8GZWUpIHkEBgkRnx+5tueXwcaMJ1om0qv1bxjxLUyHiVG3GOC0Qr1m2+rU+2lVTWYwP4";

    @Before
    public void setUp() {
        System.setProperty("yop.sdk.http", "true");
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_test_sm.json");
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testLoadSm2PrivateKey() {
        String appKey = "app_100800095600038";
        YopFileSdkConfigProvider yopFileSdkConfigProvider = (YopFileSdkConfigProvider) YopSdkConfigProviderRegistry.getProvider();
        YopFileSdkConfig yopFileSdkConfig = yopFileSdkConfigProvider.loadSdkConfig(appKey);
        List<YopCertConfig> isvPriKeys = yopFileSdkConfig.getIsvPrivateKey(appKey);
        Assert.assertTrue(isvPriKeys.size() == 2);
        for (YopCertConfig yopCertConfig : isvPriKeys) {
            String privateKey = YopCertConfigUtils.loadPrivateKey(yopCertConfig);
            Assert.assertTrue(priKey.equals(privateKey));
        }
    }

}
