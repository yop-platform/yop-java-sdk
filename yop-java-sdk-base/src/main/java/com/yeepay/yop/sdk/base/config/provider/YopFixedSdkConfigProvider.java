package com.yeepay.yop.sdk.base.config.provider;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_ENV;
import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_PROVIDER;
import static com.yeepay.yop.sdk.constants.CharacterConstants.HASH;

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

    private Map<String, YopSdkConfig> sdkConfigMap = Maps.newConcurrentMap();

    @Override
    public final YopSdkConfig getConfig() {
        return getConfig(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV);
    }

    @Override
    public YopSdkConfig getConfig(String provider, String env) {
        final String theProvider = StringUtils.defaultString(provider, YOP_DEFAULT_PROVIDER);
        final String theEnv = StringUtils.defaultString(env, YOP_DEFAULT_ENV);
        return sdkConfigMap.computeIfAbsent(theProvider + HASH + theEnv,
                p -> loadSdkConfig(theProvider, theEnv));
    }

    /**
     * 加载用户自定义sdk配置
     *
     * @return 用户自定义sdk配置列表
     */
    protected abstract YopSdkConfig loadSdkConfig();

    protected YopSdkConfig loadSdkConfig(String provider, String env) {
        return loadSdkConfig();
    }

}
