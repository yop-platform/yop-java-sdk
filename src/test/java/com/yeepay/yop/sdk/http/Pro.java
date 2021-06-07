package com.yeepay.yop.sdk.http;

//生产 内测

import com.yeepay.yop.sdk.model.RequestConfig;
import com.yeepay.yop.sdk.model.yos.YosDownloadResponse;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.service.common.response.YosUploadResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class Pro {
    private YopClient yopClient;
    String configFile = "oneFileMoreApp.json";

    @Before
    public void setUp() throws Exception {
        System.setProperty("yop.sdk.config.env", "nc");
        System.setProperty("yop.sdk.config.file", configFile);
        System.setProperty("yop.sdk.http", "true"); //显示SDK http请求信息

        yopClient = YopClientBuilder.builder().build();

    }


    // post 请求
    @Test
    public void oneFileMultiAppTest() {
        YopRequest request = new YopRequest("/rest/v1.0/test/product-query/query-for-doc", "POST");
        RequestConfig requestConfig = new RequestConfig();
        request.addParameter("string0", "yop-test");//这个写YOP就可以了
        YopResponse response = yopClient.request(request);
        System.out.println(response);
        AssertResponse.assertResult(response);
    }

    //get 、 下载文件
      @Test
    public void get() {
        String appkey = "app_10085575806"; //每次根据具体appkey修改  /yos/v1.0/test/bill-download/download-bill
        YopRequest request = new YopRequest("/yos/v1.0/bill/download", "GET");
        request.withRequestConfig(RequestConfig.Builder.builder().withAppKey(appkey).build());
        request.addParameter("clientId", appkey); //clientId :appkey  每次要变
        request.addParameter("billDate", "2021-05-13");//这个写YOP就可以了
        request.addParameter("merchantNo", "10085575806");
        request.addParameter("bizType", "UA_TRANSFER");

        YosDownloadResponse yosDownloadResponse = yopClient.download(request);
        System.out.println(yosDownloadResponse);
    }


    //上传文件
    @Test
    public void uploadpro_streamTest() throws Exception {
        String appkey = "app_100276759800039"; //每次根据具体appkey修改
        YopRequest request = new YopRequest("/yos/v1.0/sys/merchant/qual/upload", "POST");
        File file = new File("");
        String filePath = file.getCanonicalPath();
        request.withRequestConfig(RequestConfig.Builder.builder().withAppKey(appkey).build());
        request.addParameter("clientId", appkey); //clientId :appkey  每次要变
        System.out.println(filePath);
        request.addMutiPartFile("merQual", new File(filePath + "/src/test/resources/上传文件.png"));
        YosUploadResponse upload = yopClient.upload(request);
        AssertResponse.assertUpdateResult(upload);
    }

    @Test
    public void rsaTest() {
        String appkey = "app_100276759800039"; //每次根据具体appkey修改
        String authenticatedUserId = "10027675980"; //每次根据具体商户号修改
        //安全需求：OAUTH2和SM2
        YopRequest request = new YopRequest("/rest/v3.0/test/token/generate-token", "POST");
        request.withRequestConfig(RequestConfig.Builder.builder().withAppKey(appkey).build());
        request.addParameter("clientId", appkey); //clientId :appkey  每次要变
        request.addParameter("expiresIn", "7200");  //default: 7200
        request.addParameter("refreshTokenExpiresIn", "86400"); //default: 86400
        request.addParameter("authenticatedUserId", authenticatedUserId); //custom ID
        Set<String> scope = new HashSet<String>();
        scope.add("write:all");
        scope.add("read:all");
        request.addParameter("scope", scope);
        request.addParameter("grantType", "password");  //不知道该是什么值

        YopResponse response = yopClient.request(request);
        AssertResponse.assertResult(response);

        System.out.println(response);
    }

    @Test
    public void sm2() {
        String appkey = "app_100276759800044"; //每次根据具体appkey修改
        String authenticatedUserId = "10027675980"; //每次根据具体商户号修改
        //安全需求：OAUTH2和SM2
        YopRequest request = new YopRequest("/rest/v3.0/test/token/generate-token", "POST");
        request.withRequestConfig(RequestConfig.Builder.builder().withAppKey(appkey).build());
        request.addParameter("clientId", appkey); //clientId :appkey  每次要变
        request.addParameter("expiresIn", "7200");  //default: 7200
        request.addParameter("refreshTokenExpiresIn", "86400"); //default: 86400
        request.addParameter("authenticatedUserId", authenticatedUserId); //custom ID
        Set<String> scope = new HashSet<String>();
        scope.add("write:all");
        scope.add("read:all");
        request.addParameter("scope", scope);
        request.addParameter("grantType", "password");  //不知道该是什么值

        YopResponse response = yopClient.request(request);
        AssertResponse.assertResult(response);

        System.out.println(response);

    }

}