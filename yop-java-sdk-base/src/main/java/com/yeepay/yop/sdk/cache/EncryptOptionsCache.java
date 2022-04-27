/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptionsEnhancer;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptorFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.yeepay.yop.sdk.utils.CharacterConstants.COMMA;

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
        try {
            return ENCRYPT_OPTIONS_CACHE.get(StringUtils.joinWith(COMMA, appKey, encryptAlg));
        } catch (ExecutionException e) {
            throw new YopClientException("initEncryptOptions error, ex:", e);
        }
    }

    private static LoadingCache<String, Future<EncryptOptions>> initCache(Long expire, TimeUnit timeUnit) {
        CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
        if (expire > 0) {
            cacheBuilder.expireAfterWrite(expire, timeUnit);
        }
        return cacheBuilder.build(new CacheLoader<String, Future<EncryptOptions>>() {
            @Override
            public Future<EncryptOptions> load(String cacheKey) throws Exception {
                LOGGER.debug("try to init encryptOptions for cacheKey:" + cacheKey);
                Future<EncryptOptions> encryptOptions = null;
                try {
                    String[] split = cacheKey.split(COMMA);
                    String appKey = split[0], encryptAlg = split[1];
                    YopEncryptor encryptor = YopEncryptorFactory.getEncryptor(encryptAlg);
                    List<EncryptOptionsEnhancer> enhancers = Collections.singletonList(new EncryptOptionsEnhancer.Sm2Enhancer(appKey));
                    encryptOptions = encryptor.initOptions(encryptAlg, enhancers);
                } catch (Exception ex) {
                    LOGGER.warn("UnexpectedException occurred when init encryptOptions for cacheKey:" + cacheKey, ex);
                }
                return encryptOptions;
            }
        });
    }

}
