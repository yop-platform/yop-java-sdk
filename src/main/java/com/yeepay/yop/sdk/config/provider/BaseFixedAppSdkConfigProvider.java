package com.yeepay.yop.sdk.config.provider;

import com.google.common.collect.Maps;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfig;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfigProvider;
import com.yeepay.g3.core.yop.sdk.sample.config.SDKConfig;
import com.yeepay.g3.core.yop.sdk.sample.config.provider.support.AppSdkConfigInitTask;
import com.yeepay.g3.core.yop.sdk.sample.utils.CheckUtils;
import com.yeepay.g3.core.yop.sdk.sample.utils.Holder;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * title: Fixed应用sdk配置提供方基类<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/5/17 11:29
 */
public abstract class BaseFixedAppSdkConfigProvider implements AppSdkConfigProvider {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Holder<AppSdkConfig>> configs = Maps.newHashMap();

    private Holder<AppSdkConfig> defaultConfig = null;

    private volatile boolean init = false;

    @Override
    public final AppSdkConfig getConfig(String appKey) {
        if (!init) {
            init();
        }
        Holder<AppSdkConfig> holder = configs.get(appKey);
        return holder == null ? null : holder.getValue();
    }

    @Override
    public final AppSdkConfig getDefaultConfig() {
        if (!init) {
            init();
        }
        return defaultConfig == null ? null : defaultConfig.getValue();
    }

    @Override
    public AppSdkConfig getConfigWithDefault(String appKey) {
        if (!init) {
            init();
        }
        Holder<AppSdkConfig> holder = configs.get(appKey);
        return holder == null ? (defaultConfig == null ? null : defaultConfig.getValue()) : holder.getValue();
    }

    private synchronized void init() {
        if (init) {
            return;
        }
        List<SDKConfig> customSdkConfigs = loadCustomSdkConfig();
        if (customSdkConfigs == null || customSdkConfigs.size() == 0) {
            logger.warn("no custom sdkConfig provided.");
        } else {
            boolean hasDefault = false;
            for (SDKConfig sdkConfig : customSdkConfigs) {
                CheckUtils.checkCustomSDKConfig(sdkConfig);
                Holder<AppSdkConfig> holder = new Holder<AppSdkConfig>(new AppSdkConfigInitTask(sdkConfig));
                configs.put(sdkConfig.getAppKey(), holder);
                if (BooleanUtils.isTrue(sdkConfig.getDefaulted()) && !hasDefault) {
                    defaultConfig = holder;
                    hasDefault = true;
                }
            }
            if (!hasDefault) {
                defaultConfig = configs.get(customSdkConfigs.get(0).getAppKey());
            }
        }
        init = true;
    }


    /**
     * 加载用户自定义sdk配置
     *
     * @return 用户自定义sdk配置列表
     */
    protected abstract List<SDKConfig> loadCustomSdkConfig();

}
