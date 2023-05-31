package com.yeepay.g3.sdk.yop.config;

import com.yeepay.g3.sdk.yop.config.provider.DefaultFileAppSdkConfigProvider;
import com.yeepay.g3.sdk.yop.config.support.SDKConfigUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/6/12 15:57
 */
public class SDKConfigReadTest {

    @Test
    public void testDir() {
        System.setProperty("yop.sdk.config.dir", "multiconfig");
        DefaultFileAppSdkConfigProvider provider = new DefaultFileAppSdkConfigProvider();
        Assert.assertNotNull(provider.getConfig("app_oEdCdceLQF63hvYz"));
    }

    @Test
    public void testRelativePath2() {
        System.setProperty("yop.sdk.config.file", "yop_sdk_config_dev.json");
        DefaultFileAppSdkConfigProvider provider = new DefaultFileAppSdkConfigProvider();
        Assert.assertNotNull(provider.getConfig("yop-boss"));
    }

    @Test
    public void testRelativePath3() {
        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_dev.json");
        DefaultFileAppSdkConfigProvider provider = new DefaultFileAppSdkConfigProvider();
        Assert.assertNotNull(provider.getConfig("yop-boss"));
    }

    @Test
    public void testAbsolutePath() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("config/yop_sdk_config_dev.json");
        System.setProperty("yop.sdk.config.file", "file://" + url.getFile());
        DefaultFileAppSdkConfigProvider provider = new DefaultFileAppSdkConfigProvider();
        Assert.assertNotNull(provider.getConfig("yop-boss"));
    }

    @Test
    public void testBackupConfig() {
        final SDKConfig sdkConfig = SDKConfigUtils.loadConfig("config/yop_sdk_config_default.json");
        Assert.assertNotNull(sdkConfig);
    }
}