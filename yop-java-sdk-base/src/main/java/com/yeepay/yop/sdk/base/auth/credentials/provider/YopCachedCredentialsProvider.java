package com.yeepay.yop.sdk.base.auth.credentials.provider;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.base.config.YopAppConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * title: 带缓存的应用sdk配置提供方基类<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 20/11/23 16:05
 */
public abstract class YopCachedCredentialsProvider extends YopBaseCredentialsProvider {

    private final LoadingCache<String, YopAppConfig> configCache;

    public YopCachedCredentialsProvider() {
        this(-1L, null);
    }

    public YopCachedCredentialsProvider(Long expire, TimeUnit timeUnit) {
        this.configCache = initCache(expire, timeUnit);
    }


    @Override
    public YopCredentials<?> getCredentials(String appKey, String credentialType) {
        final YopAppConfig appConfig = loadFromCache(useDefaultIfBlank(appKey));
        return null != appConfig ? buildCredentials(appConfig, credentialType) : null;
    }

    @Override
    public List<CertTypeEnum> getSupportCertTypes(String appKey) {
        final YopAppConfig appConfig = loadFromCache(useDefaultIfBlank(appKey));
        return Lists.newArrayList(appConfig.getIsvPrivateKeys().keySet());
    }

    private LoadingCache<String, YopAppConfig> initCache(Long expire, TimeUnit timeUnit) {
        CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
        if (expire > 0) {
            cacheBuilder.expireAfterWrite(expire, timeUnit);
        }
        return cacheBuilder.build(new CacheLoader<String, YopAppConfig>() {
            @Override
            public YopAppConfig load(String appKey) throws Exception {
                logger.debug("try to load appSdkConfig for appKey:" + appKey);
                YopAppConfig yopAppConfig = null;
                try {
                    yopAppConfig = loadAppConfig(appKey);
                } catch (Exception ex) {
                    logger.warn("UnexpectedException occurred when loading appSdkConfig for appKey:" + appKey, ex);
                }
                return yopAppConfig;
            }
        });
    }

    /**
     * 加载App配置
     *
     * @param appKey appKey
     * @return App配置
     */
    protected abstract YopAppConfig loadAppConfig(String appKey);

    @Override
    public List<YopCertConfig> getIsvEncryptKey(String appKey) {
        final YopAppConfig appConfig = loadFromCache(useDefaultIfBlank(appKey));
        return null != appConfig ? appConfig.getIsvEncryptKey() : null;
    }

    private YopAppConfig loadFromCache(String appKey) {
        try {
            YopAppConfig appConfig = configCache.get(appKey);
            return appConfig;
        } catch (CacheLoader.InvalidCacheLoadException ex) {
            logger.warn("Null value was loaded when getting config for appKey:" + appKey);
            return null;
        } catch (Exception ex) {
            logger.error("Unexpected exception occurred when getting config for appKey:" + appKey, ex);
            return null;
        }
    }
}
