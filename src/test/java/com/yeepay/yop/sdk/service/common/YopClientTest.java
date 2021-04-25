package com.yeepay.yop.sdk.service.common;

import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.model.RequestConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
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
        return Arrays.asList(new Object[][]{
                {"yop-boss", "YOP-RSA2048-SHA256"},
                {"app_100800095600036", "YOP-SM2-SM3"}
        });
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.setProperty("yop.sdk.config.env", "qa");
        System.setProperty("yop.sdk.http", "true");

        // 应用级配置
//        YopSdkConfigProviderRegistry
    }

    @Test
    public void requestWithDefaultConfigFileWithAppId() {
        // Client 级配置
        YopClient yopClient = YopClientBuilder.builder().build();

        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

        // 请求级配置
        RequestConfig requestConfig = new RequestConfig();
        requestConfig.setAppKey(appId);// 多appId时必须指定
        requestConfig.setSecurityReq(securityReq);
        request.withRequestConfig(requestConfig);

        YopResponse response = yopClient.request(request);
        System.out.println(response);
    }

    @Test
    public void requestWithDefaultConfigFileWithoutAppId() {
        // 使用该配置文件初始化SDK，但配置文件中无appkey和密钥
        // 或者在JVM启动时指定：-Dyop.sdk.config.file=file://home/aaa/...
        System.setProperty("yop.sdk.config.env", "qa_full");
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_" + appId + ".json");

        // 一般不存在该情况，这里只是为了方便测试
        YopCredentialsProviderRegistry.getProvider().removeConfig("default");
        YopSdkConfigProviderRegistry.getProvider().removeConfig("default");

        YopClient yopClient = YopClientBuilder.builder().build();

        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

        RequestConfig requestConfig = new RequestConfig();
        requestConfig.setSecurityReq(securityReq);
        request.withRequestConfig(requestConfig);

        YopResponse response = yopClient.request(request);
        System.out.println(response);
    }

    @Test
    public void requestWithCustomConfigFile2() {
        // 使用该配置文件初始化SDK，且配置文件中有appkey和密钥
        // 或者在JVM启动时指定：-Dyop.sdk.config.file=file://home/aaa/...
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_app_10085525305.json");

        YopClient yopClient = YopClientBuilder.builder()
                .build();
        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

        RequestConfig requestConfig = new RequestConfig();
        requestConfig.setSecurityReq(securityReq);
        request.withRequestConfig(requestConfig);

        YopResponse response = yopClient.request(request);
        System.out.println(response);
    }

    private final String appKey = "app_30085525305";
    private final String priKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDFtjrUYU2t+WWOOm6CK3L6nikNKY1gty2x2cmIaAF0IWlBQ33ljJubzNODWGs4kL2nqfQnmMqetLatlMdgvquuftNL3kpZvOX8E746REHPH+MP+dyalvbitwP6vUbFbiKiO555hM1AhtdY17TVZthNtklqDmkXZ7U9Lb5+OIQrxK8uvy+0hupZehpHs0lXBx8Hh2kXFO6BomUWAuc/GVWSBhL1ziq9zx+M37LP4yYEogsqZNq70vi77IaD06r0MgYJ1foyyHz1b5J5joYrkPb9+w8hoU5iK62aitX8bsfRbGh93BbrYBGVwpZGmo8xmxmnEqDyU0HCj50P0GbJ77EtAgMBAAECggEADR318TkzaiNttW3y/vfa/P2ZQ6JKGuyMP4xvbnlX/1hoH0hXBe+6My/3qHMpSetwabtA04+zgawDoqiIQcbkpQMNCa+Jx0JdD8hPipYUt6Up71loZWk2n/v8a5o7I8YWziSecvl4lJtdlitZd/8GwsEhRcQG/OKIh7KNPNqLCkw4TzJkYsL02QogF7fL/NVCw/AakIX1MjDFXCm+e7ifylztRNdm2oUuyw66C+nkORVL3GcabHOixsxUsqhMGBcpIJ4Y+Naypb79nRH255LsexE2N2y39pi9niZJNmfprUJMGhvA1NFcaN363IADfkcoYF0InQA7mMtzszPSWbTTgQKBgQD0PEmzxJxUhpXOKJVcZJhn+1wSyPqclHDRC+uYhyJhut/lSp7gIhoI2+wvIok7bYH3zrbCBregY4hlLiQjTH5zRoF9KvWf6O6OhtZT9RTVFQ+i9yjn/+7t4alWkYaA+oaNG/YXxKLXHOG8KLH7OdBjQpqVYmPyCoqJYNrjQS9axQKBgQDPPEAMPLtFR4esztB1vdcR4lCizyHBUHab/eWxyER22AyHh5VJFP5BTfKB34TFcKXKP/Nn3X5OEzQdoEivbvy+0QkIC2AzODZlcifUY5hj+JljpmldAO4TZM8qW4hrRkvAvu8CLekyVWk6h7gQLr7H2TL7qcqJW8F0ywNRR8yDSQKBgAs3oaLyCsQPEl5PmtyNejp8XvQ16ty6LJxNUdrFihy2+oWLcdSVfGCfyS85BNiH5Qo+okIzEMf7Ck6rWdmNL9mXiWb4TCO2DQ7avolInlZTC07Oz0Aojw656I8jS+wslXVxrVHWJCyBFRURQWtqclm8u2DVDgYV2dsJacQ6QDSJAoGAIKE8+HBLkFH98+CwhAl7Jq66wZfZmcWgl1k35HFDDm9gMarQf6xViFTMnVRjZG8jO6AsJCuE6qgtaYjGSRExrJ3fTSv1Xrs4HWsHCHMSGJOZG06lgmZWFimmUOYOqc1suhGWMoKmGC3IntWlzq59jZwOYf/PCyeuY0Rf5llmwzkCgYEA7NViHZgcukk1iwy4QAu4q4IyzckKIT2pTiKFlVhb4cO48w99HSvfmqJSPUZVN3I15UwVs0lqnzLanU/+aSTYJDArpgEhEwloDAkhfy8NQzM9Ji+XXA3itDdR7A7+Wy5XULqK0FVjXMXNK3sV/O0qGWey7aOiH1QUwMyhT4qlenc=";

    @Test
    public void requestWithCodeConfig() {
        YopClient yopClient = YopClientBuilder.builder()
                .build();

        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

        // 编码指定appkey和密钥
        //YopRSACredentials yopRSACredentials = new YopRSACredentials(appKey, priKey);
        PKICredentialsItem pkiCredentialsItem = new PKICredentialsItem(RSAKeyUtils.string2PrivateKey(priKey), null, CertTypeEnum.RSA2048);
        YopPKICredentials yopPKICredentials = new YopPKICredentials(appKey, pkiCredentialsItem);
        request.getRequestConfig().setCredentials(yopPKICredentials);

        YopResponse response = yopClient.request(request);
        System.out.println(response);
    }

    @Test
    public void requestWithCustomFixedCredentialsProvider() {
        CustomFixedCredentialsProvider credentialsProvider = new CustomFixedCredentialsProvider();

        YopClient yopClient = YopClientBuilder.builder()
                .withCredentialsProvider(credentialsProvider)
                .build();

        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

        RequestConfig requestConfig = new RequestConfig();
        requestConfig.setSecurityReq(securityReq);
        request.withRequestConfig(requestConfig);

        YopResponse response = yopClient.request(request);
        System.out.println(response);
    }

    @Test
    public void requestWithCustomCachedCredentialsProvider() {
        CustomCachedCredentialsProvider credentialsProvider = new CustomCachedCredentialsProvider();

        YopClient yopClient = YopClientBuilder.builder()
                .withCredentialsProvider(credentialsProvider)
                .build();

        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

        // 编码指定appkey
        request.getRequestConfig().setAppKey(appKey);

        RequestConfig requestConfig = new RequestConfig();
        requestConfig.setSecurityReq(securityReq);
        request.withRequestConfig(requestConfig);

        YopResponse response = yopClient.request(request);
        System.out.println(response);
    }

}