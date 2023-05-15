package com.yeepay.yop.sdk.config;

/**
 * title: 应用sdk配置提供方接口<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/5/17 11:25
 */
public interface AppSdkConfigProvider {

    /**
     * 获取指定appKey的sdk配置
     *
     * @param appKey appKey
     * @return app sdk配置
     */
    AppSdkConfig getConfig(String appKey);

    /**
     * 获取默认的应用sdk配置
     *
     * @return app sdk配置
     */
    AppSdkConfig getDefaultConfig();

    /**
     * 获取指定appKey的sdk配置，如果不存在则返回默认配置
     *
     * @param appKey appKey
     * @return app sdk配置
     */
    AppSdkConfig getConfigWithDefault(String appKey);

}
