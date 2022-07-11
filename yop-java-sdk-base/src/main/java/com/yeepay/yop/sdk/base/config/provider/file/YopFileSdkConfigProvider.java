package com.yeepay.yop.sdk.base.config.provider.file;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.base.config.provider.YopFixedSdkConfigProvider;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.utils.BeanUtils;
import com.yeepay.yop.sdk.utils.JsonUtils;
import com.yeepay.yop.sdk.utils.StreamUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.yeepay.yop.sdk.YopConstants.FILE_PROTOCOL_PREFIX;
import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_APPKEY;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(YopFileSdkConfigProvider.class);

    public static final String SDK_CONFIG_ENV_PROPERTY_KEY = "yop.sdk.config.env";
    public static final String SDK_CONFIG_DIR_PROPERTY_KEY = "yop.sdk.config.dir";
    public static final String SDK_CONFIG_FILE_PROPERTY_KEY = "yop.sdk.config.file";

    private static final String SDK_CONFIG_DIR = "config";
    private static final String DEFAULT_CONFIG_FILE = SDK_CONFIG_DIR + "/yop_sdk_config_default.json";

    private Map<String, YopFileSdkConfig> sdkConfigs = Maps.newHashMap();

    @Override
    protected YopSdkConfig loadSdkConfig() {
        return convertYopSdkConfig(loadSdkConfig(""));
    }

    public YopFileSdkConfig loadSdkConfig(String appKey) {
        appKey = StringUtils.defaultIfBlank(appKey, YOP_DEFAULT_APPKEY);
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
            if (!StringUtils.startsWithAny(file, FILE_PROTOCOL_PREFIX)) {
                configFile = configDir + "/" + file;
            } else {
                configFile = file;
            }
        } else {
            configFile = configDir + "/yop_sdk_config_" + appKey + ".json";
        }

        logger.info("加载默认配置文件{}", configFile);
        YopFileSdkConfig customSdkConfig = loadSdkConfigFile(configFile);
        if (!StringUtils.equals(DEFAULT_CONFIG_FILE, configFile)) {
            YopFileSdkConfig defaultConfig = loadSdkConfigFile(DEFAULT_CONFIG_FILE);
            customSdkConfig = fillNullConfig(defaultConfig, customSdkConfig);
        }

        if (null == customSdkConfig) {
            throw new YopClientException("Can't load config, file:" + configFile);
        }

        return customSdkConfig;
    }

    private YopFileSdkConfig fillNullConfig(YopFileSdkConfig sourceBean, YopFileSdkConfig targetBean) {
        if (null == sourceBean) {
            return targetBean;
        }
        if (null == targetBean) {
            return sourceBean;
        }

        Class sourceBeanClass = sourceBean.getClass();
        Class targetBeanClass = targetBean.getClass();

        Field[] sourceFields = sourceBeanClass.getDeclaredFields();
        Field[] targetFields = targetBeanClass.getDeclaredFields();
        for (int i = 0; i < sourceFields.length; i++) {
            Field sourceField = sourceFields[i];
            if (Modifier.isStatic(sourceField.getModifiers())) {
                continue;
            }
            Field targetField = targetFields[i];
            if (Modifier.isStatic(targetField.getModifiers())) {
                continue;
            }
            sourceField.setAccessible(true);
            targetField.setAccessible(true);
            try {
                if (!(sourceField.get(sourceBean) == null)
                        && !"serialVersionUID".equals(sourceField.getName()) && targetField.get(targetBean) == null) {
                    targetField.set(targetBean, sourceField.get(sourceBean));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.error("error when fillNullConfig, ex:", e);
            }
        }
        return targetBean;
    }

    private YopFileSdkConfig loadSdkConfigFile(String configFile) {
        YopFileSdkConfig sdkConfig = null;
        try {
            if (!StringUtils.startsWithAny(configFile, FILE_PROTOCOL_PREFIX)) {
                configFile = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + configFile;
            }
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(configFile);
            for (int i = resources.length - 1; i >= 0; i--) {
                Resource resource = resources[i];
                StringBuilder script = new StringBuilder();
                BufferedReader bufferReader = null;
                try {
                    bufferReader = new BufferedReader(
                            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
                    String tempString;
                    while ((tempString = bufferReader.readLine()) != null) {
                        script.append(tempString).append("\n");
                    }
                } catch (IOException e) {
                    LOGGER.error("error when loadSdkConfigFile, ex:", e);
                } finally {
                    StreamUtils.closeQuietly(bufferReader);
                }

                if (script.length() > 0) {
                    YopFileSdkConfig sdkConfig0 = JsonUtils.loadFrom(script.toString(), YopFileSdkConfig.class);
                    sdkConfig = BeanUtils.merge(sdkConfig0, sdkConfig);
                }
            }
        } catch (IOException e) {
            LOGGER.error("error when loadSdkConfigFile, ex:", e);
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
        yopSdkConfig.setYopCertStore(yopFileSdkConfig.getYopCertStore());
        return yopSdkConfig;
    }

    public Map<String, YopFileSdkConfig> getSdkConfigs() {
        return sdkConfigs;
    }

    @Override
    public void removeConfig(String key) {
        sdkConfigs.remove(key);
    }

}
