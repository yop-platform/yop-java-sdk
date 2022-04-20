/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.junit.Test;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021/7/28
 */
public class YopCredentialsProviderTest {

    YopCredentialsProvider credentialsProvider = YopCredentialsProviderRegistry.getProvider();

    @Test
    public void loadDefaultCredential() {
        System.setProperty("yop.sdk.config.env", "qa_single_default");
        final YopCredentials<?> credentialsDefault1 = credentialsProvider.getCredentials(null, CertTypeEnum.SM2.getValue());
        final YopCredentials<?> credentialsDefault2 = credentialsProvider.getCredentials(YopConstants.YOP_DEFAULT_APPKEY, "SM2");
        assert credentialsDefault1.getAppKey().equals("app_100800095600032");
        assert credentialsDefault1 instanceof YopPKICredentials
                && null != ((YopPKICredentials) credentialsDefault1).getCredential().getPrivateKey();

        assert credentialsDefault2.getAppKey().equals("app_100800095600032");
        assert credentialsDefault2 instanceof YopPKICredentials
                && null != ((YopPKICredentials) credentialsDefault2).getCredential().getPrivateKey();
    }

    @Test
    public void loadCredentialByAppkey() {
        System.setProperty("yop.sdk.config.env", "qa_single_app");
        final YopCredentials<?> credentials = credentialsProvider.getCredentials("app_100800095600032", CertTypeEnum.SM2.getValue());
        assert credentials.getAppKey().equals("app_100800095600032");
        assert credentials instanceof YopPKICredentials
                && null != ((YopPKICredentials) credentials).getCredential().getPrivateKey();
    }
}
