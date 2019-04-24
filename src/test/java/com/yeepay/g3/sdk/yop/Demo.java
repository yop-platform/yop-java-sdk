package com.yeepay.g3.sdk.yop;

import com.TrustAllHttpsCertificates;
import com.yeepay.g3.sdk.yop.client.YopClient;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class Demo {

    private static final String BASE_URL = "http://open.yeepay.com:8064/yop-center/";

    private static final String APP_KEY = "sopay";
    private static final String APP_SECERT = "RQbBAw3GmxOIjh0386owzA==";

    @Before
    public void setUp() throws Exception {
        TrustAllHttpsCertificates.setTrue();
    }

    @Test
    public void testIdCard() throws Exception {
        YopRequest request = new YopRequest(null, "0/ZoyfKku0tunPunw7dbfA==");
        request.setSignAlg("sha-256");
        request.addParam("appKey", "jinkela2");
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "370982199101186692");
        request.addParam("testDate", "2012-10-09 12:13:14");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v2.0/auth/idcard", request);
        System.out.println(response.toString());
    }

    @Test
    public void testQueryMemberAccount() throws Exception {
        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
        request.addParam("customerNo", "10040011444");
        request.addParam("requestId", "0");
        request.addParam("platformUserNo", "1234567890123456789012345673333");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/member/queryAccount", request);
        System.out.println(response.toString());
    }

    @Test
    public void v() throws IOException {
        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
        request.addParam("customerNo", "10040011444");
        request.addParam("merchantNo", "10040028626");
//        request.addParam("platformUserNo", "12345678901234567890123456789012");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/merchant/queryBalance", request);
        System.out.println(response.toString());
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
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.2/auth/authenterprise", request);
        System.out.println(response.toString());
    }

    @Test
    public void testName1() throws Exception {
        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
        request.addParam("customerNo", "10040011444");
        request.addParam("requestId", "YOP-SDK-" + System.currentTimeMillis());
//		request.addParam("platformUserNo","YOP-USERNO-" + System.currentTimeMillis());
        request.addParam("platformUserNo", "8880222");
//		request.addParam("platformUserNo","YOP-USERNO-1435560994654");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/merchant/queryAccount", request);
        System.out.println(response.toString());
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
        System.out.println(request.toQueryString());

        YopResponse yopResponse = YopClient.get("/rest/v1.0/member/gatewayDeposit", request);
        System.out.println(yopResponse);
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
        System.out.println(response.toString());
    }

    @Test//发送短信接口
    public void testSendSms() throws IOException {
        YopRequest request = new YopRequest("TestAppKey002", "TestAppSecret002");
        // request.setSignAlg("SHA1");
        request.setSignAlg("MD5");//具体看api签名算法而定
        String notifyRule = "fundauth_MOBILE_IFVerify";//通知规则
        List recipients = new ArrayList();//接收人
        recipients.add(0, "18253166342");
        String content = "{code:12345,something:something}";//json字符串，code，mctName为消息模板变量
        String extNum = "01";//扩展码
        String feeSubject = "0.01";//计费主体
        request.addParam("notifyRule", notifyRule);
        request.addParam("recipients", recipients);
        request.addParam("content", content);
        request.addParam("extNum", extNum);
        request.addParam("feeSubject", feeSubject);
        YopResponse response = YopClient.post("/rest/v1.0/notifier/send", request);
        System.out.println(response);
    }

    @Test
    public void testSendSmsQa() throws IOException {
        YopRequest request = new YopRequest("openSmsApi", "1234554321");
        request.setSignAlg("MD5");//具体看api签名算法而定
        String notifyRule = "商户结算短信通知";//通知规则
        List recipients = new ArrayList();//接收人
        recipients.add(0, "18253166342");
        String content = "{code:1235}";//json字符串，code，mctName为消息模板变量
        String extNum = "3";//扩展码
        String feeSubject = "0.01";//计费主体
        request.addParam("notifyRule", notifyRule);
        request.addParam("recipients", recipients);
        request.addParam("content", content);
        request.addParam("extNum", extNum);
        request.addParam("feeSubject", feeSubject);
        YopResponse response = YopClient.post("/rest/v1.0/notifier/send", request);
        System.out.println(response);
    }

    @Test
    public void testSendSmsProduct() throws IOException {
        try {
            TrustAllHttpsCertificates.setTrue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        YopRequest request = new YopRequest("ypo2o", "tpcY6k2RSpEod7hsJIp33Q==");
        request.setSignAlg("MD5");//具体看api签名算法而定
        String notifyRule = "EGOU_VERIFY";//通知规则
        List recipients = new ArrayList();//接收人
        recipients.add(0, "18519193582");
        String content = "{message1:123445}";//json字符串，code，mctName为消息模板变量
        String extNum = "52";//扩展码
        String feeSubject = "0.01";//计费主体
        request.addParam("notifyRule", notifyRule);
        request.addParam("recipients", recipients);
        request.addParam("content", content);
        request.addParam("extNum", extNum);
        request.addParam("feeSubject", feeSubject);
        YopResponse response = YopClient.post("/rest/v1.0/notifier/send", request);
        System.out.println(response);
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


        YopResponse response = YopClient.get("/yop-center/rest/v1.0/kong/validator", request);
        System.out.println(response.toString());
    }

    @Test
    public void testWhiteList() throws Exception {
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
        request.addParam("appKey", "yop-boss");
        request.setSignAlg("sha-256");
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "370982199101186691");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v2.0/auth/idcard", request);
        System.out.println(response.toString());
    }

    @Test
    public void testDebit3() throws Exception {
        for (int i = 0; i < 100; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
//                YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
//        YopRequest request = new YopRequest(null,
//                "cGB2CeC3YmwSWGoVz0kAvQ==",
//                "http://localhost:8064/yop-center");
//        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
//        YopRequest request = new YopRequest(null,
//                "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
//        request.setSignAlg("SHA1");
//        request.addParam("customerNo", "10040011444");
                    request.addParam("appKey", "yop-boss");

                    request.addParam("requestFlowId", "test123456");//请求流水标识
                    request.addParam("name", "张文康", true);
                    request.addParam("idCardNumber", "370982199101186691");
                    request.addParam("bankCardNumber", "4392250043179877");
                    System.out.println(request.toQueryString());
                    YopResponse response = null;
                    try {
                        response = YopClient.post("/rest/v2.0/auth/debit3", request);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println(response.toString());
                }
            }).start();
        }
