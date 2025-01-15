/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.YopClientImpl;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: YOP SDK 工具类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/7/20
 */
public class YopSdkInitUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopSdkInitUtils.class);

    /**
     * 预加载的SPI列表
     */
    private static final String[] SPI_CLASSES = new String[]{
            "com.yeepay.yop.sdk.base.security.cert.X509CertSupportFactory",
            "com.yeepay.yop.sdk.base.auth.signer.YopSignerFactory",
            "com.yeepay.yop.sdk.base.auth.signer.process.YopSignProcessorFactory",
            "com.yeepay.yop.sdk.base.security.cert.parser.YopCertParserFactory",
            "com.yeepay.yop.sdk.base.security.digest.YopDigesterFactory",
            "com.yeepay.yop.sdk.base.security.encrypt.YopEncryptorFactory",
            "com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry",
            "com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry",
            "com.yeepay.yop.sdk.client.cmd.YopCmdExecutorRegistry",
            "com.yeepay.yop.sdk.http.YopHttpClientFactory"
    };

    /**
     * 启动时预加载SDK引用的SPI类
     *
     * @param args 自定义类列表(全路径)
     */
    public static void loadSpiClasses(String... args) {
        for (String spiClass : SPI_CLASSES) {
            doLoadSpiClass(spiClass);
        }
        if (null != args) {
            for (String arg : args) {
                doLoadSpiClass(arg);
            }
        }
    }

    private static void doLoadSpiClass(String spiClass) {
        try {
            Class<?> clazz = Class.forName(spiClass);
            LOGGER.info("spi class load success, name:{}, loader:{}", spiClass, clazz.getClassLoader());
        } catch (Throwable e) {
            LOGGER.error("spi class load fail, name:{}, loader:{}, ex:", spiClass,
                    Thread.currentThread().getContextClassLoader(), e);
        }
    }

    public static void main(String[] args) {
        YopClientImpl yopClient = YopClientBuilder.builder().build();
        final YopRequest yopRequest = new YopRequest("/rest/v2.0/yop/platform/certs", "GET");
        yopRequest.getRequestConfig().setAppKey("您的国密appKey");
        yopRequest.addParameter("certType", "SM2");
        final YopResponse response = yopClient.request(yopRequest);
        assert null != response;
    }

}
