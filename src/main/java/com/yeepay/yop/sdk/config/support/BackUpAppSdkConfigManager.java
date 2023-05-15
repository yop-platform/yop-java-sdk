package com.yeepay.yop.sdk.config.support;

import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfig;
import com.yeepay.g3.core.yop.sdk.sample.config.SDKConfig;

/**
 * title: 后备配置管理器（用户没有配置时采用后备配置）<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/5/17 17:31
 */
public class BackUpAppSdkConfigManager {

    private static volatile AppSdkConfig backUpConfig;

    public static AppSdkConfig getBackUpConfig() {
        if (backUpConfig == null) {
            synchronized (BackUpAppSdkConfigManager.class) {
                if (backUpConfig == null) {
                    SDKConfig sdkConfig = SDKConfigUtils.loadConfig("config/yop_sdk_config_default.json");
                    backUpConfig = new AppSdkConfig.Builder().withSDKConfig(sdkConfig).build();
                }
            }
        }
        return backUpConfig;
    }
}
