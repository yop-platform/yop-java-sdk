package com.yeepay.g3.sdk.yop;

import com.TrustAllHttpsCertificates;
import com.yeepay.g3.sdk.yop.client.YopClient;
import com.yeepay.g3.sdk.yop.client.YopClient3;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 15/7/8 10:23
 */
public class QaDemo {

    private static final String[] APP_KEYS = {"yop-boss", "jinkela"};
    private static final String[] APP_SECRETS = {"PdZ74F6sxapgOWJ31QKmYw==", "cAFj+DxhpeMo8afn7s0z5w=="};

    @Before
    public void setUp() throws Exception {
        TrustAllHttpsCertificates.setTrue();
        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_qa_docker.json");
    }

    @Test
    public void testIdCard() throws Exception {
        int i = 0;
        YopRequest request = new YopRequest();
//        request.setSignAlg("sha-256");
        request.addParam("appKey", APP_KEYS[i]);
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "370982199101186692");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient3.postRsa("/rest/v2.0/auth/idcard", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testQueryMemberAccount() throws Exception {
        int i = 0;
        YopRequest request = new YopRequest(null, APP_SECRETS[i]);
        request.addParam("customerNo", APP_KEYS[i]);
        request.addParam("requestId", "0");
        request.addParam("platformUserNo", "1234567890123456789012345673333");
        YopResponse response = YopClient.post("/rest/v1.0/member/queryAccount", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void v() throws IOException {
        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
        request.addParam("customerNo", "10040011444");
        request.addParam("merchantNo", "10040028626");
//        request.addParam("platformUserNo", "12345678901234567890123456789012");
        YopResponse response = YopClient.post("/rest/v1.0/merchant/queryBalance", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testEnterprise() throws Exception {
        YopRequest request = new YopRequest("yop-boss", "QFdODaBYBiVuLpP+sbyH+g==");
        request.addParam("appKey", "yop-boss");//这个写YOP就可以了
//        request.addParam("requestSystem", "YOP");//这个写YOP就可以了
        request.addParam("corpName", "青海韩都忆餐饮管理有限公司");//企业名称
        request.addParam("regNo", "630104063037404");//工商注册号
        request.addParam("requestCustomerId", "jinkela");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "wenkang.zhang");//请求者标识
        YopResponse response = YopClient.post("/rest/v1.2/auth/authenterprise", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testName1() throws Exception {
        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
        request.addParam("customerNo", "10040011444");
        request.addParam("requestId", "YOP-SDK-" + System.currentTimeMillis());
//		request.addParam("platformUserNo","YOP-USERNO-" + System.currentTimeMillis());
        request.addParam("platformUserNo", "8880222");
//		request.addParam("platformUserNo","YOP-USERNO-1435560994654");
        YopResponse response = YopClient.post("/rest/v1.0/merchant/queryAccount", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testName2() throws Exception {
        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
        request.addParam("customerNo", "10040011444");
        request.addParam("requestId", "YOP-SDK-" + System.currentTimeMillis());
        request.addParam("platformUserNo", "20150623143151652niuniu");
        request.addParam("orderNo", "YOP-SDK-ORDER-" + System.currentTimeMillis());
        request.addParam("amount", "20");
        request.addParam("payproducttype", "NET");
        request.addParam("bankid", "");
        request.addParam("callbackurl", "http://50.1.1.24:8018/fundtrans-hessian/");
        request.addParam("webcallbackurl", "http://localhost:8080/reciver/page");
        YopResponse response = YopClient.get("/rest/v1.0/member/gatewayDeposit", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void test1() throws IOException {
        YopRequest request = new YopRequest(null, "s5KI8r0920SQ339oVlFE6eWJ0yk019SD7015nw39iaXJp10856z0C1d7JV5l");
        request.addParam("customerNo", "10011830665");
        request.addParam("customernumber", "10012544672");
        request.addParam("requestid", System.currentTimeMillis());
        request.addParam("amount", "0.1");
        request.addParam("callbackurl", "http://www.baidu.com");
        request.addParam("webcallbackurl", "http://www.baidu.com");
        request.addParam("bankid", "ICBC");
        request.addParam("payproducttype", "SALES");
        YopResponse response = YopClient.post("/rest/v1.0/merchant/pay", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testValidate() throws IOException {
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
        request.setSignAlg("sha-256");
        request.addParam("appKey", "yop-boss");
        request.addParam("not_null", "10011830665");
        request.addParam("complex", "33647");
        request.addParam("not_blank", "张文康");

        request.addParam("length", "fkdjfld");
        request.addParam("range", "10");
        request.addParam("int", "3");
        request.addParam("email", "wenkang.zhang@yeepay.com");
        request.addParam("mobile", "15901189967");
        request.addParam("idcard", "370982199101186");

        YopResponse response = YopClient.get("/rest/v1.0/kong/validator", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testWhiteList() throws Exception {
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
        request.addParam("appKey", "yop-boss");
        request.setSignAlg("sha-256");
        request.addParam("name", "张文康");
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("idCardNumber", "370982199101186691");
        YopResponse response = YopClient.post("/rest/v2.0/auth/idcard", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testCreateToken() throws IOException {
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
        request.addParam("appKey", "yop-boss");

        request.addParam("grant_type", "password");//请求流水标识
        request.addParam("client_id", "appKey");
        request.addParam("authenticated_user_id", "wenkang.zhang");
        request.addParam("scope", "test");
        YopResponse response = YopClient.post("/rest/v1.0/oauth2/token", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testAmount() throws IOException {
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
        request.addParam("appKey", "yop-boss");

        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康", true);
        request.addParam("idCardNumber", "370982199101186691");
        request.addParam("bankCardNumber", "4392250043179877");

        YopResponse response = YopClient.post("/rest/v2.0/auth/debit3", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testJvmCollect() throws IOException {
        int i = 0;
        YopRequest request = new YopRequest(null, APP_SECRETS[i]);
        request.setSignAlg("sha-256");
//        request.addParam("customerNo", "10040011444");
        request.addParam("appKey", "yop-boss");

        YopResponse response = YopClient.post("/rest/v1.0/system/jvm", request);
        AssertUtils.assertYopResponse(response);
    }


    @Test
    public void testQueryMember() throws IOException {
        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
        request.addParam("customerNo", "10040011444");
        request.addParam("requestId", "YOP-SDK-" + System.currentTimeMillis());
        request.addParam("platformUserNo", "x");

        YopResponse response = YopClient.post("/rest/v1.0/member/queryAccount", request);
        AssertUtils.assertYopResponse(response);
    }

//    @Test
//    public void testUpLoadFile() {
//        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
//        request.addParam("fileType", "IMAGE");
//        request.addParam("appKey", APP_KEYS[0]);
//        request.addParam("_file", "file:/Users/zhangwenkang/Desktop/tomcat-lifecycle.png");

//        YopResponse response = YopClient.upload("/rest/v1.0/file/upload", request);
//        AssertUtils.assertYopResponse(response);
//    }

    @Test
    public void testSopay() throws IOException {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.setSignAlg("sha-256");
        request.addParam("appKey", APP_KEYS[0]);
        request.addParam("request_no", RandomStringUtils.randomAlphanumeric(20));
        request.addParam("uname", "13811112222");
        request.addParam("pwd", APP_KEYS[0]);
        request.addParam("imei", APP_KEYS[0]);

        YopResponse response = YopClient.post("/rest/v1.0/sopay/user/login", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testLaike() throws IOException {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.setSignAlg("sha-256");
        request.addParam("appKey", APP_KEYS[0]);
        request.addParam("request_no", RandomStringUtils.randomAlphanumeric(20));
        request.addParam("phone_no", "13811112222");
        request.addParam("location", "13811112222");
        request.addParam("version_id", "1");
        request.addParam("pwd", APP_KEYS[0]);
        request.addParam("imei", APP_KEYS[0]);

        YopResponse response = YopClient.post("/rest/v1.0/laike/login", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testLaikeToken() throws IOException {
        int i = 0;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i]);
        request.setSignAlg("sha-256");
        request.addParam("grant_type", "password");
        request.addParam("refresh_token", "123");//请求流水标识
        request.addParam("scope", "123");

        YopResponse response = YopClient.post("/rest/v1.0/laike/token", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testLoadMethod() throws IOException {
        int i = 0;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i]);
//        request.setSignAlg("sha-256");
        request.addParam("className", "com.yeepay.g3.facade.auth2.facade.Auth2Facade");

        YopResponse response = YopClient.post("/rest/v1.0/system/loader/methods", request);
        AssertUtils.assertYopResponse(response);
    }
}
