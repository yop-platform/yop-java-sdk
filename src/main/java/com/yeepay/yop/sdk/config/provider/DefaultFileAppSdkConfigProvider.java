package com.yeepay.yop.sdk.config.provider;

import com.yeepay.g3.core.yop.sdk.sample.config.SDKConfig;
import com.yeepay.g3.core.yop.sdk.sample.config.support.ConfigUtils;
import com.yeepay.g3.core.yop.sdk.sample.config.support.SDKConfigUtils;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopClientException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * title: 文件sdk配置provider<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/5/17 12:35
 */
public final class DefaultFileAppSdkConfigProvider extends BaseFixedAppSdkConfigProvider {

    private static final String SDK_CONFIG_FILE_PROPERTY_KEY = "yop.sdk.config.file";

    private static final String SDK_CONFIG_DIR_PROPERTY_KEY = "yop.sdk.config.dir";

    private static final String SDK_CONFIG_FILE_SEPARATOR = ",";

    private static final String SDK_CONFIG_DIR = "config";

    private static final Pattern SDK_CONFIG_FILE_NAME_PATTERN = Pattern.compile("^yop_sdk_config_(.+).json$");

    @Override
    protected List<SDKConfig> loadCustomSdkConfig() {
        List<String> files = loadSystemConfigSdkFiles();
        if (CollectionUtils.isEmpty(files)) {
            files = loadConfigFilesFromClassPath();
        }
        if (CollectionUtils.isEmpty(files)) {
            return Collections.emptyList();
        }
        List<SDKConfig> customSdkConfigs = new ArrayList<SDKConfig>(files.size());
        for (String filePath : files) {
            String fileName = StringUtils.substringAfterLast(filePath, File.separator);
            Matcher matcher = SDK_CONFIG_FILE_NAME_PATTERN.matcher(fileName);
            if (matcher.matches()) {
                customSdkConfigs.add(SDKConfigUtils.loadConfig(filePath));
            } else {
                logger.warn("Illegal SDkConfig File Name:" + fileName);
            }
        }
        return customSdkConfigs;
    }


    private List<String> loadSystemConfigSdkFiles() {
        String configDir = System.getProperty(SDK_CONFIG_DIR_PROPERTY_KEY);
        String configFile = System.getProperty(SDK_CONFIG_FILE_PROPERTY_KEY);
        if (StringUtils.isEmpty(configDir) && StringUtils.isEmpty(configFile)) {
            return Collections.emptyList();
        }
        if (StringUtils.isEmpty(configFile)) {
            try {
                return ConfigUtils.listFiles(configDir);
            } catch (Exception ex) {
                logger.error("Unexpected exception occurred when loaded configFiles from dir:" + configDir, ex);
                throw new YopClientException("unable to load configFiles from dir:" + configDir, ex);
            }
        }

        String[] subFiles = StringUtils.split(configFile, SDK_CONFIG_FILE_SEPARATOR);
        if (subFiles == null || subFiles.length == 0) {
            return Collections.emptyList();
        }
        if (StringUtils.isEmpty(configDir)) {
            return Arrays.asList(subFiles);
        }
        List<String> files = new ArrayList<String>(subFiles.length);
        for (String subFile : subFiles) {
            files.add(configDir + File.separator + subFile);
        }
        return files;
    }

    private List<String> loadConfigFilesFromClassPath() {
        try {
            return ConfigUtils.listFiles(SDK_CONFIG_DIR);
        } catch (Exception ex) {
            logger.warn("Unexpected exception occurred when loaded configFiles from dir:" + SDK_CONFIG_DIR, ex);
            return Collections.emptyList();
        }
    }

}
