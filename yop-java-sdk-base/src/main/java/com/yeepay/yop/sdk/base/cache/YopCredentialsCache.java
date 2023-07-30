/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.cache;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        if (null == credentials || null == appKey) {
            return;
        }
        CREDENTIALS_MAP.put(appKey, credentials);
    }

    /**
     * 获取调用凭证
     * @param appKey 应用
     * @return 调用凭证
     */
    public static YopCredentials<?> get(String appKey) {
        if (null == appKey) {
            return null;
        }
        return CREDENTIALS_MAP.get(appKey);
    }

    /**
     * 清除过期凭证
     * @param appKey 应用
     */
    public static void invalidate(String appKey) {
        if (null == appKey) {
            return;
        }
        CREDENTIALS_MAP.remove(appKey);
    }

    /**
     * 获取key列表
     * @return List
     */
    public static List<String> listKeys() {
        return new ArrayList<>(CREDENTIALS_MAP.keySet());
    }

}
