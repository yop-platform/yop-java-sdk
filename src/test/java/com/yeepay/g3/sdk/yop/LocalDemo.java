package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.sdk.yop.client.*;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.hbird.HbirdLoginToken;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.model.PartETag;
import com.yeepay.g3.sdk.yop.utils.mapper.JsonMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
public class LocalDemo {

    protected static final Logger LOGGER = LoggerFactory.getLogger(LocalDemo.class);

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonDefaultMapper();

    private static final String[] APP_KEYS = {"yop-boss", "app_smtpZIFHFtrvLd9BR", "yop-boss"};
    private static final String[] APP_SECRETS = {"PdZ74F6sxapgOWJ31QKmYw==", "Iw4WPHO6hhjmOt7AoBP7HWWmdg6RMRsT6xZElc4RIAA=", "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCXsKWWClznZbdTwp9183e4Ygu/twbQhuS6LPpu/TZ+OFwwauvIZnOyKu+rFh6apKyVxiLEkssnTsBjLjUIlypEGU2SdLGkswWAPvVdunLjjWEz37W2w4VNkGf8bGCQ9fIxMynoBCTeeWcQz896e1y2p5YZHygUhXGLM/9q5mr3iQQgrEPdFEAdlfLexkbVIF2bS02NsDFLNvqKNk7219cefxWPgJfN7RukUIZyy4nbeevbMAAFpNUFh1NlAh4qzwocOfbZ3NgtwJDf29jibpM3dacS7tqYGwpeGpKazS9tZgTAYcX2kLT7s+G6vVzVQR61pvvDs5ubyfsw/KFR8KDDAgMBAAECggEAShSE6Z+p+4AbZhaYVbxPbYbEgh5af6BBOAMbUvTqlf3kV+j/uWD/g7WgUod87r0ZZBPdiu69tDarkkRQth9NDvDkh2/iCbM8LoOQxPN3hFXZcMICNn2KLnUls4siJelXHFwGTT8o2lWj1fwHMaPphXKWxTIIGu2IpBkC1iwtdTF8mqe2HH+H2djBE96JXVZIf3/FgGu8ppmXa/xG4DfrTxFnGEJzgaadT3Z+ybXbqjYgFgmmBnZOaTx1XPQfLGQVYJz9BunDhwhrqBUM+QuLr1jUsMsj/Yud52cNXjwq9z8FfkKUdVVfE4VrzH8JpKKk7Vim7RWBQER29jlEnV+ysQKBgQDjMWxZz4AveXxWSx7MgXN9PEzxzmGWSApseDskSi5PAmXa4ut5XyNJUiGJ8Zf+cssPfWFNtB7suJBuoMTtrQSap2tgoo70y7QSO0ZlZ0v5Ny9LYh8oHvDgBJVNmS5HWv1U1/VHxNHczNmQ05smXNo1bzMYe5Xo10J2W47UUTgOHwKBgQCq7G6B5RfD+O1jdmYWlilh5oi1XGdYJGnzhs9DmAUN5plQ3VxpUFxxQCgOwXCskfT9QUVYhsIpQIs2iCylwuNDuxxiEQyRpeBirRaqmxvosv08Trwsr1Vs/Cuh17ZZOS+OUehN0fDZCiruK4e2btVfv8LlE1KMuoiUsn1X2gWQ3QKBgCyqBrcRSA4NQBhm5EMoH+A6/pV7EUxOFV6FtHrJ6pi1y/hgLBLMVU+Qye8og80OHEWLTJnOE1ZOYnadPJnNLd6Jk16IFrqhYWFELe65hAIWi0GypJVqn8gqnn+G4cY9aRhI7HuTgf56dzs1nobIMk3W8qCZizsfNn22OjobTX3ZAoGBAJsTusvF1IMs5g05DjTt9wvpQx3xgZ46I5sdNA3q7qMHFxGEVeUDUWw7Plzs61LXdoUU5FsGoUEWW3iVopSett3r9TuQpmu7KVO+IXOXGYJOa259LUQJrKMeRGQpuDtJpDknXXLFyRTSodLH0fEWrCecb7KxjlM6ptLrAshjemtNAoGBAMzGo6aNER8VZfET8Oy0i5G8aVBp6yrMiQsNOj4S1VPoHI+Pc6ot5rDQdjek9PRzF9xeCU4K7+KLaOs6fVmTfsFpPbDafCTTmos9LGr5FIyXpU7LQCl3QPHWPDd5ezsu9SPVjzsEPX3WTSOJuUA8hE7pJnAzMHLGAFpIXJRu3Z/y"};

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_local.json");
//        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_dev.json");
//        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_qa.json");
//        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_pro.json");
//        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_10000470992.json");
//        System.setProperty("yop.sdk.trust.all.certs", "true");
    }

    @Test
    public void testAES_BASE64_URLEncode() throws Exception {
        YopRequest request = new YopRequest();
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "411122199104318257");

        YopResponse response = YopClient.post("/rest/v2.0/auth/enterprise", request);
        AssertUtils.assertYopResponse(response);
        if (response.isSuccess()) {
            Assert.assertNull(((Map) response.getResult()).get("result"));
        }
    }

    @Test
    public void testAES_sha1_verify_failed() throws Exception {
        YopRequest request = new YopRequest();
        request.setSignAlg("sha1");
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "370982199101186692");

        request.addHeader("Accept-Encoding", "gzip");

        YopResponse response = YopClient.get("/rest/v3.0/auth/idcard", request);
        assertEquals("40047", response.getError().getCode());
        assertEquals("isv.authentication.digest.verify-failure", response.getError().getSubCode());
    }

    @Test
    public void testAES_SHA1() throws Exception {
        YopRequest request = new YopRequest();
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "370982199101186692");

        YopResponse response = YopClient.post("/rest/v3.0/auth/idcard", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testAESYosFileUpload() throws Exception {
        YopRequest request = new YopRequest();
        request.setSignAlg("SHA256");
        request.addParam("request_flow_id", "test123456");//请求流水标识
        request.addParam("request_system", "YOP");//请求流水标识
        request.addParam("name", "谌猛浩");
        request.addParam("id_card_number", "411122199102218257");
        request.addFile("id_card_file", "src/test/resources/log4j2.xml");

        YopResponse response = YopClient.upload("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        Assert.assertNotNull(response.getHeaders().get(Headers.YOP_HASH_CRC64ECMA));
    }

    @Test
    public void testRSAYosFileUpload() throws Exception {
        YopRequest request = new YopRequest();
        request.setSignAlg("SHA256");
//        request.addParam("request_flow_id", "test123456");//请求流水标识
//        request.addParam("request_system", "YOP");//请求流水标识
//        request.addParam("name", "谌猛浩");
//        request.addParam("id_card_number", "411122199102218257");
        request.addFile("id_card_file", "src/test/resources/log4j2.xml");

        YopResponse response = YopClient.upload("/yos/v1.0/test123/auth2/auth-id-card", request);
        AssertUtils.assertYopResponse(response);
        Assert.assertNotNull(response.getHeaders().get(Headers.YOP_HASH_CRC64ECMA));
    }

    @Test
    public void testAES_SHA256() throws Exception {
        YopRequest request = new YopRequest();
        request.setSignAlg("sha-256");
        request.addParam("corpName", "安徽四创电子股份有限公司青海分公司");//企业名称
        request.addParam("regNo", "630104063035716");//工商注册号
        request.addParam("requestCustomerId", "yop-boss");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "unit test");//请求者标识

        YopResponse response = YopClient.get("/rest/v3.0/auth/enterprise", request);
        AssertUtils.assertYopResponse(response);

        response = YopClient.post("/rest/v3.0/auth/enterprise", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testAES256_SHA256() throws Exception {
        int i = 0;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i]);
        request.setSignAlg("sha-256");
        request.addParam("corpName", "安徽四创电子股份有限公司青海分公司");//企业名称
        request.addParam("regNo", "630104063035716");//工商注册号
        request.addParam("requestCustomerId", "yop-boss");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "unit test");//请求者标识

        YopResponse response = YopClient.get("/rest/v3.0/auth/enterprise", request);
        AssertUtils.assertYopResponse(response);

        response = YopClient.post("/rest/v3.0/auth/enterprise", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testAES256_SHA256_payplus() throws IOException {
        String merchant = "yop";
        YopRequest request = new YopRequest();
        request.setSignAlg("sha-256");
        request.addParam("requestNo", "trx" + RandomStringUtils.randomNumeric(10));
        request.addParam("merchantUserId", merchant);
        YopResponse response = YopClient.post("/rest/v1.0/payplus/user/register", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testCreateToken() throws IOException {
        YopRequest request = new YopRequest();
        request.setSignAlg("SHA-256");

        request.addParam("grant_type", "password");//请求流水标识
        request.addParam("client_id", "appKey");
        request.addParam("authenticated_user_id", "unit test");
        request.addParam("scope", "test");

        YopResponse response = YopClient.post("/rest/v1.0/oauth2/token", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test(timeout = 30000)
    public void testUpLoadFileOld() throws IOException {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.addParam("fileType", "IMAGE");
        request.addFile("_file", "src/test/resources/log4j2.xml");

        YopResponse response = YopClient.upload("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".xml"));
    }

    @Test(timeout = 30000)
    public void testUpLoadFileNew1() throws IOException {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.addParam("fileType", "IMAGE");
        request.addFile("src/test/resources/log4j.xml");

        YopResponse response = YopClient.upload("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".xml"));
    }

    @Test(timeout = 30000)
    public void testUpLoadFileNew2() throws IOException {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.addParam("fileType", "IMAGE");
        request.addFile(new File("src/test/resources/log4j2.xml"));

        YopResponse response = YopClient.upload("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".xml"));
    }

    @Test(timeout = 30000)
    public void testUpLoadFileNew3() throws IOException, URISyntaxException {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.addParam("fileType", "IMAGE");

        FileInputStream stream = new FileInputStream(new File("src/test/resources/log4j.xml"));
        request.addFile(stream);

        YopResponse response = YopClient.upload("/rest/v1.0/file/upload", request);

        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".png"));
    }

    @Test(timeout = 50000)
    public void testUpLoadFileNew4() throws IOException, URISyntaxException {
        YopRequest request = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request.setSignAlg("SHA-256");
        request.addParam("corp_id", "356e1dc1-4c11-419b-a043-cccb537dfb9b");
        request.addParam("user_name", "baitao.ji");
        request.addParam("password", "yeepay.com");
        request.addParam("need_corp_info", "true");
        request.addParam("need_token", "true");
        request.addParam("verified", "true");

        YopResponse response = YopClient.post("/rest/v2.0/hbird/oauth2/token", request);
        AssertUtils.assertYopResponse(response);

        YopRequest request2 = new YopRequest(APP_KEYS[0], APP_SECRETS[0]);
        request2.addParam("target_id", "010b3c35-f384-493f-919d-67177f70dda7");
        request2.addParam("target_type", "USER");
//        request2.addParam("fileBase64", Base64.encodeBase64String(IOUtils.toByteArray(new FileInputStream(new File("/Users/dreambt/test2.txt")))));

        HbirdLoginToken hbirdLoginToken = JSON_MAPPER.fromJson(response.getStringResult(), HbirdLoginToken.class);
        assert null != hbirdLoginToken.getoAuth2AccessToken();
        request2.addHeader("Authorization", "Bearer " + hbirdLoginToken.getoAuth2AccessToken().getValue());

        request2.addFile("fileBase64", new FileInputStream(new File("src/test/resources/log4j.xml")));

        response = YopClient.upload("/rest/v2.0/hbird/sharefile/upload", request2);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testRsa_auth() throws Exception {
        YopRequest request = new YopRequest();
        request.addParam("corpName", "安徽四创电子股份有限公司青海分公司");//企业名称
        request.addParam("regNo", "630104063035716");//工商注册号
        request.addParam("requestCustomerId", "yop-boss");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "unit test");//请求者标识

        request.setNeedEncrypt(true);

        YopResponse response = YopClient3.postRsa("/rest/v3.0/auth/enterprise", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testRsa_notifier() throws Exception {
        YopRequest request = new YopRequest();
        request.setSignAlg("sha-256");//具体看api签名算法而定
        String notifyRule = "ad_rule";//通知规则
        List<String> recipients = new ArrayList<>();//接收人
        recipients.add("18511620061");
        String content = "{\"code\":\"123445\"}";//json字符串，code为消息模板变量
        request.addParam("notifyRule", notifyRule);
        request.addParam("recipients", recipients);
        request.addParam("content", content);

        YopResponse response = YopClient3.postRsa("/rest/v4.0/notifier/send", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testRsa2() throws IOException {
        int i = 2;
        YopRequest request = new YopRequest(APP_KEYS[i], APP_SECRETS[i]);
        request.addParam("corpName", "安徽四创电子股份有限公司青海分公司");//企业名称
        request.addParam("regNo", "630104063035716");//工商注册号
        request.addParam("requestCustomerId", "yop-boss");//子商户编号
        request.addParam("requestFlowId", "test-" + System.currentTimeMillis() + RandomStringUtils.randomNumeric(3));//请求流水标识
        request.addParam("requestIdentification", "unit test");//请求者标识

        YopResponse response = YopClient3.postRsa("/rest/v1.0/sys/trade/order", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test(timeout = 30000)
    public void testRsaUploadFile() throws IOException {
        YopRequest request = new YopRequest();
        request.addParam("fileType", "IMAGE");
//        request.addParam("_file", "file:/Users/dreambt/xuekun-3.pfx");
        request.addParam("_file", "src/test/resources/log4j.xml");

        YopResponse response = YopClient3.uploadRsa("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".xml"));
    }

    @Test
    public void testRsaUploadFileNew1() throws IOException {
        YopRequest request = new YopRequest();
        request.addParam("fileType", "IMAGE");

        request.addFile("src/test/resources/log4j2.xml");

        YopResponse response = YopClient3.uploadRsa("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".xml"));
    }

    @Test(timeout = 30000)
    public void testRsaUploadFileNew2() throws IOException {
        YopRequest request = new YopRequest();
        request.addParam("fileType", "IMAGE");

        request.addFile(new File("src/test/resources/log4j.xml"));

        YopResponse response = YopClient3.uploadRsa("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".xml"));
    }

    @Test(timeout = 30000)
    public void testRsaUploadFileNew3() throws IOException, URISyntaxException {
        YopRequest request = new YopRequest();
        request.addParam("fileType", "IMAGE");

        FileInputStream stream = new FileInputStream(new File("src/test/resources/log4j.xml"));
        request.addFile(stream);

        YopResponse response = YopClient3.uploadRsa("/rest/v1.0/file/upload", request);

        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".png"));
    }

    @Test(timeout = 30000)
    public void testRsaUploadFileNewUseURL() throws IOException, URISyntaxException {
        YopRequest request = new YopRequest();
        request.addParam("fileType", "IMAGE");

        request.addFile(new URL("https://www.baidu.com/img/bd_logo1.png").openStream());

        YopResponse response = YopClient3.uploadRsa("/rest/v1.0/file/upload", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(0 < ((Integer) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileSize")));
        assertTrue(StringUtils.endsWith((String) ((HashMap) (((ArrayList) ((HashMap) response.getResult()).get("files")).get(0))).get("fileName"), ".png"));
    }

    @Test
    public void testOAuth2() throws Exception {
        YopRequest request = new YopRequest();
        request.addHeader(Headers.AUTHORIZATION, "Bearer eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJ5b3AiLCJqdGkiOiJhcHBfeE5UVTRORE13TURRMTphMzM1ZTMwNS1jYjUzLTQ4N2ItYmNhOS1kZGIwODhkYzFjMmEiLCJzdWIiOiJkODVlNzM2Yy0wNDZmLTRmMzctYjQyMC1jZjlhMTI1NDc0MjciLCJleHAiOjE1MjA0NzY0MTQsImNpZCI6IjQzIiwic2NvcGUiOltdLCJha19leHAiOjQzMjAwLCJya19leHAiOjI1OTIwMDAsInR5cGUiOiJyayIsInZlciI6IjIuMCJ9.tq87qVP2eOmr3em1MdwVwH0vkuET8MmQxJlkI-BM7IdgfNjMjB-yfJmtkAbgI6D7lOdsXmZxW13ZqS1j2WpWbQ");

        YopResponse response = YopClient.post("/rest/v2.0/hbird/qr/create-qr", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testLoadLocalClass() throws Exception {
        YopRequest request = new YopRequest();
        request.getAppSdkConfig().setServerRoot("http://ycetest.yeepay.com:30228/yop-center");
        request.addParam("className", "com.yeepay.g3.facade.xls.bankfront.facade.TradeOrderFacade");

        YopResponse response = YopClient.post("/rest/v1.0/system/loader/methods", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testLoadRemoteClass() throws Exception {
        YopRequest request = new YopRequest();
        request.addParam("className", "com.yeepay.g3.facade.opr.facade.yop.YopOrderFacade");

        YopResponse response = YopClient.post("/rest/v1.0/system/loader/methods", request);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testLoadClassTimeout() throws Exception {
        YopRequest request = new YopRequest();
        request.addParam("className", "com.yeepay.g3.facade.ymf.facade.laike.OrderFacade");

        YopResponse response = YopClient.post("/rest/v1.0/system/loader/methods", request);
        AssertUtils.assertYopResponse(response);
        assertTrue(StringUtils.contains("调用超过最大处理时长", response.getError().getMessage()));
    }

    @Test
    public void testConstraintViolationException() throws Exception {
        YopRequest request = new YopRequest();
        request.setSignAlg("SHA1");
        request.addParam("requestFlowId", "test123456");//请求流水标识
        request.addParam("name", "张文康");
        request.addParam("idCardNumber", "czr+7xY");

        YopResponse response = YopClient.post("/rest/v2.0/auth/idcard", request);
        AssertUtils.assertYopResponse(response);
        assertEquals("个人身份证号长度必须为15位或18位", response.getError().getMessage());
    }

    @Test
    public void testYeepayBizException() throws IOException {
        YopRequest request = new YopRequest();

        request.addParam("grant_type", "password0");//请求流水标识
        request.addParam("client_id", "appKey");
        request.addParam("authenticated_user_id", "unit test");
        request.addParam("scope", "test");

        YopResponse response = YopClient.post("/rest/v1.0/oauth2/token", request);
        assertEquals("40029", response.getError().getCode());
        assertEquals("isp.code.invalid_grant", response.getError().getSubCode());
    }

    @Test
    public void testPayPlusRemitQuery() throws IOException {
        YopRequest request = new YopRequest();
        request.setSignAlg("sha-256");
//        request.addParam("trxRequestNo","111");
        request.addParam("remitRequestNo", "Remit1534859751218");

        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/payplus/remit/query", request);
        System.out.println(response.toString());
    }

    @Test
    public void testNoProvider() throws IOException {
        YopRequest request = new YopRequest();
        request.setSignAlg("sha-256");
//        request.addParam("trxRequestNo","111");
        request.addParam("remitRequestNo", "Remit1534859751218");

        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/mt-wallet/order/queryCodeInfo", request);
        System.out.println(response.toString());
    }

    @Test
    public void testDisabledApi() throws IOException {
        YopRequest request = new YopRequest();
        request.setSignAlg("sha-256");
//        request.addParam("trxRequestNo","111");
        request.addParam("remitRequestNo", "Remit1534859751218");

        System.out.println(request.toQueryString());
        YopResponse response = YopClient.post("/rest/v1.0/insordering-interface/car-insordering-interface/create-policy", request);
        System.out.println(response.toString());
    }

    @Test
    public void name() {
        YopRequest request = new YopRequest("yop-boss", "PdZ74F6sxapgOWJ31QKmYw==");
        request.setSignAlg("SHA-256");
        request.addParam("backendLatency", "100");

        YopResponse response = null;
        try {
            StopWatch stopWatch = StopWatch.createStarted();
            response = YopClient.post("/rest/v1.0/yop/mock/backend-latency", request);
            assertTrue(response.isSuccess());
//                assertTrue(response.isValidSign());
            stopWatch.stop();
            System.out.println("stopWatch:" + stopWatch.getTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void name1() throws IOException {
        //secretKey:商户私钥（字符串形式）
        String OPRkey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDKLgI+64mmJdNg1TwlCPBnNH3b3qfw2TdHVc2uDd4LTyQI8nRr0heFhhdj0OZi6agqekIyzAH/XmO9PdLrTi4YXJXOfiO/dYwKA6gSktRe6FKY4C2WzX1yA4fGfqJMV7RYVoL6In50Hur6rGnavNSQZqbiDJOgy5yokJ14Mey1iMqqqWvADtKN9SqxtbyIxYD/jj/6qLWwmu88wSwSaGdO3wNFgzajsHgRJe9G9IhD0zr5d72HvJGoedq7VaPn3jhIszcPQE6oqbXAddZRGKBehA4WSCjLEl87XH33zZPrxrQlBTHVVGzfxjbB4QvYz0hlEoWh1ntxeDHTfgyhdPQpAgMBAAECggEATmxMSLW6Xe08McpkmwT9ozq0Oy4BvKW1EIGS15nfcEmRc7sAN7Z1k0BxIDGuu91gcqGbvfJuL+0gCQ7LGqTnsmFvZnp9SU3CNTw33ISBxhKdv1jtthodN7Vw3CjQsYYvmThtc7Mfk9FOWk+4e7VVSnHW98XjGbMBIE2AF1heNgeZ40ubdgzuz9+4g4pphjWncPpwcaMfsDZm3JtFyvUp0+LME0CmUqrxvONZAkpFR/PyejGHnIh3ptHzhe/VjNcuIC4PphkCNBakCBCrtohTy0YeeWfDAUTAO4tPXF/JUhlxjPuqR6rpQY/0uQdMAtTpiWHVJar7eGdK81QnuuOFRQKBgQDrklUPM0pkvGG/wREa0bgUI+ki+1/wv7O8X94/8onomJqPpkD8z4hv/Lev/wD5gDcgmgLC36u/XDuhFfVNOmw4eUWenU6pzonroEjhi91AKcRRfzDfOfWg3wPm1J9WQOn5A033tNRydCpVcX/Ot4qDbKcAwLiPNPXXMTn4LUQE/wKBgQDbtmE0KS/kSfjscWJOqwv1XbxckipkxncqIbdiSdU+DzaLd+Vuaco7TLQJRFp7S7WJW4Tz6KBX2UiA7O7ezXY9PwlgXxXiZDDtneXNAqk7DNxmTTZHrF2C7qdU98klppCFiFx9bysGY6lFWofWmg3Pu5IiPqO3iLRPTvZgQOE+1wKBgQC9SCgmfYzyIlfcjtIinY5uSGiEnjz5od9WpiVbdpOPHEdc0zZ2rH6xlPs3ZAuxbm9dN8KuOLC0ovSau50Nv7rDKdZh234gfP9fH7xP1mUhsC25Why30MdnyqpE6GVbFe+qERitx1PI30RAwWDzhZC7hystNK1XDDPZBAnTOvPjmwKBgDFuujX7IkxRnFDOPdkHQNyGp2+Ib0NXJ85x4YmapQCeeZ4tbpBF+vsWidcf6t+crA5oaeRarWC2gUqIhEHapkSnXxuwqQLTmfKMOPzEIYEoppnZu2Gq1Ss1OK60RSxUamWwxWZvUZXRbG8vLCrLZFodkIZl433SowbI9EO5tTPnAoGAJRsy1z95Q1GPkKrFtKivkxZy1k7zJXjM0VWDc7lT9fBnoeGUyt+vuq+lC5i2aiWKJK7pe8MM9QFDGlWPnly+J8jbyMfm99k5oJtCWDfF0or1pAQ4mw0kjL9TvDVXdojgYA+rxSMQ09hwsYukQ4bblrwfBUmRjLN5WibcRzIW5ZA=";

        //step1  生成yop请求对象
        //arg0:appkey（举例授权扣款是SQKK+商户编号，亿企通是OPR:+商户编号，具体是什么请参考自己开通产品的手册。
        //arg1:商户私钥字符串
        YopRequest request = new YopRequest("OPR:10000466938", OPRkey);

        //step2 配置参数
        //arg0:参数名
        //arg1:参数值
        request.addParam("merchantNo", "10000466938");
        request.addParam("dayString", "2018-11-20");


        //step3 发起请求
        //arg0:接口的uri（参见手册）
        //arg1:配置好参数的请求对象
        YopResponse response = YopClient3.postRsa("/yos/v1.0/std/bill/tradedaydownload", request);
        assertNotNull(response);
        assertNotNull(response.getResult());

//        InputStream retstream= response.getFile();
//        byte[] bytes = new byte[0];
//        bytes = new byte[retstream.available()];
//        retstream.read(bytes);
//        String str = new String(bytes);

        System.out.println(IOUtils.toString(response.getFile()));
    }

    String apiUri = "/yos/v1.0/file/multipart/upload";
    String bizCode = "testBizCode";
    String bucket = "yop-file-upload-api";
    String key = "multipart-yop-boss-1566897556443-42rZYlU5QpSmZ5TWAL2C5g.bin";
    String uploadId = "2~c4DZmYePnPQnl-CuzY6WKO6Dqjni3dp";

    @Test
    public void testInitMultipartUpload() throws Exception {
        InitiateMultipartUploadRequest req = new InitiateMultipartUploadRequest();
        req.setApiUri(apiUri);
        req.setBizCode(bizCode);
        req.setFileName("产品培训－易宝开放平台.pdf");
        YopResponse response = YopRsaClient.initMultipartUpload(req);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testUploadPart() throws Exception {
        List<PartETag> partETags = new ArrayList<PartETag>();
        //Object file = new File("src/test/resources/log4j2.xml");//2175字节
        //Object file = new URL("https://www.baidu.com/img/bd_logo1.png").openStream();//7787字节
        Object file = new File("/Users/yp-tc-m-7042/Downloads/产品培训－易宝开放平台.pdf");//5.6M
        //Object file = new File("/Users/yp-tc-m-7042/Downloads/node-v10.15.3.pkg");//16.6M
        //Object file = new File("/Users/yp-tc-m-7042/Downloads/robo3t-1.3.1-darwin-x86_64-7419c40.dmg");//26.4M
        InputStream inputStream = null;
        ByteArrayInputStream bais = null;
        long totalRead = 0L;
        try {
            //遍历分片上传
            for (int i = 0; ; i++) {
                UploadPartRequest req = new UploadPartRequest();
                req.setApiUri(apiUri);
                long startPos = i * YopConstants.FILE_MULTIPART_PART_SIZE;
                Pair<Boolean, InputStream> pair = YopRsaClient.getInputStreamPair(file);
                Boolean skipFlag = pair.getLeft();
                inputStream = pair.getRight();
                if (skipFlag) {
                    //跳过已经上传的分片
                    inputStream.skip(startPos);
                }
                int partSize = (int) YopConstants.FILE_MULTIPART_PART_SIZE;
                byte[] bytes = new byte[partSize];
                int offset;
                int readed = 0;
                for (offset = 0; offset < partSize && (readed = inputStream.read(bytes, offset, partSize - offset)) != -1; offset += readed) {

                }
                if (offset <= 0 && readed == -1) {
                    break;
                }
                totalRead = totalRead + offset;
                if (totalRead > YopConstants.FILE_MULTIPART_UPLOAD_SIZE) {
                    throw new YopClientException("one file should not be more than 25M.");
                }
                bais = new ByteArrayInputStream(bytes, 0, offset);
                req.setFile(bais);
                //设置分片号。每一个上传的分片都有一个分片号
                req.setUploadId(uploadId);
                req.setPartNumber(i + 1);
                req.setPartSize(offset);
                req.setBucket(bucket);
                req.setKey(key);
                YopResponse response = YopRsaClient.uploadPart(req);
                AssertUtils.assertYopResponse(response);
                Assert.assertNotNull(response.getHeaders().get(Headers.YOP_HASH_CRC64ECMA));
                //每次上传分片之后，返回结果会包含一个PartETag。PartETag将被保存到partETags中。
                partETags.add(new PartETag(i + 1, response.getHeaders().get(Headers.ETAG)));
            }
            System.out.println(partETags);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bais != null) {
                    bais.close();
                }
            } catch (IOException ioEx) {
                LOGGER.error("upload file exception occurred when close is", ioEx);
            }
        }
    }

    @Test
    public void testCompleteMultipartUpload() throws Exception {
        CompleteMultipartUploadRequest req = new CompleteMultipartUploadRequest();
        req.setApiUri(apiUri);
        req.setUploadId(uploadId);
        req.setBucket(bucket);
        req.setKey(key);
        List<PartETag> partETags = new ArrayList<PartETag>();
        partETags.add(new PartETag(1, "0361c7d565465107c701c9ca2f12dafc"));
        partETags.add(new PartETag(2, "489e91619e84a9f3e4f445e48cafbb6a"));
        req.setParts(partETags);
        YopResponse response = YopRsaClient.completeMultipartUpload(req);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testAbortMultipartUpload() throws Exception {
        AbortMultipartUploadRequest req = new AbortMultipartUploadRequest();
        req.setApiUri(apiUri);
        req.setUploadId(uploadId);
        req.setBucket(bucket);
        req.setKey(key);
        YopResponse response = YopRsaClient.abortMultipartUpload(req);
        AssertUtils.assertYopResponse(response);
    }

    @Test
    public void testUploadSmallFile() throws Exception {
        UploadFileRequest req = new UploadFileRequest();
        req.setApiUri(apiUri);
        req.setBizCode(bizCode);
        req.setUploadFlag(YopConstants.MULTIPART_UPLOAD_FLAG_1);
        req.setFile("src/test/resources/log4j2.xml");
        //req.setFile(new File("src/test/resources/log4j2.xml"));
        //req.setFile(new FileInputStream(new File("src/test/resources/log4j2.xml")));
        //req.setFile(new URL("https://www.baidu.com/img/bd_logo1.png").openStream());
        //req.setFile(new FileInputStream(new File("/Users/yp-tc-m-7042/Downloads/产品培训－易宝开放平台.pdf")));
        //req.setFile(new FileInputStream(new File("/Users/yp-tc-m-7042/Downloads/robo3t-1.3.1-darwin-x86_64-7419c40.dmg")));
        //req.setFileName("log4j2.png");
        YopResponse response = YopRsaClient.multipartUpload(req);
        AssertUtils.assertYopResponse(response);
        Assert.assertNotNull(response.getHeaders().get(Headers.YOP_HASH_CRC64ECMA));
    }

    @Test
    public void testMultipartUpload() throws Exception {
        UploadFileRequest req = new UploadFileRequest();
        req.setApiUri(apiUri);
        req.setBizCode(bizCode);
        req.setUploadFlag(YopConstants.MULTIPART_UPLOAD_FLAG_2);
        //req.setFile(new FileInputStream(new File("src/test/resources/log4j2.xml")));//2175字节
        //req.setFile(new URL("https://www.baidu.com/img/bd_logo1.png").openStream());//7787字节
        //req.setFile(new FileInputStream(new File("/Users/yp-tc-m-7042/Downloads/产品培训－易宝开放平台.pdf")));//5.6M
        //req.setFile(new FileInputStream(new File("/Users/yp-tc-m-7042/Downloads/node.pkg")));//16.6M
        //req.setFileName("node-v10.15.3.pkg");
        req.setFile(new FileInputStream(new File("/Users/yp-tc-m-7042/Downloads/robo3t-1.3.1-darwin-x86_64-7419c40.dmg"))); //26.4M
        YopResponse response = YopRsaClient.multipartUpload(req);
        AssertUtils.assertYopResponse(response);
    }

}
