/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yeepay.yop.sdk.base.security.encrypt.RsaEnhancer;
import com.yeepay.yop.sdk.base.security.encrypt.Sm2Enhancer;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptorFactory;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptionsEnhancer;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.yeepay.yop.sdk.YopConstants.*;
import static com.yeepay.yop.sdk.constants.CharacterConstants.COMMA;
import static com.yeepay.yop.sdk.constants.CharacterConstants.EMPTY;

/**
 * title: 加密选项缓存<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/26
 */
public class EncryptOptionsCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptOptionsCache.class);

    // 默认缓存24小时
    private static final LoadingCache<String, Future<EncryptOptions>> ENCRYPT_OPTIONS_CACHE = initCache(24L, TimeUnit.HOURS);

    /**
     * 初始化加密选项，并缓存
     *
     * @param appKey 应用
     * @param encryptAlg 加解密算法
     * @return Future<EncryptOptions>
     */
    public static Future<EncryptOptions> loadEncryptOptions(String appKey, String encryptAlg) {
        return loadEncryptOptions(appKey, encryptAlg, null);
    }

    /**
     * 初始化加密选项，并缓存
     *
     * @param appKey     应用
     * @param encryptAlg 加解密算法
     * @param serverRoot 平台证书请求端点
     * @return Future<EncryptOptions>
     */
    public static Future<EncryptOptions> loadEncryptOptions(String appKey, String encryptAlg, String serverRoot) {
        return loadEncryptOptions(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, encryptAlg, serverRoot);
    }

    public static Future<EncryptOptions> loadEncryptOptions(String provider, String env, String appKey, String encryptAlg, String serverRoot) {
        try {
            return ENCRYPT_OPTIONS_CACHE.get(getCacheKey(provider, env, appKey, encryptAlg, serverRoot));
        } catch (ExecutionException e) {
            throw new YopClientException("ConfigProblem, LoadEncryptOptions Fail, appKey:"
                    + appKey + ", encryptAlg:" + encryptAlg + ", ex:", e);
        }
    }

    /**
     * 刷新加密选项，并缓存（异步操作，不立即生效）
     *
     * Loads a new value for key, possibly asynchronously.
     * While the new value is loading the previous value (if any) will continue to be returned by get(key) unless it is evicted.
     * If the new value is loaded successfully it will replace the previous value in the cache;
     * if an exception is thrown while refreshing the previous value will remain, and the exception will be logged (using java.util.logging.Logger) and swallowed
     *
     * @param appKey     应用
     * @param encryptAlg 加解密算法
     */
    public static void refreshEncryptOptions(String appKey, String encryptAlg) {
        refreshEncryptOptions(appKey, encryptAlg, null);
    }

    /**
     * 刷新加密选项，并缓存（异步操作，不立即生效）
     *
     * Loads a new value for key, possibly asynchronously.
     * While the new value is loading the previous value (if any) will continue to be returned by get(key) unless it is evicted.
     * If the new value is loaded successfully it will replace the previous value in the cache;
     * if an exception is thrown while refreshing the previous value will remain, and the exception will be logged (using java.util.logging.Logger) and swallowed
     *
     * @param appKey     应用
     * @param encryptAlg 加解密算法
     * @param serverRoot 平台证书请求端点
     */
    public static void refreshEncryptOptions(String appKey, String encryptAlg, String serverRoot) {
        refreshEncryptOptions(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, encryptAlg, serverRoot);
    }

    public static void refreshEncryptOptions(String provider, String env, String appKey, String encryptAlg, String serverRoot) {
        try {
            ENCRYPT_OPTIONS_CACHE.refresh(getCacheKey(provider, env, appKey, encryptAlg, serverRoot));
        } catch (Exception e) {
            throw new YopClientException("ConfigProblem, RefreshEncryptOptions Fail, appKey:"
                    + appKey + ", encryptAlg:" + encryptAlg + ", ex:", e);
        }
    }

    /**
     * 立即失效加密选项
     *
     * @param appKey     应用
     * @param encryptAlg 加解密算法
     */
    public static void invalidateEncryptOptions(String appKey, String encryptAlg) {
        invalidateEncryptOptions(appKey, encryptAlg, null);
    }

    /**
     * 立即失效加密选项
     *
     * @param appKey     应用
     * @param encryptAlg 加解密算法
     * @param serverRoot 平台证书请求端点
     */
    public static void invalidateEncryptOptions(String appKey, String encryptAlg, String serverRoot) {
        invalidateEncryptOptions(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, encryptAlg, serverRoot);
    }

    public static void invalidateEncryptOptions(String provider, String env, String appKey, String encryptAlg, String serverRoot) {
        try {
            ENCRYPT_OPTIONS_CACHE.invalidate(getCacheKey(provider, env, appKey, encryptAlg, serverRoot));
        } catch (Exception e) {
            throw new YopClientException("ConfigProblem, InvalidateEncryptOptions Fail, appKey:"
                    + appKey + ", encryptAlg:" + encryptAlg + ", ex:", e);
        }
    }

    /**
     * 失效并同步加载新的加密选项，并缓存
     *
     * @param appKey 应用
     * @param encryptAlg 加解密算法
     * @return Future<EncryptOptions>
     */
    public static Future<EncryptOptions> reloadEncryptOptions(String appKey, String encryptAlg) {
        return reloadEncryptOptions(appKey, encryptAlg, null);
    }

    /**
     * 失效并同步加载新的加密选项，并缓存
     *
     * @param appKey     应用
     * @param encryptAlg 加解密算法
     * @param serverRoot 平台证书请求端点
     * @return Future<EncryptOptions>
     */
    public static Future<EncryptOptions> reloadEncryptOptions(String appKey, String encryptAlg, String serverRoot) {
        return reloadEncryptOptions(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, encryptAlg, serverRoot);
    }

    public static Future<EncryptOptions> reloadEncryptOptions(String provider, String env, String appKey, String encryptAlg, String serverRoot) {
        try {
            ENCRYPT_OPTIONS_CACHE.invalidate(getCacheKey(provider, env, appKey, encryptAlg, serverRoot));
        } catch (Exception e) {
            throw new YopClientException("ConfigProblem, InvalidateEncryptOptions Fail, appKey:"
                    + appKey + ", encryptAlg:" + encryptAlg + ", ex:", e);
        }
        return loadEncryptOptions(provider, env, appKey, encryptAlg, serverRoot);
    }

    private static String getCacheKey(String provider, String env, String appKey, String encryptAlg, String serverRoot) {
        return StringUtils.joinWith(COMMA, StringUtils.defaultIfBlank(provider, YOP_DEFAULT_PROVIDER),
                StringUtils.defaultIfBlank(env, YOP_DEFAULT_ENV),
                StringUtils.defaultIfBlank(appKey, YOP_DEFAULT_APPKEY),
                StringUtils.defaultIfBlank(encryptAlg, EMPTY), StringUtils.defaultIfBlank(serverRoot, EMPTY));
    }

    private static LoadingCache<String, Future<EncryptOptions>> initCache(Long expire, TimeUnit timeUnit) {
        CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
        if (expire > 0) {
            cacheBuilder.expireAfterWrite(expire, timeUnit);
        }
        return cacheBuilder.build(new CacheLoader<String, Future<EncryptOptions>>() {
            @Override
            public Future<EncryptOptions> load(String cacheKey) throws Exception {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("try to init encryptOptions for cacheKey:{}", cacheKey);
                }
                Future<EncryptOptions> encryptOptions = null;
                try {
                    String[] split = StringUtils.splitPreserveAllTokens(cacheKey, COMMA);
                    String provider = split[0], env = split[1]
                            ,appKey = split[2], encryptAlg = split[3]
                            , serverRoot = split.length > 4 ? split[4]: null;
                    YopEncryptor encryptor = YopEncryptorFactory.getEncryptor(encryptAlg);
                    List<EncryptOptionsEnhancer> enhancers = Collections.singletonList(getEnhancer(provider, env, appKey, encryptAlg, serverRoot));
                    encryptOptions = encryptor.initOptions(encryptAlg, enhancers);
                } catch (Exception ex) {
                    LOGGER.warn("UnexpectedException occurred when init encryptOptions for cacheKey:" + cacheKey, ex);
                }
                return encryptOptions;
            }
        });
    }

    private static EncryptOptionsEnhancer getEnhancer(String provider, String env,
                                                      String appKey,
                                                      String encryptAlg,
                                                      String serverRoot) {
        switch (encryptAlg) {
            case SM4_CBC_PKCS5PADDING:
                return new Sm2Enhancer(provider, env, appKey, EMPTY, serverRoot);
            case AES:
            case AES_ECB_PKCS5PADDING:
                return new RsaEnhancer(provider, env, appKey);
            default:
                throw new YopClientException("not supported encryptAlg:" + encryptAlg);
        }
    }

}