//        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
////        YopRequest request = new YopRequest(null,
////                "cGB2CeC3YmwSWGoVz0kAvQ==",
////                "http://localhost:8064/yop-center");
////        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a");
////        YopRequest request = new YopRequest(null,
////                "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a",
////                "http://localhost:8064/yop-center");
////        request.setSignAlg("SHA1");
////        request.addParam("customerNo", "10040011444");
//        request.addParam("appKey", "yop-boss");
//
//        request.addParam("requestFlowId", "test123456");//请求流水标识
//        request.addParam("name", "张文康", true);
//        request.addParam("idCardNumber", "370982199101186691");
//        request.addParam("bankCardNumber", "4392250043179877");
//        System.out.println(request.toQueryString());
//        YopResponse response = YopClient.post("/rest/v2.0/auth/debit3", request);
//        System.out.println(response.toString());

        Thread.sleep(5000);

    }

    @Test
    public void testCreateToken() throws IOException {
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
//        YopRequest request = new YopRequest(null,
//                "cGB2CeC3YmwSWGoVz0kAvQ==",
//                "http://localhost:8064/yop-center");
//        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a", "http://127.0.0.1:7777");
//        YopRequest request = new YopRequest(null,
//                "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a",
//                "http://localhost:8064/yop-center");
//        request.setSignAlg("SHA1");
//        request.addParam("customerNo", "10040011444");
        request.addParam("appKey", "yop-boss");

        request.addParam("grant_type", "password");//请求流水标识
        request.addParam("client_id", "appKey");
        request.addParam("authenticated_user_id", "wenkang.zhang");
        request.addParam("scope", "test");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/oauth2/token", request);
        System.out.println(response.toString());
    }

    @Test
    public void testAmount() throws IOException {
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
//        YopRequest request = new YopRequest(null,
//                "cGB2CeC3YmwSWGoVz0kAvQ==",
//                "http://localhost:8064/yop-center");
//        YopRequest request = new YopRequest(null, "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a", "http://127.0.0.1:7777");
//        YopRequest request = new YopRequest(null,
//                "8intulgnqibv77f1t8q9j0hhlkiy6ei6c82sknv63vib3zhgyzl8uif9ky7a",
//                "http://localhost:8064/yop-center");
//        request.setSignAlg("SHA1");
//        request.addParam("customerNo", "10040011444");
        request.addParam("appKey", "yop-boss");

        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康", true);
        request.addParam("idCardNumber", "370982199101186691");
        request.addParam("bankCardNumber", "4392250043179877");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v2.0/auth/debit3", request);
        System.out.println(response.toString());
    }


    @Test
    public void testKong() throws Exception {
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
        request.setSignAlg("SHA-256");
        request.addParam("appKey", "yop-boss");
        request.addParam("idCardNumber", "370982199101186691");
        request.addParam("bankCardNumber", "370982114101186691");
        request.addParam("requestFlowId", "test1234567");//请求流水标识
        request.addParam("name", "张文康");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/kong/validator", request);
        System.out.println(response.toString());
    }

    @Test
    public void testKong2() throws Exception {
        YopRequest request = new YopRequest(null, "cGB2CeC3YmwSWGoVz0kAvQ==");
        request.setSignAlg("SHA-256");
        request.addParam("appKey", "yop-boss");
//        request.addParam("idCardNumber", "370982199101186691");
//        request.addParam("bankCardNumber", "370982114101186691");
        request.addParam("requestFlowId", "test1234567");//请求流水标识
//        request.addParam("name", "张文康");
        request.addParam("corpName", "南京大学");
//        request.addParam("regNo", "630104063035716");

        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v2.0/auth/enterprise", request);
        System.out.println(response.toString());
    }

    @Test
    public void testXue() throws IOException {
        YopRequest request = new YopRequest(null, "0owN80Vs39386sSSi7B76wa7497P41gZ3G4b8971V8R8sc6lS7ns4FA2846T");
        request.setSignAlg("sha-256");
        request.addParam("customerNo", "10040020578");
        request.addParam("merchantno", "10040020578");
        request.addParam("requestno", "YOP-SDK-" + System.currentTimeMillis());
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/paperorder/api/pay/query", request);
        System.out.println(response.toString());
    }

    @Test
    public void testLihui() throws IOException {
//        YopRequest request = new YopRequest("B112345678901237",
//                "nUXQx0Mt0aSKvR0uNOp6kg==");
//        request.setSignAlg("sha-256");
//        request.addParam("accountingType", "TRADE");
//        request.addParam("ppMerchantNo", "B112345678901237");
//        request.addParam("requestNo","requestNo1471431944138");
//        System.out.println(request.toQueryString());
//        YopResponse response = YopClient.post("/rest/v1.0/accounting/queryFee", request);
//        System.out.println(response.toString());

        YopRequest request = new YopRequest("B112345678901237", "nUXQx0Mt0aSKvR0uNOp6kg==");
        request.setSignAlg("sha-256");
        request.addParam("ppMerchantNo", "B112345678901237");
        request.addParam("trxRequestNo", "trx1472193269370");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/order/query", request);
        System.out.println(response);
    }

    @Test
    public void testHanfan() throws IOException {
        YopRequest request = new YopRequest("B112345678901239", "Vmt/sFQgWbJ6b4uKWLlFGw==");
        request.setSignAlg("sha-256");
        request.addParam("merchantNo", "B112345678901239");
        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/merchant/queryMerchantBalance", request);
        System.out.println(response.toString());
    }

    @Test
    public void testJiaoLinJie() {
        YopRequest request = new YopRequest(null, "ER59202j4EqI44c18L76iyjVjTl17c75268w15L9M5f7638A8l2zJ7UB0d54");//appKey,secretKey平台上秘钥,serverRoot服务路径
        //商户入网请求接口依赖外部系统：yop
        //http://open.yeepay.com:8064/yop-center  http://open.yeepay.com:18064/yop-center
        //https://open.yeepay.com/yop-center     yop-生产环境

        request.addParam("bizSystem", "QFT");//入网业务方
        request.addParam("customerNo", "10012442799");//10012442799  平台商商编
        //平台商编  10012442799  ER59202j4EqI44c18L76iyjVjTl17c75268w15L9M5f7638A8l2zJ7UB0d54
        //入网请求号
        request.addParam("requestNo", "request1473216975649");//通入网请求时保持一致
//        request.addParam("requestNo", "requestNo"+System.currentTimeMillis());//"request" + System.currentTimeMillis()
        request.addParam("subCustomerNo", "10013851721");//子商户编号
        request.addParam("merchantCategory", "042001");//商户一二级分类
        request.addParam("cashDepositType", "DELAYMONEY");//保证金缴纳方式：a滞留金（DELAYMONEY）；b线上充值（ONLINE）
        request.addParam("cashDepositAmount", "1000");//保证金金额
    }

    @Test
    public void testBase64() {
        String x = "+/dkjfdkjfs?kdjfkdjfkdjfkdjkdj";

        String base64UrlSafe = Base64.encodeBase64URLSafeString(x.getBytes());
        String base64 = "0/ZoyfKku0tunPunw7dbfA==";
        System.out.println(base64UrlSafe);
        System.out.println(base64);

        System.out.println(new String(com.yeepay.g3.sdk.yop.encrypt.Base64.decode(base64UrlSafe)));
        System.out.println(new String(com.yeepay.g3.sdk.yop.encrypt.Base64.decode(base64)));
        System.out.println(new String(new Base64(true).decode(base64UrlSafe)));
        System.out.println(new String(new Base64(true).decode(base64)));
    }
}
