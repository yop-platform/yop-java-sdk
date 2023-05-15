package com.yeepay.yop.sdk.config.provider.support;


import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfig;
import com.yeepay.g3.core.yop.sdk.sample.config.SDKConfig;

import java.util.concurrent.Callable;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/5/17 17:21
 */
public class AppSdkConfigInitTask implements Callable<AppSdkConfig> {
    private final SDKConfig sdkConfig;

    public AppSdkConfigInitTask(final SDKConfig sdkConfig) {
        this.sdkConfig = sdkConfig;
    }

    @Override
    public AppSdkConfig call() throws Exception {
        return new AppSdkConfig.Builder().withSDKConfig(sdkConfig).build();
    }
}