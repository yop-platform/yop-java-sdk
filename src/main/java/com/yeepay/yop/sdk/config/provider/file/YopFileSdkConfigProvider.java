package com.yeepay.yop.sdk.config.provider.file;

import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.YopFixedSdkConfigProvider;
import com.yeepay.yop.sdk.exception.YopClientException;
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
import java.util.HashMap;
import java.util.Map;

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

    public static final String SDK_CONFIG_ENV_PROPERTY_KEY = "yop.sdk.config.env";
    public static final String SDK_CONFIG_DIR_PROPERTY_KEY = "yop.sdk.config.dir";
    public static final String SDK_CONFIG_FILE_PROPERTY_KEY = "yop.sdk.config.file";

    private static final String SDK_CONFIG_DIR = "config";

    private static final String DEFAULT_SDK_CONFIG = "default";

    private Map<String, YopFileSdkConfig> sdkConfigs = new HashMap<>();

    @Override
    protected YopSdkConfig loadSdkConfig() {
        return convertYopSdkConfig(loadSdkConfig("default"));
    }

    public YopFileSdkConfig loadSdkConfig(String appKey) {
        appKey = StringUtils.defaultIfBlank(appKey, "default");
        if (!sdkConfigs.containsKey(appKey)) {
            sdkConfigs.computeIfAbsent(appKey, k -> doLoadYopFileSdkConfig(k));
        }
        return sdkConfigs.get(appKey);
    }

    private YopFileSdkConfig doLoadYopFileSdkConfig(String appKey) {
        // 读取目录
        String configDir = System.getProperty(SDK_CONFIG_DIR_PROPERTY_KEY);
        if (StringUtils.isNotEmpty(configDir)) {
            logger.info("指定了-Dyop.sdk.config.dir，值为：{}", configDir);
        } else {
            configDir = SDK_CONFIG_DIR;
        }

        // 读取环境，文件名：config/qa/yop_sdk_config_{appKey}.json
        String env = System.getProperty(SDK_CONFIG_ENV_PROPERTY_KEY);
        if (StringUtils.isNotEmpty(env)) {
            logger.info("指定了-Dyop.sdk.config.env，值为：{}", env);
            configDir += "/" + env;
        }

        // 读取文件
        String configFile = "";
        String file = System.getProperty(SDK_CONFIG_FILE_PROPERTY_KEY);
        if (StringUtils.isNotEmpty(file)) {
            logger.info("指定了-Dyop.sdk.config.file，值为：{}", file);
            configFile = configDir + "/" + file;
        } else {
            configFile = configDir + "/yop_sdk_config_" + appKey + ".json";
        }

        logger.info("加载默认配置文件{}", configFile);
        YopFileSdkConfig sdkConfig = loadSdkConfigFile(configFile);
        String defaultFileConfig = SDK_CONFIG_DIR + "/yop_sdk_config_" + DEFAULT_SDK_CONFIG + ".json";
        if (!StringUtils.equals(defaultFileConfig, configFile)) {
            YopFileSdkConfig customSdkConfig = loadSdkConfigFile(defaultFileConfig);
            sdkConfig = BeanUtils.merge(customSdkConfig, sdkConfig);
        }

        if (null == sdkConfig) {
            throw new YopClientException("Can't load config, file:" + configFile);
        }

        return sdkConfig;
    }

    private YopFileSdkConfig loadSdkConfigFile(String configFile) {
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
        return yopSdkConfig;
    }

    @Override
    public void removeConfig(String key) {
        sdkConfigs.remove(key);
    }

}
