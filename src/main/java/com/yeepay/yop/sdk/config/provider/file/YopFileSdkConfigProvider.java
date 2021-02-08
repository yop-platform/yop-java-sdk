package com.yeepay.yop.sdk.config.provider.file;

import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.YopFixedSdkConfigProvider;
import com.yeepay.yop.sdk.utils.BeanUtils;
import com.yeepay.yop.sdk.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * title: 文件sdk配置provider<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 20/11/23 12:35
 */
public final class YopFileSdkConfigProvider extends YopFixedSdkConfigProvider {

    private static final String SDK_CONFIG_FILE_PROPERTY_KEY = "yop.sdk.config.file";

    private static final String SDK_CONFIG_DIR = "config";

    private static final String DEFAULT_SDK_CONFIG_FILE_NAME = "yop_sdk_config_default.json";

    private YopFileSdkConfig loadedSdkConfig;

    @Override
    protected YopSdkConfig loadSdkConfig() {
        return convertYopSdkConfig(loadYopFileSdkConfig());
    }

    public YopFileSdkConfig loadYopFileSdkConfig() {
        if (null == loadedSdkConfig) {
            synchronized (YopFileSdkConfigProvider.class) {
                if (null == loadedSdkConfig) {
                    String configFile = SDK_CONFIG_DIR + "/" + DEFAULT_SDK_CONFIG_FILE_NAME;
                    logger.info("加载默认配置文件{}", configFile);
                    loadedSdkConfig = loadSdkConfig(configFile);

                    // 优先按照用户指定的文件参数尝试获取配置文件
                    configFile = System.getProperty(SDK_CONFIG_FILE_PROPERTY_KEY);
                    if (StringUtils.isNotEmpty(configFile)) {
                        logger.info("指定了-Dyop.sdk.config.file，尝试从{}加载配置文件", configFile);
                        YopFileSdkConfig customSdkConfig = loadSdkConfig(configFile);
                        loadedSdkConfig = BeanUtils.merge(customSdkConfig, loadedSdkConfig);
                    }
                }
            }
        }
        return loadedSdkConfig;
    }

    public static YopFileSdkConfig loadSdkConfig(String configFile) {
        YopFileSdkConfig sdkConfig = null;
        try {
            if (!StringUtils.startsWithAny(configFile, "file://")) {
                configFile = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + configFile;
            }
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(configFile);
            for (int i = resources.length - 1; i >= 0; i--) {
                Resource resource = resources[i];
                StringBuilder script = new StringBuilder();
                try (InputStreamReader isr = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                     BufferedReader bufferReader = new BufferedReader(isr)) {
                    String tempString;
                    while ((tempString = bufferReader.readLine()) != null) {
                        script.append(tempString).append("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (script.length() > 0) {
                    YopFileSdkConfig sdkConfig0 = JsonUtils.loadFrom(script.toString(), YopFileSdkConfig.class);
                    sdkConfig = BeanUtils.merge(sdkConfig0, sdkConfig);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sdkConfig;
    }

    private YopSdkConfig convertYopSdkConfig(YopFileSdkConfig yopFileSdkConfig) {
        YopSdkConfig yopSdkConfig = new YopSdkConfig();
        yopSdkConfig.setServerRoot(yopFileSdkConfig.getServerRoot());
        yopSdkConfig.setYosServerRoot(yopFileSdkConfig.getYosServerRoot());
        yopSdkConfig.setSandboxServerRoot(yopFileSdkConfig.getSandboxServerRoot());
        yopSdkConfig.setTrustAllCerts(yopFileSdkConfig.getTrustAllCerts());
        yopSdkConfig.setProxy(yopFileSdkConfig.getProxy());
        yopSdkConfig.setRegion(yopFileSdkConfig.getRegion());
        yopSdkConfig.setYopHttpClientConfig(yopFileSdkConfig.getHttpClient());
        yopSdkConfig.storeYopPublicKey(yopFileSdkConfig.getYopPublicKey());
        yopSdkConfig.setYopCertStore(yopFileSdkConfig.getYopCertStore());
        yopSdkConfig.setYopEncryptKey(yopFileSdkConfig.getYopEncryptKey());
        return yopSdkConfig;
    }

}
