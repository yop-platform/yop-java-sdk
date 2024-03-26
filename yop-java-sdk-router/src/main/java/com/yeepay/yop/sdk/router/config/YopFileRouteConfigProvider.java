/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.config;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * title: 基于配置文件的路由配置<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/2/2
 */
public class YopFileRouteConfigProvider implements YopRouteConfigProvider {

    public static YopRouteConfigProvider INSTANCE = new YopFileRouteConfigProvider();

    private static final Logger LOGGER = LoggerFactory.getLogger(YopFileRouteConfigProvider.class);
    private static final String SDK_CONFIG_DIR = "config";
    private static final String DEFAULT_CONFIG_FILE_NAME = "yop_route_config_default.json";
    private static final String DEFAULT_CONFIG_FILE = SDK_CONFIG_DIR + "/" + DEFAULT_CONFIG_FILE_NAME;
    private static final String FILE_PROTOCOL_PREFIX = "file://";

    private final Map<String, YopRouteConfig> ROUTE_CONFIG_MAP = Maps.newHashMap();

    private final String configFile;

    public YopFileRouteConfigProvider() {
        this.configFile = DEFAULT_CONFIG_FILE;
    }

    public YopFileRouteConfigProvider(String configFile) {
        this.configFile = configFile;
    }

    @Override
    public YopRouteConfig getRouteConfig() {
        return ROUTE_CONFIG_MAP.computeIfAbsent(this.configFile, this::loadRouteConfigFile);
    }

    @Override
    public YopRouteConfig getRouteConfig(String configKey) {
        return ROUTE_CONFIG_MAP.computeIfAbsent(configKey, this::loadRouteConfigFile);
    }

    private YopRouteConfig loadRouteConfigFile(String configFile) {
        try (InputStream inputStream = StringUtils.startsWith(configFile, FILE_PROTOCOL_PREFIX) ?
                Files.newInputStream(Paths.get(StringUtils.substringAfter(configFile, FILE_PROTOCOL_PREFIX))) :
                Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile)){
            if (null != inputStream) {
                return JsonUtils.loadFrom(inputStream, YopRouteConfig.class);
            }
            LOGGER.warn("yop route config not found, file:{}", configFile);
        } catch (Exception e) {
            LOGGER.error("error when load route config file, ex:", e);
        }
        return new YopRouteConfig();
    }
}
