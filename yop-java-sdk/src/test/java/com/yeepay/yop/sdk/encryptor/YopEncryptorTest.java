/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.encryptor;

import com.google.common.collect.Maps;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.exception.YopServiceException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.internal.MultiPartFile;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.internal.RequestEncryptor;
import com.yeepay.yop.sdk.model.yos.YosDownloadInputStream;
import com.yeepay.yop.sdk.model.yos.YosDownloadResponse;
import com.yeepay.yop.sdk.security.encrypt.*;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.request.YopRequestMarshaller;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.utils.FileUtils;
import com.yeepay.yop.sdk.utils.JsonUtils;
import com.yeepay.yop.sdk.utils.StreamUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.yeepay.yop.sdk.YopConstants.*;
import static com.yeepay.yop.sdk.utils.CharacterConstants.DOLLAR;
import static org.junit.Assert.*;

/**
 * title: 加密器单元测试<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/13
 */
public class YopEncryptorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopEncryptorTest.class);

    YopEncryptor sm4Encryptor;
    YopEncryptor sm2Encryptor;
    Future<EncryptOptions> sm4Options;
    Future<EncryptOptions> sm4OptionsEnhanced;
    String appKey = "app_100800095600038";
    String credentialType = "SM2";
    YopCredentials<?> yopCredentials;
    EncryptOptions encryptOptions;
    String specialCharacters;
    YopClient yopClient;

    @Before
    public void init() throws ExecutionException, InterruptedException, IOException {
        System.setProperty("yop.sdk.config.env", "qa");
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_test_sm.json");
        yopCredentials = YopCredentialsProviderRegistry.getProvider().getCredentials(appKey, credentialType);
        sm4Encryptor = YopEncryptorFactory.getEncryptor(SM4_CBC_PKCS5PADDING);
        sm2Encryptor = YopEncryptorFactory.getEncryptor(YOP_CREDENTIALS_ENCRYPT_ALG_SM2);
        sm4Options = sm4Encryptor.initOptions(SM4_CBC_PKCS5PADDING, null);
        sm4OptionsEnhanced = sm4Encryptor.initOptions(SM4_CBC_PKCS5PADDING,
                Collections.singletonList(new EncryptOptionsEnhancer.Sm4Enhancer(appKey)));
        encryptOptions = sm4OptionsEnhanced.get();
        specialCharacters = IOUtils.toString(FileUtils.getResourceAsStream("/test.txt"), DEFAULT_ENCODING);
        yopClient = YopClientBuilder.builder().withEndpoint("http://ycetest.yeepay.com:30228/yop-center")
                .withYosEndpoint("http://ycetest.yeepay.com:30228/yop-center").build();
//        yopClient = YopClientBuilder.builder().withEndpoint("http://localhost:8064/yop-center")
//                .withYosEndpoint("http://localhost:8064/yop-center").build();
    }

    @Test
    public void testInitOptions() throws Exception {
        EncryptOptions options = sm4Options.get();
        assertNotNull(options);
        assertTrue(options.getEnhancerInfo().isEmpty());
        assertNull(options.getEncryptedCredentials());

        EncryptOptions optionsEnhanced = sm4OptionsEnhanced.get();
        assertNotNull(optionsEnhanced);
        Map<String, Object> enhancerInfo = optionsEnhanced.getEnhancerInfo();
        assertFalse(enhancerInfo.isEmpty());
        assertTrue(optionsEnhanced.getEncryptedCredentials().length() > 0);
    }

    @Test
    public void testSm4EncryptSimple() throws Exception {
        String plainText = "你好,hello";
        byte[] plainBytes = plainText.getBytes(DEFAULT_ENCODING);
        ByteArrayInputStream plainStream = new ByteArrayInputStream(plainBytes);

        String cipherText = sm4Encryptor.encryptToBase64(plainText, sm4Options.get());
        byte[] cipherBytes = sm4Encryptor.encrypt(plainBytes, sm4Options.get());
        InputStream cipherStream = sm4Encryptor.encrypt(plainStream, sm4Options.get());

        String decryptedText = sm4Encryptor.decryptFromBase64(cipherText, sm4Options.get());
        byte[] decryptedBytes = sm4Encryptor.decrypt(cipherBytes, sm4Options.get());
        InputStream decryptedStream = sm4Encryptor.decrypt(cipherStream, sm4Options.get());

        assertEquals(plainText, decryptedText);
        assertEquals(plainText, IOUtils.toString(decryptedStream, DEFAULT_ENCODING));
        assertEquals(plainText, new String(decryptedBytes, DEFAULT_ENCODING));
    }

    /**
     * 流式加解密，不能共享cipher，否则下次加解密须等上次的流完全读写完
     * Caused by: javax.crypto.IllegalBlockSizeException: last block incomplete in decryption
     * at org.bouncycastle.jcajce.provider.symmetric.util.BaseBlockCipher.engineDoFinal(Unknown Source)
     * at javax.crypto.Cipher.doFinal(Cipher.java:2051
     *
     * @throws Exception
     */
    @Test
    public void testSm4EncryptStream() throws Exception {
        InputStream plain1 = FileUtils.getResourceAsStream("/test.txt");
        InputStream plain2 = FileUtils.getResourceAsStream("/test.txt");
        InputStream cipher1 = sm4Encryptor.encrypt(plain1, sm4Options.get());
        InputStream cipher2 = sm4Encryptor.encrypt(plain2, sm4Options.get());
        InputStream decrypted1 = sm4Encryptor.decrypt(cipher1, sm4Options.get());
        InputStream decrypted2 = sm4Encryptor.decrypt(cipher2, sm4Options.get());
        assertEquals(specialCharacters, IOUtils.toString(decrypted2, DEFAULT_ENCODING));
        assertEquals(specialCharacters, IOUtils.toString(decrypted1, DEFAULT_ENCODING));
    }

    @Test
    public void testSm4EncryptRequest() throws Exception {
        // 无加密参数
        noneEncrypt();

        // form单参数加密
        formSingleParamEncrypt();

        // form多参数加密
        formMultiParamEncrypt();

        // form文件参数加密
        formMultipartParamEncrypt();

        // 单文件流式加密
        singleFileStreamEncrypt();

        // json参数整体加密
        jsonParamTotalEncrypt();

        // json参数部分加密(伪)
        for (String jsonPath : JSON_PATH_ROOT) {
            jsonParamsPeudoPartEncrypt(Collections.singletonList(jsonPath));
        }
        assertThrows("illegal json paths:[$, $..author]", RuntimeException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                jsonParamsPeudoPartEncrypt(Arrays.asList("$", "$..author"));
            }
        });

        // json参数部分加密(真)
        jsonParamsRealPartEncrypt();
    }

    @Test
    public void yopRequestGet() {
        YopRequest request = new YopRequest("/rest/v1.0/test/product-query/query-for-doc", "GET");
//        request.addParameter("string0", "le1");
        request.addEncryptParameter("string0", "le1");
        request.getRequestConfig().setAppKey("app_15958159879157110002");
        YopResponse response = yopClient.request(request);
        assertTrue(((Map) response.getResult()).get("id").equals(94));
    }

    @Test
    public void yopRequestForm() {
        YopRequest request = new YopRequest("/rest/v1.0/test-wdc/test/http-json", "POST");
        String paramString = "你好";
//        request.addParameter("string", paramString);
        request.addEncryptParameter("string", paramString);
        request.getRequestConfig().setAppKey("app_15958159879157110002");
        YopResponse resp = yopClient.request(request);
        assertTrue (((Map) ((Map) resp.getResult()).get("testDTO")).get("string").equals(paramString));
    }

    @Test
    public void yopRequestJsonSimple() {
        YopRequest request = new YopRequest("/rest/v1.0/test-wdc/product/find/rvs", "POST");
        request.getRequestConfig().setAppKey("app_15958159879157110002");
//        request.setContent(JsonUtils.toJsonString("test_wdc"));
        request.setEncryptContent(JsonUtils.toJsonString("test_wdc"));
        YopResponse response = yopClient.request(request);
        Assert.assertNotNull(response);
        Assert.assertTrue(((Map) response.getResult()).get("id").equals(140));
    }

    @Test
    public void yopRequestJson() {
        YopRequest request = new YopRequest("/rest/v1.0/test/app-alias/create", "POST");
        request.getRequestConfig().setAppKey("app_15958159879157110002");
//        request.setContent("{\"appId\":\"app_1595815987915711\",\"alias\":\"alias_0329\"}");
        request.setEncryptContent("{\"appId\":\"app_1595815987915711\",\"alias\":\"alias_0329\"}");
        try {
            yopClient.request(request);
        } catch (Exception e) {
            assertTrue(e instanceof YopServiceException);
            YopServiceException ex = (YopServiceException) e;
            assertEquals(ex.getSubErrorCode(),"isp.scene.unknown");
            assertEquals(ex.getSubMessage(),"别名(alias_0329)已经存在");
        }
    }

    @Test
    public void yopRequestUpload() throws Exception {
        YopRequest request = new YopRequest("/yos/v1.0/p2f/file-upload", "POST");
        request.getRequestConfig().setAppKey("app_15958159879157110002");

        File tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".txt");
        tmpFile.deleteOnExit();

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tmpFile);
            IOUtils.copy(new ByteArrayInputStream("hello".getBytes()), out);
        } finally {
            StreamUtils.closeQuietly(out);
        }

        request.addEncryptMutiPartFile("image", tmpFile);
        request.addParameter("requestNo",String.valueOf(System.currentTimeMillis()));
        request.addParameter("idNO","12345");
        request.addEncryptParameter("identityType","ID_CARD");
        request.addParameter("fileType","CERT_BACK");
        YopResponse response = yopClient.request(request);
        assertNotNull(response);
        assertEquals(((Map)response.getResult()).get("status"), "SUCCESS");
        assertEquals(((Map)response.getResult()).get("imageMd5"), "5d41402abc4b2a76b9719d911017c592");
        LOGGER.debug("fileUrl：{}", ((Map)response.getResult()).get("image"));
    }

    @Test
    // 加密后下载速度慢很多
    public void yopRequestDownload() {
        try {
            final long start = System.currentTimeMillis();
            YopRequest request = new YopRequest("/yos/v1.0/std/bill/fundbill/download", "GET");
            request.addParameter("fileId", "30343");
//            request.addParameter("merchantNo", "10040040287");
            request.addEncryptParameter("merchantNo", "10040040287");
            String appKey = "OPR:10040040287";
            request.getRequestConfig().setAppKey(appKey).setSecurityReq("YOP-SM2-SM3");

            YosDownloadResponse response = yopClient.download(request);
            YosDownloadInputStream yosDownloadInputStream = response.getResult();
            try {
                Assert.assertNotNull(yosDownloadInputStream);
                File tmpFile = File.createTempFile(UUID.randomUUID().toString(), ".zip");
                tmpFile.deleteOnExit();
                long size = IOUtils.copy(response.getResult(), new FileOutputStream(tmpFile));
                LOGGER.debug("downloaded file size:{}，elapsedTime:{}ms", size, System.currentTimeMillis() - start);
                Assert.assertTrue(size > 0);
            } catch (Exception e) {
                LOGGER.error("ex:", e);
            } finally {
                StreamUtils.closeQuietly(yosDownloadInputStream);
            }
        } catch (Exception e) {
            LOGGER.error("ex:", e);
        }
    }

    @Test
    public void yopRequestRsa() {
        YopRequest request = new YopRequest("/rest/v1.0/test/product-query/query-for-doc", "GET");
        request.addParameter("string0", "le1");
//        request.addEncryptParameter("string0", "le1");
        request.getRequestConfig().setAppKey("app_15958159879157110002");
        YopResponse response = yopClient.request(request);
        assertTrue(((Map) response.getResult()).get("id").equals(94));
    }

    private void formSingleParamEncrypt() throws Exception {
        YopRequest yopRequest = aFormRequest();
        yopRequest.addEncryptParameter("string0", "dsbzb");
        Request<YopRequest> request = YopRequestMarshaller.getInstance().marshall(yopRequest);
        YopEncryptProtocol.Inst encryptProtocol = parseProtocol(doEncrypt(request));
        assertTrue(encryptProtocol.getEncryptHeaders().isEmpty()
                && encryptProtocol.getEncryptParams().size() == 1
                && encryptProtocol.getEncryptParams().get(0).equals("string0"));
        assertEquals("dsbzb", sm4Encryptor.decryptFromBase64(
                request.getParameters().get("string0").get(0), encryptOptions));
    }

    private void formMultiParamEncrypt() throws Exception {
        YopRequest yopRequest = aFormRequest();
        yopRequest.addParameter("string0", "dsbzb");
        yopRequest.addEncryptParameter("string1", "mock1");
        yopRequest.addEncryptParameter("string2", "mock中国");
        yopRequest.addEncryptParameter("string3", specialCharacters);
        Request<YopRequest> request = YopRequestMarshaller.getInstance().marshall(yopRequest);
        YopEncryptProtocol.Inst encryptProtocol = parseProtocol(doEncrypt(request));
        assertTrue(encryptProtocol.getEncryptHeaders().isEmpty()
                && encryptProtocol.getEncryptParams().size() == 3
                && CollectionUtils.isEqualCollection(encryptProtocol.getEncryptParams(),
                Arrays.asList("string1", "string2", "string3")));
        assertEquals("dsbzb", request.getParameters().get("string0").get(0));
        assertEquals("mock1", sm4Encryptor.decryptFromBase64(
                request.getParameters().get("string1").get(0), encryptOptions));
        assertEquals("mock中国", sm4Encryptor.decryptFromBase64(
                request.getParameters().get("string2").get(0), encryptOptions));
        assertEquals(specialCharacters, sm4Encryptor.decryptFromBase64(
                request.getParameters().get("string3").get(0), encryptOptions));
    }

    private void formMultipartParamEncrypt() throws Exception {
        YopRequest yopRequest = aMultiPartFormRequest();
        yopRequest.addMultiPartFile("_file", getClass().getResourceAsStream("/test.txt"));
        yopRequest.addParameter("mock1", "不加密");
        yopRequest.addEncryptParameter("mock2", "加密");
        yopRequest.addEncryptMultiPartFile("_fileMock1", getClass().getResourceAsStream("/test.txt"));
        yopRequest.addEncryptMultiPartFile("_fileMock2", getClass().getResourceAsStream("/test1.txt"));
        Request<YopRequest> request = YopRequestMarshaller.getInstance().marshall(yopRequest);
        YopEncryptProtocol.Inst encryptProtocol = parseProtocol(doEncrypt(request));
        assertTrue(encryptProtocol.getEncryptHeaders().isEmpty()
                && encryptProtocol.getEncryptParams().size() == 3
                && CollectionUtils.isEqualCollection(encryptProtocol.getEncryptParams(),
                Arrays.asList("mock2", "_fileMock1", "_fileMock2")));

        assertEquals("不加密", request.getParameters().get("mock1").get(0));
        assertEquals("加密", sm4Encryptor.decryptFromBase64(
                request.getParameters().get("mock2").get(0), encryptOptions));

        Map<String, List<MultiPartFile>> multiPartFiles = request.getMultiPartFiles();
        assertEquals(specialCharacters,
                IOUtils.toString(multiPartFiles.get("_file").get(0).getInputStream(), DEFAULT_ENCODING));
        assertEquals(specialCharacters, IOUtils.toString(sm4Encryptor.decrypt(
                multiPartFiles.get("_fileMock1").get(0).getInputStream(), encryptOptions), DEFAULT_ENCODING));
        assertEquals("你好", IOUtils.toString(sm4Encryptor.decrypt(
                multiPartFiles.get("_fileMock2").get(0).getInputStream(), encryptOptions), DEFAULT_ENCODING));
    }

    private void singleFileStreamEncrypt() throws Exception {
        YopRequest yopRequest = aMultiPartFormRequest();
        yopRequest.setEncryptStream(getClass().getResourceAsStream("/test.txt"));
        Request<YopRequest> request = YopRequestMarshaller.getInstance().marshall(yopRequest);
        YopEncryptProtocol.Inst encryptProtocol = parseProtocol(doEncrypt(request));
        assertTrue(encryptProtocol.getEncryptHeaders().isEmpty()
                && encryptProtocol.getEncryptParams().get(0).equals(DOLLAR));

        assertEquals(specialCharacters, IOUtils.toString(sm4Encryptor.decrypt(
                request.getContent(), encryptOptions), DEFAULT_ENCODING));
    }

    private void jsonParamTotalEncrypt() throws Exception {
        YopRequest yopRequest = aJsonRequest();
        String jsonParam = jsonParam();
        yopRequest.setEncryptContent(jsonParam);

        Request<YopRequest> request = YopRequestMarshaller.getInstance().marshall(yopRequest);
        YopEncryptProtocol.Inst encryptProtocol = parseProtocol(doEncrypt(request));
        assertTrue(encryptProtocol.getEncryptHeaders().isEmpty()
                && encryptProtocol.getEncryptParams().get(0).equals(DOLLAR));

        assertJsonResult(jsonParam, request);
    }

    private void assertJsonResult(String jsonParam, Request<YopRequest> request) throws Exception {
        assertEquals(jsonParam, sm4Encryptor.decryptFromBase64(
                IOUtils.toString(request.getContent(), DEFAULT_ENCODING), encryptOptions));
    }

    private void jsonParamsPeudoPartEncrypt(List<String> jsonPath) throws Exception {
        YopRequest yopRequest = aJsonRequest();
        String jsonParam = jsonParam();
        yopRequest.setEncryptContent(jsonParam, jsonPath);

        Request<YopRequest> request = YopRequestMarshaller.getInstance().marshall(yopRequest);
        YopEncryptProtocol.Inst encryptProtocol = parseProtocol(doEncrypt(request));
        assertTrue(encryptProtocol.getEncryptHeaders().isEmpty()
                && encryptProtocol.getEncryptParams().get(0).equals(DOLLAR));

        assertJsonResult(jsonParam, request);
    }

    private void jsonParamsRealPartEncrypt() throws Exception {
        YopRequest yopRequest = aJsonRequest();
        String jsonParam = jsonParam();
        yopRequest.setEncryptContent(jsonParam, Arrays.asList("$.store..price", "$..book[?(@.author =~ /.*REES/i)]"));

        Request<YopRequest> request = YopRequestMarshaller.getInstance().marshall(yopRequest);
        YopEncryptProtocol.Inst encryptProtocol = parseProtocol(doEncrypt(request));
        assertTrue(encryptProtocol.getEncryptHeaders().isEmpty()
                && encryptProtocol.getEncryptParams().size() == 5);

        String encryptJson = IOUtils.toString(request.getContent(), DEFAULT_ENCODING);
        DocumentContext ctx = JsonPath.parse(encryptJson);
        Map<Object, Object> originJsonMap = jsonMap();
        for (String encryptParam : encryptProtocol.getEncryptParams()) {
            assertEquals(sm4Encryptor.decryptFromBase64(ctx.read(encryptParam), encryptOptions), originJsonMap.get(encryptParam));
        }
    }

    private Map<Object, Object> jsonMap() {
        Map<Object, Object> result = Maps.newHashMap();
        result.put("$['store']['bicycle']['price']", "19.95");
        result.put("$['store']['book'][0]", "{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":8.95}");
        result.put("$['store']['book'][1]['price']", "12.99");
        result.put("$['store']['book'][2]['price']", "8.99");
        result.put("$['store']['book'][3]['price']", "22.99");
        return result;
    }

    private YopRequest aJsonRequest() {
        YopRequest request = new YopRequest("/rest/v1.0/test-wdc/test/http-json", "POST");
        request.getRequestConfig().setAppKey(appKey).setNeedEncrypt(true);
        return request;
    }

    private YopRequest aMultiPartFormRequest() {
        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");
        request.getRequestConfig().setAppKey(appKey).setNeedEncrypt(true);
        return request;
    }

    private void noneEncrypt() throws Exception {
        YopRequest yopRequest = aFormRequest();
        yopRequest.addParameter("string0", "dsbzb");
        Request<YopRequest> request = YopRequestMarshaller.getInstance().marshall(yopRequest);
        YopEncryptProtocol.Inst encryptProtocol = parseProtocol(doEncrypt(request));
        assertTrue(encryptProtocol.getEncryptHeaders().isEmpty() && encryptProtocol.getEncryptParams().isEmpty());
        assertEquals(encryptOptions.getCredentials(), encryptProtocol.getEncryptOptions().getCredentials());
    }

    private YopEncryptProtocol.Inst parseProtocol(String encryptHeader) {
        YopEncryptProtocol.Inst encryptProtocol = YopEncryptProtocol.fromProtocol(encryptHeader).parse(new YopEncryptProtocol.ParseParams(encryptHeader,
                yopCredentials, encryptOptions));
        assertNotNull(encryptProtocol);
        assertEquals(encryptOptions.getCredentials(), encryptProtocol.getEncryptOptions().getCredentials());
        return encryptProtocol;
    }

    private String doEncrypt(Request<YopRequest> request) throws Exception {
        RequestEncryptor.encrypt(request, sm4Encryptor, encryptOptions);
        String encryptHeader = request.getHeaders().get(Headers.YOP_ENCRYPT);
        assertTrue(encryptHeader.startsWith(YOP_ENCRYPT_V1));
        return encryptHeader;
    }

    private YopRequest aFormRequest() {
        YopRequest yopRequest = new YopRequest("/rest/v1.0/test/product-query/query-for-doc", "GET");
        yopRequest.getRequestConfig().setNeedEncrypt(true).setAppKey(appKey);
        return yopRequest;
    }


    private String jsonParam() {
        String jsonParam = "{\n" +
                "    \"store\": {\n" +
                "        \"book\": [\n" +
                "            {\n" +
                "                \"category\": \"reference\",\n" +
                "                \"author\": \"Nigel Rees\",\n" +
                "                \"title\": \"Sayings of the Century\",\n" +
                "                \"price\": 8.95\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"Evelyn Waugh\",\n" +
                "                \"title\": \"Sword of Honour\",\n" +
                "                \"price\": 12.99\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"Herman Melville\",\n" +
                "                \"title\": \"Moby Dick\",\n" +
                "                \"isbn\": \"0-553-21311-3\",\n" +
                "                \"price\": 8.99\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"J. R. R. Tolkien\",\n" +
                "                \"title\": \"The Lord of the Rings\",\n" +
                "                \"isbn\": \"0-395-19395-8\",\n" +
                "                \"price\": 22.99\n" +
                "            }\n" +
                "        ],\n" +
                "        \"bicycle\": {\n" +
                "            \"color\": \"red\",\n" +
                "            \"price\": 19.95\n" +
                "        }\n" +
                "    },\n" +
                "    \"expensive\": 10\n" +
                "}";
        return jsonParam;
    }
}