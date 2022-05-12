package com.yeepay.yop.sdk.service.common;

import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.exception.YopServiceException;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2020/11/18 上午10:56
 */
@RunWith(Parameterized.class)
public class YopClientTest {

    private String appId;
    private String securityReq;

    public YopClientTest(String appId, String securityReq) {
        this.appId = appId;
        this.securityReq = securityReq;
    }

    @Parameterized.Parameters
    public static Collection securityReq() {
        return CredentialsRepository.getApps();
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty("yop.sdk.config.env", "qa");
        System.setProperty("yop.sdk.http", "true");

        // 应用级配置
//        YopSdkConfigProviderRegistry
    }

    @Test
    public void requestWithDefaultSingleApp() {
        System.setProperty("yop.sdk.config.env", "qa_single_default");

        // Client 级配置
        YopClient yopClient = YopClientBuilder.builder()
//                .withYopSdkConfigProvider(you diy provider)
//                .withCredentialsProvider(your diy provider)
                .build();

        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

        // Request 级配置
//        request.getRequestConfig().setSecurityReq("");

        YopResponse response = yopClient.request(request);
        System.out.println(response);
    }

    @Test
    public void requestWithDefaultMultiApp() {
        // 使用该配置文件初始化SDK，但配置文件中无appkey和密钥
        // 或者在JVM启动时指定：-Dyop.sdk.config.file=file://home/aaa/...
        System.setProperty("yop.sdk.config.env", "qa_multi_default");

        YopClient yopClient = YopClientBuilder.builder().build();

        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

        YopRequestConfig requestConfig = request.getRequestConfig();
        requestConfig.setAppKey(appId);//多appId时必须指定
        requestConfig.setSecurityReq(securityReq);
        requestConfig.setReadTimeout(3000);
        requestConfig.setConnectTimeout(3000);

        YopResponse response = yopClient.request(request);
        System.out.println(response);
    }

    // 一般不存在该情况，这里只是为了方便测试，单app推荐用requestWithDefaultSingleApp()方式
    @Test
    public void requestWithSingleAppConfig() {
        System.setProperty("yop.sdk.config.env", "qa_single_app");
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_" + appId + ".json");

        YopClient yopClient = YopClientBuilder.builder().build();

        // 请求级配置
        YopRequestConfig requestConfig = new YopRequestConfig();
        requestConfig.setSecurityReq(securityReq).setAppKey(appId);
        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST", requestConfig);

        YopResponse response = yopClient.request(request);
        Assert.assertNotNull(response);
    }

    @Test
    public void requestWithMultiAppConfig() {
        System.setProperty("yop.sdk.config.env", "qa_multi_app");

        // Client 级配置
        YopClient yopClient = YopClientBuilder.builder().build();

        // 请求级配置
        YopRequestConfig requestConfig = new YopRequestConfig();
        requestConfig.setAppKey(appId);// 多appId时必须指定
        requestConfig.setSecurityReq(securityReq);
        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST", requestConfig);

        YopResponse response = yopClient.request(request);
        System.out.println(response);
    }

    @Test(expected = YopServiceException.class)
    public void requestWithSingleAppNotExists() {
        System.setProperty("yop.sdk.config.env", "qa");
        // 使用该配置文件初始化SDK，且配置文件中有appkey和密钥
        // 或者在JVM启动时指定：-Dyop.sdk.config.file=file://home/aaa/...
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_app_10085525305.json");

        YopClient yopClient = YopClientBuilder.builder().build();
        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

        YopRequestConfig requestConfig = request.getRequestConfig();
        requestConfig.setAppKey("app_10085525305");

        YopResponse response = yopClient.request(request);
        Assert.assertNotNull(response);
    }

    @Test
    public void requestWithCodeConfig() {
        System.setProperty("yop.sdk.config.env", "qa");
        YopClient yopClient = YopClientBuilder.builder().build();

        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

        // 编码指定appkey和密钥
        YopRequestConfig requestConfig = request.getRequestConfig();
        PKICredentialsItem pkiCredentialsItem = new PKICredentialsItem(CredentialsRepository.getPrivateKey(appId), CredentialsRepository.getSupportCertType(appId));
        YopPKICredentials yopPKICredentials = new YopPKICredentials(appId, pkiCredentialsItem);
        requestConfig.setSecurityReq(securityReq);
        requestConfig.setCredentials(yopPKICredentials);

        YopResponse response = yopClient.request(request);
        System.out.println(response);
    }

    @Test
    public void requestWithCustomFixedCredentialsProvider() {
        System.setProperty("yop.sdk.config.env", "qa");
        final YopCredentialsProvider oldProvider = YopCredentialsProviderRegistry.getProvider();
        try {
            CustomFixedCredentialsProvider credentialsProvider = new CustomFixedCredentialsProvider();
            YopCredentialsProviderRegistry.registerProvider(credentialsProvider);

            YopClient yopClient = YopClientBuilder.builder().build();

            YopRequestConfig requestConfig = new YopRequestConfig();
            requestConfig.setAppKey(appId);
            requestConfig.setSecurityReq(securityReq);
            YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST", requestConfig);

            YopResponse response = yopClient.request(request);
            Assert.assertNotNull(response);
        } finally {
            YopCredentialsProviderRegistry.registerProvider(oldProvider);
        }

    }

    @Test
    public void requestWithCustomCachedCredentialsProvider() {
        System.setProperty("yop.sdk.config.env", "qa");
        final YopCredentialsProvider oldProvider = YopCredentialsProviderRegistry.getProvider();
        try {
            CustomCachedCredentialsProvider credentialsProvider = new CustomCachedCredentialsProvider();
            YopCredentialsProviderRegistry.registerProvider(credentialsProvider);

            YopClient yopClient = YopClientBuilder.builder().build();

            // 编码指定appkey
            YopRequestConfig requestConfig = new YopRequestConfig();
            requestConfig.setAppKey(appId);
            requestConfig.setSecurityReq(securityReq);
            YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST", requestConfig);

            YopResponse response = yopClient.request(request);
            Assert.assertNotNull(response);
        } finally {
            YopCredentialsProviderRegistry.registerProvider(oldProvider);
        }
    }

}