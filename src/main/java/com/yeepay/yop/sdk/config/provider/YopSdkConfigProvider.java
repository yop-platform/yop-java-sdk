package com.yeepay.yop.sdk.config.provider;

import com.yeepay.yop.sdk.config.YopSdkConfig;

/**
 * title: sdk配置提供方接口<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 20/11/23 11:25
 */
public interface YopSdkConfigProvider {

    /**
     * 获取 sdk配置
     *
     * @return sdk配置
     */
    YopSdkConfig getConfig();

    /**
     * 移除SDK配置
     *
     * @param key key
     */
    void removeConfig(String key);

}
