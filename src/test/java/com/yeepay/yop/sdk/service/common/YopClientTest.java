package com.yeepay.yop.sdk.service.common;

import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import lombok.Builder;
import lombok.Singular;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * title: YopClientTest<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2020/11/18 上午10:56
 */
@DisplayName(value = "Tests Config")
@RunWith(value = Parameterized.class)
public class YopClientTest {

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

    @BeforeEach
    public void beforeEach() throws IOException {
        // do something
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource(value = "credentialProvider")
    public void requestWithDefaultSingleApp(TestConfig testConfig) {
        System.setProperty("yop.sdk.config.env", testConfig.env);
        if (StringUtils.isNotEmpty(testConfig.configFile)) {
            System.setProperty("yop.sdk.config.file", testConfig.configFile);
        }

        // Client 级配置，实际使用中请勿放在成员方法内部
        YopClient yopClient = YopClientBuilder.builder()
                // .withYopSdkConfigProvider(your DIY provider)
                // .withCredentialsProvider(your DIY provider)
                .build();

        YopRequest request = new YopRequest(testConfig.apiUri, testConfig.httpMethod);

        // Request 级配置
        if (ConfigLevel.REQUEST.equals(testConfig.configLevel)) {
            request.getRequestConfig()
                    .setAppKey(testConfig.appId) // 多appId时必须指定
                    .setSecurityReq(testConfig.securityReq);
        }

        try {
            YopResponse response = yopClient.request(request);
            System.out.println(response);

            assertTrue(testConfig.expectResult);
            // TODO 对 response 中的值做校验
        } catch (YopClientException e) {
            e.printStackTrace();
            assertFalse(testConfig.expectResult);
            // TODO 对错误码做校验
        } catch (Exception e) {
            // 不应该有未包装的异常
            e.printStackTrace();
            throw e;
        }
    }

    static Iterator<TestConfig> credentialProvider() {
        List<TestConfig> testConfigs = new LinkedList<>();
//        testConfigs.add(TestConfig.builder()
//                .title("requestWithDefaultSingleApp 不指定应用名")
//                .env("qa_single_default")
////                .appId("yop-boss") // 不指定应用名
//                .apiUri("/rest/v1.0/file/upload")
//                .httpMethod("POST")
//                .build());
//        testConfigs.add(TestConfig.builder()
//                .title("requestWithDefaultSingleApp 指定应用名")
//                .env("qa_single_default")
//                .configLevel(ConfigLevel.REQUEST)
//                .appId("app_100800095600032")
//                .securityReq("YOP-SM2-SM3")
//                .apiUri("/rest/v1.0/file/upload")
//                .httpMethod("POST")
//                .build());

//        testConfigs.add(TestConfig.builder()
//                .title("requestWithDefaultMultiApp 指定应用名")
//                .env("qa_multi_default")
//                .configLevel(ConfigLevel.REQUEST)
//                .appId("yop-boss")
//                .securityReq("YOP-RSA2048-SHA256")
//                .apiUri("/rest/v1.0/file/upload")
//                .httpMethod("POST")
//                .build());

        //    // 一般不存在该情况，这里只是为了方便测试，单app推荐用requestWithDefaultSingleApp()方式
//        YopCredentialsProviderRegistry.getProvider().removeConfig(null);
//        YopSdkConfigProviderRegistry.getProvider().removeConfig("default");
//        testConfigs.add(TestConfig.builder()
//                .title("requestWithSingleAppConfig 指定应用名")
//                .env("qa_single_app")
//                .configFile("yop_sdk_config_yop-boss.json")
//                .configLevel(ConfigLevel.REQUEST)
//                .appId("yop-boss")
//                .securityReq("YOP-RSA2048-SHA256")
//                .apiUri("/rest/v1.0/file/upload")
//                .httpMethod("POST")
//                .build());

//        testConfigs.add(TestConfig.builder()
//                .title("requestWithMultiAppConfig 指定应用名")
//                .env("qa_multi_app")
//                .configFile("yop_sdk_config_yop-boss.json")
//                .configLevel(ConfigLevel.REQUEST)
//                .appId("yop-boss")
//                .securityReq("YOP-RSA2048-SHA256")
//                .apiUri("/rest/v1.0/file/upload")
//                .httpMethod("POST")
//                .build());

        // ？？？？？
        testConfigs.add(TestConfig.builder()
                .title("requestWithSingleAppNotExists")
                .env("qa")
                .configFile("yop_sdk_config_app_10085525305.json")
                .appId("app_10085525305")
                .configLevel(ConfigLevel.REQUEST)
                .securityReq("YOP-SM2-SM3")
                .apiUri("/rest/v1.0/file/upload")
                .httpMethod("POST")
                .build());

        // ？？？？
//        String appId = "app_10085525305";
//        PKICredentialsItem pkiCredentialsItem = new PKICredentialsItem(CredentialsRepository.getPrivateKey(appId),
//                null,
//                CredentialsRepository.getSupportCertType(appId));
//        YopPKICredentials yopPKICredentials = new YopPKICredentials(appId, pkiCredentialsItem);
//        testConfigs.add(TestConfig.builder()
//                .title("requestWithCodeConfig")
//                .env("qa")
//                .configLevel(ConfigLevel.REQUEST)
//                .appId(appId)
//                .securityReq("YOP-SM2-SM3")
//                .yopCredentials(yopPKICredentials)
//                .apiUri("/rest/v1.0/file/upload")
//                .httpMethod("POST")
//                .build());

//        CustomFixedCredentialsProvider customFixedCredentialsProvider = new CustomFixedCredentialsProvider();
//        YopCredentialsProviderRegistry.registerProvider(customFixedCredentialsProvider);
//        testConfigs.add(TestConfig.builder()
//                .title("requestWithCustomFixedCredentialsProvider")
//                .env("qa")
//                .configLevel(ConfigLevel.REQUEST)
////                .appId()
//                .securityReq("YOP-SM2-SM3")
//                .credentialsProvider(customFixedCredentialsProvider)
//                .apiUri("/rest/v1.0/file/upload")
//                .httpMethod("POST")
//                .build());

//        CustomCachedCredentialsProvider customCachedCredentialsProvider = new CustomCachedCredentialsProvider();
//        YopCredentialsProviderRegistry.registerProvider(customCachedCredentialsProvider);
//        testConfigs.add(TestConfig.builder()
//                .title("requestWithCustomCachedCredentialsProvider")
//                .env("qa")
//                .configLevel(ConfigLevel.REQUEST)
////                .appId()
//                .securityReq("YOP-SM2-SM3")
//                .credentialsProvider(customFixedCredentialsProvider)
//                .apiUri("/rest/v1.0/file/upload")
//                .httpMethod("POST")
//                .build());

        return testConfigs.iterator();
    }

    @Builder
    static class TestConfig {

        /**
         * 测试场景描述
         */
        @Builder.Default
        private String title = "这个家伙很懒";

        @Builder.Default
        private String env = "prod";
        private String configFile;

        @Builder.Default
        private ConfigLevel configLevel = ConfigLevel.CLIENT;

        private String appId;

        @Builder.Default
        private String securityReq = "YOP-SM2-SM3";

        private YopCredentials yopCredentials;

        private YopCredentialsProvider credentialsProvider;

        /**
         * 商户私钥（ConfigLevel.REQUEST时指定）
         */
        private String priKey;

        /**
         * 平台公钥（ConfigLevel.REQUEST时指定）
         */
        private String pubKey;

        private String apiUri;

        @Builder.Default
        private String httpMethod = "POST";

        @Singular("param")
        private Map<String, Object> params;

        @Builder.Default
        private Boolean expectResult = true;
        private String expectCode;

    }

    static enum ConfigLevel {

        REQUEST, CLIENT;

    }

}