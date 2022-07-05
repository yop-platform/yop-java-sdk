/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.signer.YopSigner;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessorFactory;
import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.crypto.YopCertCategory;
import com.yeepay.yop.sdk.crypto.YopCertParserFactory;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.SmUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.security.PrivateKey;
import java.util.List;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/4/23 6:09 下午
 */
public class Sm2SignAndVerifyTest {

    private static final String priKey = "MIICSwIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBVTCCAVECAQEEIJJjmqos2ap/Hf/qV6/FCRrnRwgZNOfLj3k+T6tLcDejoIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQGhRANCAASlKx2pDQcXfgdCZb8E8GZWUpIHkEBgkRnx+5tueXwcaMJ1om0qv1bxjxLUyHiVG3GOC0Qr1m2+rU+2lVTWYwP4";

    private static final String appKey = "app_100800095600038";

    static {
        System.setProperty("yop.sdk.http", "true");
        System.setProperty("yop.sdk.config.env", "qa");
        SmUtils.init();
    }

    public class YopTestSigner implements YopSigner {
        @Override
        public List<String> supportSignerAlg() {
            return Lists.newArrayList("TEST");
        }

        @Override
        public void sign(Request<? extends BaseRequest> request, YopCredentials<?> credentials, SignOptions options) {
        }
    }

    @Test
    public void testSign() {
        String content = "yop-auth-v3/app_100800095600038/2021-04-23T10:35:23Z/1800\n" +
                "POST\n" +
                "/rest/file/upload\n" +
                "\n" +
                "content-type:application%2Fx-www-form-urlencoded\n" +
                "x-yop-appkey:app_100800095600038\n" +
                "x-yop-request-id:c81634dc-9404-4cbe-8ccb-27269a7ced55";
        PKICredentialsItem pkiCredentialsItem = new PKICredentialsItem(getPrivateKey(priKey, CertTypeEnum.SM2), CertTypeEnum.SM2);
        String signature = YopSignProcessorFactory.getSignProcessor(pkiCredentialsItem.getCertType().name()).sign(content, pkiCredentialsItem);
        Assert.assertTrue(StringUtils.isNotEmpty(signature));
    }

    private PrivateKey getPrivateKey(String priKey, CertTypeEnum certType) {
        final YopCertConfig yopCertConfig = new YopCertConfig();
        yopCertConfig.setCertType(certType);
        yopCertConfig.setValue(priKey);
        yopCertConfig.setStoreType(CertStoreType.STRING);
        return (PrivateKey) YopCertParserFactory.getCertParser(YopCertCategory.PRIVATE, certType).parse(yopCertConfig);
    }

    @Test
    public void testVerify() {
        String serialNo = "275550212193";
        String signature = "XoIZA9Z1YKr1lraNuq62CoBOdmvr3Ae2Q9Fu0na8ugKvfDjoBWIEZ8z65BiXG1Ju5qPQ1Xn+RJALBx0bsQ3AEw==";
        String content = "{\"requestId\":\"3dd63639-bf35-427b-9110-f691ef00c20a\",\"code\":\"40042\",\"message\":\"非法的参数\",\"subCode\":\"isv.service.not-exists\",\"subMessage\":\"服务不存在\",\"docUrl\":\"http://10.151.31.146/docs/v2/platform/sdk_guide/error_code/index.html#platform_isv_service_not-exists\"}";
        YopSignProcessor yopSignProcessor = YopSignProcessorFactory.getSignProcessor(CertTypeEnum.SM2.name());
        YopPlatformCredentials yopPlatformCredentials = YopPlatformCredentialsProviderRegistry.getProvider().getCredentials(appKey, serialNo);
        boolean verifySuccess = yopSignProcessor.verify(content, signature, yopPlatformCredentials.getCredential());
        Assert.assertTrue(verifySuccess);
    }

}
