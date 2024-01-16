/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.cache;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_ENV;
import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_PROVIDER;
import static com.yeepay.yop.sdk.constants.CharacterConstants.COLON;

/**
 * title: YOP调用凭证缓存<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/7/30
 */
public class YopCredentialsCache {

    private static Map<String, YopCredentials<?>> CREDENTIALS_MAP = Maps.newConcurrentMap();

    /**
     * 更新最新凭证
     * @param appKey 应用
     * @param credentials 调用凭证
     */
    public static void put(String appKey, YopCredentials<?> credentials) {
        put(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, credentials);
    }
    public static void put(String provider, String env, String appKey, YopCredentials<?> credentials) {
        if (null == credentials) {
            return;
        }
        CREDENTIALS_MAP.put(cacheKey(provider, env, appKey), credentials);
    }

    /**
     * 获取调用凭证
     *
     * @param appKey   应用
     * @return 调用凭证
     */
    public static YopCredentials<?> get(String appKey) {
        return get(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey);
    }

    public static YopCredentials<?> get(String provider, String env, String appKey) {
        return CREDENTIALS_MAP.get(cacheKey(provider, env, appKey));
    }

    public static String cacheKey(String provider, String env, String appKey) {
        return StringUtils.defaultString(provider, YOP_DEFAULT_PROVIDER) + COLON +
                StringUtils.defaultString(env, YOP_DEFAULT_ENV) + COLON + appKey;
    }

    /**
     * 清除过期凭证
     * @param appKey 应用
     */
    public static void invalidate(String appKey) {
        invalidate(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey);
    }
    public static void invalidate(String provider, String env, String appKey) {
        CREDENTIALS_MAP.remove(cacheKey(provider, env, appKey));
    }

    /**
     * 获取key列表
     * @return List
     */
    public static List<String> listKeys() {
        return new ArrayList<>(CREDENTIALS_MAP.keySet());
    }

}
