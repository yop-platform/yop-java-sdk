package com.yeepay.yop.sdk.config.provider.file;

import com.yeepay.yop.sdk.exception.YopClientException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2021/3/24 14:39
 */
public class YopFileSdkConfigProviderTest {

    private YopFileSdkConfigProvider yopFileSdkConfigProvider = new YopFileSdkConfigProvider();

    @Before
    public void setUp() throws Exception {
        System.clearProperty(YopFileSdkConfigProvider.SDK_CONFIG_DIR_PROPERTY_KEY);
        System.clearProperty(YopFileSdkConfigProvider.SDK_CONFIG_ENV_PROPERTY_KEY);
        System.clearProperty(YopFileSdkConfigProvider.SDK_CONFIG_FILE_PROPERTY_KEY);
    }

    @Test
    public void loadSdkConfigDir() {
        System.setProperty(YopFileSdkConfigProvider.SDK_CONFIG_DIR_PROPERTY_KEY, "config1");
        YopFileSdkConfig yopSdkConfig = yopFileSdkConfigProvider.loadSdkConfig("app_10085525305");
        assertEquals("dir", yopSdkConfig.getRegion());
    }

    @Test
    public void loadSdkConfigEnv() {
        System.setProperty(YopFileSdkConfigProvider.SDK_CONFIG_ENV_PROPERTY_KEY, "qa");
        YopFileSdkConfig yopSdkConfig = yopFileSdkConfigProvider.loadSdkConfig("app_10085525305");
        assertEquals("qa", yopSdkConfig.getRegion());
    }

    @Test
    public void loadSdkConfigFile() {
        System.setProperty(YopFileSdkConfigProvider.SDK_CONFIG_FILE_PROPERTY_KEY, "yop_sdk_config_app_20085525305.json");
        YopFileSdkConfig yopSdkConfig = yopFileSdkConfigProvider.loadSdkConfig("app_10085525305");
        assertEquals("file", yopSdkConfig.getRegion());
    }

    @Test
    public void loadSdkConfigByAppKey() {
        YopFileSdkConfig yopSdkConfig = yopFileSdkConfigProvider.loadSdkConfig("app_20085525305");
        assertEquals("file", yopSdkConfig.getRegion());
    }

}