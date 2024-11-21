/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.BaseTest;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.YopContentType;
import com.yeepay.yop.sdk.service.common.callback.YopCallback;
import com.yeepay.yop.sdk.service.common.callback.YopCallbackRequest;
import com.yeepay.yop.sdk.service.common.callback.YopCallbackResponse;
import com.yeepay.yop.sdk.service.common.callback.enums.YopCallbackHandleStatus;
import com.yeepay.yop.sdk.service.common.callback.handler.YopCallbackHandler;
import com.yeepay.yop.sdk.service.common.callback.handler.YopCallbackHandlerFactory;
import org.apache.commons.lang3.StringUtils;
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

    private static String callbackPath = "/rest/v1.0/test/app-alias/create";
    private static String sm2CallbackPath = "/yop-callback/cs_1720925143663";

    @Before
    public void init() {
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_test_callback.json");
        MockYopCallbackHandler mockHandler = new MockYopCallbackHandler();
        MockSm2YopCallbackHandler mockSm2Handler = new MockSm2YopCallbackHandler();
        YopCallbackHandlerFactory.register(mockHandler.getType(), mockHandler);
        YopCallbackHandlerFactory.register(mockSm2Handler.getType(), mockSm2Handler);
    }

    @Test
    public void testRsa() {
        YopCallbackRequest request =
                new YopCallbackRequest(callbackPath,"POST")
                        .setProvider("yeepay")
                        .setEnv("qa")
                        .setContentType(YopContentType.FORM_URL_ENCODE)
                        .addParam("customerIdentification", "app_Fe51qCyZWcEnDMtK")
                        .addParam("response", "ZIcrArlonH0mxIWCRejL2VQS2qeK5EFz2gALzdMbusIU8eqwnWNgJRWiTwJElSQEhnT42KkU3jXWZr2dd0A8-bZSjT-hvNCUI0aoJZkadRtJrWoe_ygGhOLegj7cTbk8y7GOzfFQteIFbB9ALae1CqWVHgfgyozbTLgsse4MfuYjio9r3DOkCJJSkW6mEHB0G4rTXSWFni0h_Uhtu5jsuCTU4vWDPKrBIZI17rr1AIqmyOd8C8oLCAplC1JT4KnLq5QCir4cnvZJrGYB5-bI00gPOdGX2_v4Az3VqMkh8PqqPSDriJ-PqDo9T2dnjR5njYkTSSzUpIXg6cfhLaTNIQ$0sn1fsX0zRYXv-bb0tV531Brbhb-fPORrXYqe8JzHbnL8NkAwIPRkSaTXfq3etJnmslkkBlPpZeTc7639TWQlBrl3eVW-aQKIjFX4bhfyythIh5ByjBAHw1RaYwoHw10kkpbBBk01K-6pzE9QzT6TvjZLsSsXZ6O3WJdvrB8dtpJA-PI-sOzm7DXkBqfKOSufkN1C1mRvexBlcN3ScSH2TKo5ZwKw3Fo_93GsYFD0hzYmHpC6yCyHXeY1PPlHYqd_KsqXVo_xBtXMCadoKnldYnMljXdhAQJLRdlkwTgeD8FX18SQSJ18O6Ag0w3IM9QXkcgZVgIo1-_ZUncc5AtNXyQCvfT4tNyaIRFsXlFqj5tCc5bekMz8OzYeRTPfmfCLKXmjvg4ICMw0aIRboX1tyZpCHdHU269u0-wX90pMDNRqBZsLag6glNDSzEG8RQaB4vGrjvxYy0ixeUnogwni2qqnnGX5Gfhkst7FPYubAsi5HweDT_aJIrmE6kMiBrpMAOcIGZ6slYK854FOH3ODO9-raz7n2P__NUTpziTF4t4Jru_erJevVoGyHH81qq_msIMvK7IRx2z1QoExRL08A$AES$SHA256");
        YopCallbackResponse response = YopCallbackEngine.handle(request);
        Assert.assertEquals(response.getStatus(), YopCallbackHandleStatus.SUCCESS);
        Assert.assertTrue(null == response.getHeaders() || StringUtils.isBlank(response.getHeaders().get(Headers.YOP_SIGN)));
    }

    @Test
    public void testSm4() {
        YopCallbackRequest request =
                new YopCallbackRequest(callbackPath,"POST")
                        .setProvider("yeepay").setEnv("qa")
                        .setContentType(YopContentType.FORM_URL_ENCODE)
                        .addParam("customerIdentification", "app_15958159879157110009")
                        .addParam("cipherText", "2MV2vk_W8NlpHxB3L8wdiBXVALVF13aNxmbwILLQiSNdWYSXN_aM2YZk1n-oPMdW-heEt9_cIyNb")
                        .addParam("algorithm", YopConstants.SM4_CALLBACK_ALGORITHM);
        YopCallbackResponse response = YopCallbackEngine.handle(request);
        Assert.assertEquals(response.getStatus(), YopCallbackHandleStatus.SUCCESS);
        Assert.assertTrue(null == response.getHeaders() || StringUtils.isBlank(response.getHeaders().get(Headers.YOP_SIGN)));
    }

    @Test
    public void testSm2() {
        // 回调地址格式为，https://xxx/{path}，eventType为path部分加上"/"前缀
        // 举例说明：加入回调地址为"https://callback.test/payNotify"，则eventType为"/payNotify"
        String eventType = sm2CallbackPath;
        Map<String, String> headers = Maps.newHashMap();
        headers.put("Authorization", "YOP-SM2-SM3 yop-auth-v3/sandbox_sm_10080041523/2024-07-14T02:45:44Z/1800/content-length;content-type;x-yop-content-sm3;x-yop-encrypt;x-yop-request-id/GbzQbci-j9Qe2pC8lOwWejXo_1Cy8N5FD80xYKNhfX6d3LievqWPIanRLI8YL5UAb5AFJhcRpaax1BsxDQcHvA$SM3");
        headers.put("x-yop-content-sm3", "71a21d1557847e2865c9c5716490f16f4543686888cbdad6261afc015b4f91f7");
        headers.put("x-yop-encrypt", "yop-encrypt-v1//SM4_CBC_PKCS5Padding/BCbsJljzCjgpg7x1zK4z-EexCpADSpw0mSQL0ZXKOxyRDh_XSr_KUj7tX7GzJ-ouns1oT8MTrX76FYCp3pPQ-LElGwmvl-L3OpcaCP5ou9ABIH0zDrD8bCgm9eSLqiVLv_Jli6zh9VcWFsI-AZboUQo/4x2G0lyi51StUZjfPradeQ;eW9w/stream//JA");
        headers.put("x-yop-request-id", "cs_1720925143663");
        headers.put("x-yop-sign-serial-no", "4059376239");
        headers.put("x-yop-appkey", "sandbox_sm_10080041523");
        headers.put("Content-Type", "application/json");
        YopCallbackRequest request =
                new YopCallbackRequest(eventType, "POST")
                        .setProvider("yeepay").setEnv("qa")
                        .setContentType(YopContentType.JSON)
                        .setHeaders(headers)
                        .setContent("\"cdsnFDl8bVJSOuROSxNVDbLyxlDC0QGYvzZwTWxb0dMuTzDVwTCkATAmiiiUSfgOYoJlsz91qH6d1j0_TBdnSjN7EecCFZdo6MvatWIX5burxxLDAMAwo3QAmyiT5dVh72giQ-EIJAt8R_X5rO4PcA\"");
        YopCallbackResponse response = YopCallbackEngine.handle(request);
        Assert.assertEquals(response.getStatus(), YopCallbackHandleStatus.SUCCESS);
        Assert.assertTrue(StringUtils.isNotBlank(response.getHeaders().get(Headers.YOP_SIGN)));
    }

    private static class MockYopCallbackHandler implements YopCallbackHandler {

        @Override
        public String getType() {
            return callbackPath;
        }

        @Override
        public void handle(YopCallback callback) {
            LOGGER.info("YopCallback handled success, callback:{}, type:{}", callback, getType());
        }
    }

    private static class MockSm2YopCallbackHandler implements YopCallbackHandler {

        @Override
        public String getType() {
            return sm2CallbackPath;
        }

        @Override
        public void handle(YopCallback callback) {
            LOGGER.info("YopCallback handled success, callback:{}, type:{}", callback, getType());
        }
    }
}
