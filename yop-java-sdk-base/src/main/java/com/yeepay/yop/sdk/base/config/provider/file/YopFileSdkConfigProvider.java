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

import static com.yeepay.yop.sdk.YopConstants.*;
import static com.yeepay.yop.sdk.constants.CharacterConstants.HASH;

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

    private static final YopFileSdkConfigProvider INSTANCE = new YopFileSdkConfigProvider();

    private static final Logger LOGGER = LoggerFactory.getLogger(YopFileSdkConfigProvider.class);

    public static final String SDK_CONFIG_ENV_PROPERTY_KEY = "yop.sdk.config.env";
    public static final String SDK_CONFIG_DIR_PROPERTY_KEY = "yop.sdk.config.dir";
    public static final String SDK_CONFIG_FILE_PROPERTY_KEY = "yop.sdk.config.file";

    private static final String SDK_CONFIG_DIR = "config";
    private static final String DEFAULT_CONFIG_FILE_NAME = "yop_sdk_config_default.json";
    private static final String DEFAULT_CONFIG_FILE = SDK_CONFIG_DIR + "/" + DEFAULT_CONFIG_FILE_NAME;

    private Map<String, YopFileSdkConfig> sdkConfigs = Maps.newHashMap();

    @Override
    protected YopSdkConfig loadSdkConfig() {
        return convertYopSdkConfig(loadSdkConfig(""));
    }

    @Override
    protected YopSdkConfig loadSdkConfig(String provider, String env) {
        return super.loadSdkConfig(provider, env);
    }

    public YopFileSdkConfig loadSdkConfig(String appKey) {
        return loadSdkConfig(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey);
    }

    public YopFileSdkConfig loadSdkConfig(String provider, String env, String appKey) {
        final String theProvider = StringUtils.defaultString(provider, YOP_DEFAULT_PROVIDER);
        final String theEnv = StringUtils.defaultString(env, YOP_DEFAULT_ENV);
        final String theAppKey = StringUtils.defaultIfBlank(appKey, YOP_DEFAULT_APPKEY);
        final String configKey = theProvider + HASH + theEnv + HASH + theAppKey;

        return sdkConfigs.computeIfAbsent(configKey,
                k -> doLoadYopFileSdkConfig(theProvider, theEnv, theAppKey));
    }

    private YopFileSdkConfig doLoadYopFileSdkConfig(String provider, String currentEnv, String appKey) {
        // 读取目录
        String configDir = System.getProperty(SDK_CONFIG_DIR_PROPERTY_KEY);
        if (StringUtils.isNotEmpty(configDir)) {
            logger.info("指定了-Dyop.sdk.config.dir，值为：{}", configDir);
        } else {
            configDir = SDK_CONFIG_DIR;
        }

        // 指定provider
        if (StringUtils.isNotBlank(provider)) {
            configDir += "/" + provider;
            logger.info("指定了provider，值为：{}", provider);
        }

        // 读取环境，文件名：config/{provider}/{env}/yop_sdk_config_{appKey}.json
        String env = System.getProperty(SDK_CONFIG_ENV_PROPERTY_KEY, YOP_DEFAULT_ENV);
        if (StringUtils.isNotBlank(currentEnv)) {
            logger.info("指定了env，值为：{}, 系统env：{}", currentEnv, env);
            env = currentEnv;
            configDir += "/" + currentEnv;
        } else if (StringUtils.isNotBlank(env)) {
            // 兼容旧版env指定方式
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

        logger.info("加载配置文件{}", configFile);
        YopFileSdkConfig customSdkConfig = loadSdkConfigFile(configFile);

        // 环境相关default配置
        String envConfigDefaultFile = getEnvConfigDefaultFile(provider, env);
        if (!StringUtils.equals(envConfigDefaultFile, configFile)) {
            YopFileSdkConfig envDefaultConfig = loadSdkConfigFile(envConfigDefaultFile);
            customSdkConfig = fillNullConfig(envDefaultConfig, customSdkConfig);
        }

        // 全局default配置
        if (!StringUtils.equals(DEFAULT_CONFIG_FILE, configFile)
                && !StringUtils.equals(DEFAULT_CONFIG_FILE, envConfigDefaultFile)) {
            YopFileSdkConfig globalDefaultConfig = loadSdkConfigFile(DEFAULT_CONFIG_FILE);
            customSdkConfig = fillNullConfig(globalDefaultConfig, customSdkConfig);
        }

        if (null == customSdkConfig) {
            throw new YopClientException("ConfigProblem, Can't load config, file:" + configFile);
        }

        return customSdkConfig;
    }

    private String getEnvConfigDefaultFile(String provider, String env) {
        return SDK_CONFIG_DIR + "/"
                + (StringUtils.isNotBlank(provider) ? (provider + "/") : "")
                + (StringUtils.isNotBlank(env) ? (env + "/") : "")
                + DEFAULT_CONFIG_FILE_NAME;
    }

    private YopFileSdkConfig fillNullConfig(YopFileSdkConfig sourceBean, YopFileSdkConfig targetBean) {
        // TODO 是否需要实现更细粒度的覆盖
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
        yopSdkConfig.setPreferredServerRoots(yopFileSdkConfig.getPreferredServerRoots());
        yopSdkConfig.setPreferredYosServerRoots(yopFileSdkConfig.getPreferredYosServerRoots());
        yopSdkConfig.setSandboxServerRoot(yopFileSdkConfig.getSandboxServerRoot());
        yopSdkConfig.setTrustAllCerts(yopFileSdkConfig.getTrustAllCerts());
        yopSdkConfig.setProxy(yopFileSdkConfig.getProxy());
        yopSdkConfig.setRegion(yopFileSdkConfig.getRegion());
        yopSdkConfig.setYopHttpClientConfig(yopFileSdkConfig.getHttpClient());
        yopSdkConfig.setYopCertStore(yopFileSdkConfig.getYopCertStore());
        yopSdkConfig.setYopReportConfig(yopFileSdkConfig.getYopReportConfig());

        return yopSdkConfig;
    }

    public Map<String, YopFileSdkConfig> getSdkConfigs() {
        return sdkConfigs;
    }

    @Override
    public void removeConfig(String key) {
        sdkConfigs.remove(key);
    }

    public static YopFileSdkConfigProvider instance() {
        return INSTANCE;
    }
}
