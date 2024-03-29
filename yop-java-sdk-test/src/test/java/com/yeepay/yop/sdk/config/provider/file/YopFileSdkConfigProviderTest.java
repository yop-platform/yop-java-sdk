package com.yeepay.yop.sdk.config.provider.file;

import com.yeepay.yop.sdk.BaseTest;
import com.yeepay.yop.sdk.base.config.provider.file.YopFileSdkConfigProvider;
import org.junit.Assert;
import org.junit.Test;

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
public class YopFileSdkConfigProviderTest extends BaseTest {

    private YopFileSdkConfigProvider yopFileSdkConfigProvider = new YopFileSdkConfigProvider();

    @Test
    public void loadSdkConfigDir() {
        System.setProperty(YopFileSdkConfigProvider.SDK_CONFIG_DIR_PROPERTY_KEY, "config1");
        YopFileSdkConfig yopSdkConfig = yopFileSdkConfigProvider.loadSdkConfig("app_10085525305");
        Assert.assertEquals("CN_N1", yopSdkConfig.getRegion());
    }

    @Test
    public void loadSdkConfigEnv() {
        System.setProperty(YopFileSdkConfigProvider.SDK_CONFIG_ENV_PROPERTY_KEY, "qa");
        YopFileSdkConfig yopSdkConfig = yopFileSdkConfigProvider.loadSdkConfig("app_10085525305");
        Assert.assertEquals("qa", yopSdkConfig.getRegion());
    }

    @Test
    public void loadSdkConfigFile() {
        System.setProperty(YopFileSdkConfigProvider.SDK_CONFIG_FILE_PROPERTY_KEY, "yop_sdk_config_app_20085525305.json");
        YopFileSdkConfig yopSdkConfig = yopFileSdkConfigProvider.loadSdkConfig("app_10085525305");
        Assert.assertEquals("file", yopSdkConfig.getRegion());
    }

    @Test
    public void loadSdkConfigByAppKey() {
        YopFileSdkConfig yopSdkConfig = yopFileSdkConfigProvider.loadSdkConfig("app_20085525305");
        Assert.assertEquals("file", yopSdkConfig.getRegion());
    }

    @Test
    public void loadDefaultSdkConfig() {
        System.setProperty(YopFileSdkConfigProvider.SDK_CONFIG_ENV_PROPERTY_KEY, "qa");
        System.setProperty(YopFileSdkConfigProvider.SDK_CONFIG_FILE_PROPERTY_KEY, "yop_sdk_config_app_10085525305.json");
        YopFileSdkConfig yopSdkConfig = yopFileSdkConfigProvider.loadSdkConfig("");
        final String defaultAppKey = yopSdkConfig.getAppKey();
        Assert.assertEquals(defaultAppKey, "app_10085525305");
        Assert.assertEquals(yopSdkConfig.getHttpClient().getMaxConnTotal(), 200);
    }
}