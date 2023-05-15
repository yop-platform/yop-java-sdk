package com.yeepay.yop.sdk.config.provider;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfig;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfigProvider;
import com.yeepay.g3.core.yop.sdk.sample.config.SDKConfig;
import com.yeepay.g3.core.yop.sdk.sample.config.provider.support.AppSdkConfigInitTask;
import com.yeepay.g3.core.yop.sdk.sample.utils.Holder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * title: 带缓存的应用sdk配置提供方基类<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/5/17 16:05
 */
public abstract class BaseCachedAppSdkConfigProvider implements AppSdkConfigProvider {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LoadingCache<String, Holder<AppSdkConfig>> configCache;

    private final String defaultAppKey;

    public BaseCachedAppSdkConfigProvider() {
        this(null, -1L, null);
    }

    public BaseCachedAppSdkConfigProvider(String defaultAppKey) {
        this(defaultAppKey, -1L, null);
    }

    public BaseCachedAppSdkConfigProvider(Long expire, TimeUnit timeUnit) {
        this(null, expire, timeUnit);
    }

    public BaseCachedAppSdkConfigProvider(String defaultAppKey, Long expire, TimeUnit timeUnit) {
        this.configCache = initCache(expire, timeUnit);
        List<SDKConfig> initSdkConfigs = loadOnInit();
        if (CollectionUtils.isNotEmpty(initSdkConfigs)) {
            for (SDKConfig sdkConfig : initSdkConfigs) {
                this.configCache.put(sdkConfig.getAppKey(), new Holder<AppSdkConfig>(new AppSdkConfigInitTask(sdkConfig)));
                if (BooleanUtils.isTrue(sdkConfig.getDefaulted()) && defaultAppKey == null) {
                    defaultAppKey = sdkConfig.getAppKey();
                }
            }
        }
        this.defaultAppKey = defaultAppKey;
    }

    private final LoadingCache<String, Holder<AppSdkConfig>> initCache(Long expire, TimeUnit timeUnit) {
        CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
        if (expire > 0) {
            cacheBuilder.expireAfterWrite(expire, timeUnit);
        }
        return cacheBuilder.build(new CacheLoader<String, Holder<AppSdkConfig>>() {
            @Override
            public Holder<AppSdkConfig> load(String key) throws Exception {
                logger.debug("try to load appSdkConfig for appKey:" + key);
                try {
                    SDKConfig sdkConfig = loadSDKConfig(key);
                    if (sdkConfig == null) {
                        return null;
                    } else {
                        return new Holder<AppSdkConfig>(new AppSdkConfigInitTask(sdkConfig));
                    }
                } catch (Exception ex) {
                    logger.warn("UnexpectedException occurred when loading appSdkConfig for appKey:" + key, ex);
                    return null;
                }
            }
        });
    }

    @Override
    public AppSdkConfig getConfig(String appKey) {
        try {
            Holder<AppSdkConfig> holder = configCache.get(appKey);
            return holder == null ? null : holder.getValue();
        } catch (CacheLoader.InvalidCacheLoadException ex) {
            logger.warn("Null value was loaded when getting config for appKey:" + appKey);
            return null;
        } catch (Exception ex) {
            logger.error("Unexpected exception occurred when getting config for appKey:" + appKey, ex);
            return null;
        }
    }

    @Override
    public AppSdkConfig getDefaultConfig() {
        if (defaultAppKey == null) {
            return null;
        }
        return getConfig(defaultAppKey);
    }

    @Override
    public AppSdkConfig getConfigWithDefault(String appKey) {
        AppSdkConfig appSdkConfig = getConfig(appKey);
        return appSdkConfig == null ? (defaultAppKey == null ? null : getConfig(defaultAppKey)) : appSdkConfig;
    }

    /**
     * 初始化时需要加载sdk配置（会直接缓存起来）
     */
    protected List<SDKConfig> loadOnInit() {
        return null;
    }

    /**
     * 加载sdk配置
     *
     * @param appKey appKey
     * @return sdk配置
     */
    protected abstract SDKConfig loadSDKConfig(String appKey);


}
