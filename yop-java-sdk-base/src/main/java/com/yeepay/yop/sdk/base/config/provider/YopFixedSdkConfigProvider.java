package com.yeepay.yop.sdk.base.config.provider;

import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: 固定方式加载sdk配置的提供方基类<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 20/11/23 11:29
 */
public abstract class YopFixedSdkConfigProvider implements YopSdkConfigProvider {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private YopSdkConfig sdkConfig = null;

    @Override
    public final YopSdkConfig getConfig() {
        if (null == sdkConfig) {
            synchronized (YopFixedSdkConfigProvider.class) {
                if (null == sdkConfig) {
                    sdkConfig = loadSdkConfig();
                }
            }
        }
        return sdkConfig;
    }

    /**
     * 加载用户自定义sdk配置
     *
     * @return 用户自定义sdk配置列表
     */
    protected abstract YopSdkConfig loadSdkConfig();

}
