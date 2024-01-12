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
     * 获取指定服务方、环境的服务端配置
     *
     * @param provider 服务方
     * @param env      环境
     * @return 服务端配置
     */
    default YopSdkConfig getConfig(String provider, String env) {
        return getConfig();
    }

    /**
     * 移除SDK配置
     *
     * @param key key
     */
    void removeConfig(String key);

}
