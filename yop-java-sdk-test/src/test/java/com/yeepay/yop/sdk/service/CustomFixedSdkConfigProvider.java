/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.base.config.provider.YopFixedSdkConfigProvider;
import com.yeepay.yop.sdk.config.YopSdkConfig;

/**
 * title: 自定义SDK配置<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/5/10
 */
public class CustomFixedSdkConfigProvider extends YopFixedSdkConfigProvider {

    @Override
    protected YopSdkConfig loadSdkConfig() {
        YopSdkConfig yopSdkConfig = new YopSdkConfig();
        // 可以根据当前环境(测试/灰度/生产等等)，自定义其他请求根路径
        yopSdkConfig.setServerRoot(YopConstants.DEFAULT_SERVER_ROOT);
        yopSdkConfig.setYosServerRoot(YopConstants.DEFAULT_YOS_SERVER_ROOT);
        yopSdkConfig.setSandboxServerRoot(YopConstants.DEFAULT_SANDBOX_SERVER_ROOT);
        // 连接超时时间、读取超时时间等其他配置，可根据需要setXXX即可
        return yopSdkConfig;
    }

    @Override
    public void removeConfig(String key) {
        // 可以不实现
    }
}
