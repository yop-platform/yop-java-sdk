/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common;

import com.yeepay.yop.sdk.BaseTest;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.http.YopContentType;
import com.yeepay.yop.sdk.service.common.callback.YopCallback;
import com.yeepay.yop.sdk.service.common.callback.YopCallbackRequest;
import com.yeepay.yop.sdk.service.common.callback.YopCallbackResponse;
import com.yeepay.yop.sdk.service.common.callback.enums.YopCallbackHandleStatus;
import com.yeepay.yop.sdk.service.common.callback.handler.YopCallbackHandler;
import com.yeepay.yop.sdk.service.common.callback.handler.YopCallbackHandlerFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * title: Yop商户回调处理测试<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/16
 */
public class YopCallbackEngineTest extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopCallbackEngineTest.class);

    private static String callback = "/rest/v1.0/test/app-alias/create";

    @Before
    public void init() {
        System.setProperty("yop.sdk.config.env", "qa");
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_test_callback.json");
        MockYopCallbackHandler mockHandler = new MockYopCallbackHandler();
        YopCallbackHandlerFactory.register(mockHandler.getType(), mockHandler);
    }

    @Test
    public void testRsa() {
        YopCallbackRequest request =
                new YopCallbackRequest(callback,"POST")
                        .setContentType(YopContentType.FORM_URL_ENCODE)
                        .addParam("customerIdentification", "app_Fe51qCyZWcEnDMtK")
                        .addParam("response", "ZIcrArlonH0mxIWCRejL2VQS2qeK5EFz2gALzdMbusIU8eqwnWNgJRWiTwJElSQEhnT42KkU3jXWZr2dd0A8-bZSjT-hvNCUI0aoJZkadRtJrWoe_ygGhOLegj7cTbk8y7GOzfFQteIFbB9ALae1CqWVHgfgyozbTLgsse4MfuYjio9r3DOkCJJSkW6mEHB0G4rTXSWFni0h_Uhtu5jsuCTU4vWDPKrBIZI17rr1AIqmyOd8C8oLCAplC1JT4KnLq5QCir4cnvZJrGYB5-bI00gPOdGX2_v4Az3VqMkh8PqqPSDriJ-PqDo9T2dnjR5njYkTSSzUpIXg6cfhLaTNIQ$0sn1fsX0zRYXv-bb0tV531Brbhb-fPORrXYqe8JzHbnL8NkAwIPRkSaTXfq3etJnmslkkBlPpZeTc7639TWQlBrl3eVW-aQKIjFX4bhfyythIh5ByjBAHw1RaYwoHw10kkpbBBk01K-6pzE9QzT6TvjZLsSsXZ6O3WJdvrB8dtpJA-PI-sOzm7DXkBqfKOSufkN1C1mRvexBlcN3ScSH2TKo5ZwKw3Fo_93GsYFD0hzYmHpC6yCyHXeY1PPlHYqd_KsqXVo_xBtXMCadoKnldYnMljXdhAQJLRdlkwTgeD8FX18SQSJ18O6Ag0w3IM9QXkcgZVgIo1-_ZUncc5AtNXyQCvfT4tNyaIRFsXlFqj5tCc5bekMz8OzYeRTPfmfCLKXmjvg4ICMw0aIRboX1tyZpCHdHU269u0-wX90pMDNRqBZsLag6glNDSzEG8RQaB4vGrjvxYy0ixeUnogwni2qqnnGX5Gfhkst7FPYubAsi5HweDT_aJIrmE6kMiBrpMAOcIGZ6slYK854FOH3ODO9-raz7n2P__NUTpziTF4t4Jru_erJevVoGyHH81qq_msIMvK7IRx2z1QoExRL08A$AES$SHA256");
        YopCallbackResponse response = YopCallbackEngine.handle(request);
        Assert.assertEquals(response.getStatus(), YopCallbackHandleStatus.Success);
    }

    @Test
    public void testSm4() {
        YopCallbackRequest request =
                new YopCallbackRequest(callback,"POST")
                        .setContentType(YopContentType.FORM_URL_ENCODE)
                        .addParam("customerIdentification", "app_15958159879157110009")
                        .addParam("cipherText", "2MV2vk_W8NlpHxB3L8wdiBXVALVF13aNxmbwILLQiSNdWYSXN_aM2YZk1n-oPMdW-heEt9_cIyNb")
                        .addParam("algorithm", YopConstants.SM4_CALLBACK_ALGORITHM);
        YopCallbackResponse response = YopCallbackEngine.handle(request);
        Assert.assertEquals(response.getStatus(), YopCallbackHandleStatus.Success);
    }

    @Test
    public void testSm2() {
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Authorization", "YOP-SM2-SM3 yop-auth-v3/app_15958159879157110009/2022-05-17T02:33:24Z/1800/content-length;content-type;x-yop-appkey;x-yop-content-sm3;x-yop-encrypt;x-yop-request-id/fuKri2WjLqmr_gKInxstDLn6zz9XPR518TKK2iF9sMROSEcWllrAxApO4ldPrjNPPc0UsAbitCxnumA3-CJt8A$SM3");
        headers.put("x-yop-content-sm3", "eaa5391d992058fce198590bcfb7f7a4533d8ea311ac97c964513d7da080351f");
        headers.put("x-yop-encrypt", "yop-encrypt-v1/275568425014/SM4_CBC_PKCS5Padding/BEmuYglu6Y0M5jkqZN_yssw137rWIiaB0ToXJXsQytFDSwau5sMGnPKCnEe-2Bgg_ThowDqOdcGnsvzATS4ol4rk_fSPebBPMvnjyWZk5hpMYPJxCCEJ80MgHYE3pBt50LulUCaCYhYDyf4VO5rYyjQ/u3E2PbLDjeiZi9IeQm7xyA/stream//JA");
        headers.put("x-yop-request-id", "wuTest1652754804319");
        headers.put("x-yop-sign-serial-no", "275568425014");
        headers.put("x-yop-appkey", "app_15958159879157110009");
        headers.put("Content-Type", "application/json");
        YopCallbackRequest request =
                new YopCallbackRequest(callback, "POST")
                        .setContentType(YopContentType.JSON)
                        .setHeaders(headers)
                        .setContent("EZgjreIx_ZW-gIM2NtHoKSk2sMQ35eolEjZ76XPcCtEqbXRfv77Z2eUJHhfoN4TcAZjPykzzDJ2pH7FC8xbhXw");
        YopCallbackResponse response = YopCallbackEngine.handle(request);
        Assert.assertEquals(response.getStatus(), YopCallbackHandleStatus.Success);
    }

    private static class MockYopCallbackHandler implements YopCallbackHandler {

        @Override
        public String getType() {
            return callback;
        }

        @Override
        public void handle(YopCallback callback) {
            LOGGER.info("YopCallback handled success, callback:{}, type:{}", callback, getType());
        }
    }
}
