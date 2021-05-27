package com.yeepay.yop.sdk.http;


import com.yeepay.yop.sdk.http.reselut.AssertResponse;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.model.RequestConfig;
import com.yeepay.yop.sdk.model.yos.YosDownloadResponse;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QA {

    private YopClient yopClient;
    String configFile = "oneFileMoreApp.json";
//    String configFile = "yop_sdk_config_app_100400394480007.json";


    @Before
    public void setUp() throws Exception {
        System.setProperty("yop.sdk.config.env", "qa");
        System.setProperty("yop.sdk.config.file", configFile);
        System.setProperty("yop.sdk.http", "true"); //显示SDK http请求信息
        yopClient = YopClientBuilder.builder().build();

    }
    //get rsa
    @Test
    public void get(){
        String appkey="app_100400394480007"; //每次根据具体appkey修改
        YopRequest request = new YopRequest("/rest/v2.0/auth/idcard","GET");
        request.withRequestConfig(RequestConfig.Builder.builder().withAppKey(appkey).build());

        request.addParameter("requestFlowId","12345");
        request.addParameter("name","吴艳琳");
        request.addParameter("idCardNumber","410522198808137287");
       YopResponse response = yopClient.request(request);
        AssertResponse.assertResult(response);


//    assertEquals("调用网关失败", "SUCCESS", response.getState());
    }

    //文件下载
    @Test
    public void bindcardGetcardbinTestHttpClientFileDown() throws YopClientException {

        YopRequest yopRequest = new YopRequest("/yos/v1.0/settle/settle-file-query/get-settle-file-for-yop","GET");

        Map<String, String> headers = new HashMap<String, String>();
        yopRequest.withRequestConfig(RequestConfig.Builder.builder().withCustomRequestHeaders(headers).build());


        RequestConfig requestConfig = new RequestConfig();
        requestConfig.setCustomRequestHeaders(headers);
        requestConfig.setSecurityReq("YOP-RSA2048-SHA256");
        yopRequest.withRequestConfig(requestConfig);

        yopRequest.addParameter("merchantNo","10040039448");
        yopRequest.addParameter("appMerchantNo","app_100400394480006");
        yopRequest.addParameter("parentMerchantNo","10040039448");
        yopRequest.addParameter("settleRequestNo","20201220");
        yopRequest.addParameter("settleDate","2020-12-30");
        YosDownloadResponse yopResponse = yopClient.download(yopRequest);
        System.out.println(yopResponse);
    }


    @Test
    public void testOrder() {
        String appkey="app_100400394480007"; //每次根据具体appkey修改
        YopRequest request = new YopRequest("/rest/v1.0/sys/trade/order", "POST");
        request.withRequestConfig(RequestConfig.Builder.builder().withAppKey(appkey).build());
        request.addParameter("string0", "yop-test");
        YopResponse response = yopClient.request(request);
        System.out.println(response.getStringResult());
    }

}
