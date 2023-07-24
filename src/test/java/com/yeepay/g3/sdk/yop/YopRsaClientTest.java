/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.sdk.yop.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/7/24
 */
public class YopRsaClientTest {

    @Test
    public void rsaForm() throws IOException {
        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_qa.json");
        final com.yeepay.g3.sdk.yop.client.YopRequest request = new com.yeepay.g3.sdk.yop.client.YopRequest();
        request.addParam("apiUri", "/rest/v1.0/test/old-api-mgr/find-api-by-uri");
        final com.yeepay.g3.sdk.yop.client.YopResponse response = com.yeepay.g3.sdk.yop.client.YopRsaClient.post("/rest/v1.0/test/old-api-mgr/find-api-by-uri", request);
        Assert.assertNotNull(response);
        Assert.assertTrue(((Map) response.getResult()).get("id").equals(5965));
    }

    @Test
    public void jsonSimple() throws IOException {
        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_qa.json");
        final com.yeepay.g3.sdk.yop.client.YopRequest request = new com.yeepay.g3.sdk.yop.client.YopRequest();
        request.setJsonParam(JsonUtils.toJsonString("test_wdc"));
        final com.yeepay.g3.sdk.yop.client.YopResponse response = com.yeepay.g3.sdk.yop.client.YopRsaClient.post("/rest/v1.0/test-wdc/product/find/rvs", request);
        Assert.assertNotNull(response);
        Assert.assertTrue(((Map) response.getResult()).get("id").equals(140));
        System.in.read();
    }

    @Test
    public void jsonComplex() throws IOException {
        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_qa.json");
        final com.yeepay.g3.sdk.yop.client.YopRequest request = new com.yeepay.g3.sdk.yop.client.YopRequest();
        final String jsonString =
                "{\n" +
                        "  \"arg1\" : {\n" +
                        "    \"appId\" : \"app_1111111111\",\n" +
                        "    \"customerNo\" : \"333333333\"\n" +
                        "  },\n" +
                        "  \"arg0\" : {\n" +
                        "    \"string\" : \"hello\",\n" +
                        "    \"array\" : [ \"test\" ]\n" +
                        "  }\n" +
                        "}";
        request.setJsonParam(jsonString);
        final com.yeepay.g3.sdk.yop.client.YopResponse response = com.yeepay.g3.sdk.yop.client.YopRsaClient.post("/rest/v1.0/test-wdc/test/http-json/test", request);
        Assert.assertNotNull(response);
        Assert.assertTrue(((List)((Map)(((Map) response.getResult()).get("testDTO"))).get("array")).get(0).equals("test"));
    }

}
