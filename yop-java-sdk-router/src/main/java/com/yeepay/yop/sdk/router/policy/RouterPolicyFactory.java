/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.policy;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.invoke.RouterPolicy;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * title: 路由策略工厂<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/1
 */
public class RouterPolicyFactory {

    /**
     * 路由策略Map
     * <p>
     * key: 路由策略名称
     * value: 路由策略
     */
    private static final Map<String, RouterPolicy> SERVICE_MAP = Maps.newHashMap();

    static {
        ServiceLoader<RouterPolicy> serviceLoader = ServiceLoader.load(RouterPolicy.class);
        for (RouterPolicy item : serviceLoader) {
            SERVICE_MAP.put(item.name(), item);
        }
    }

    /**
     * 扩展路由策略
     *
     * @param name 路由策略名称
     * @param item 路由策略
     */
    public static void register(String name, RouterPolicy item) {
        SERVICE_MAP.put(name, item);
    }

    /**
     * 根据路由策略名称获取路由策略
     *
     * @param name 路由策略名称
     * @return 路由策略
     */
    public static RouterPolicy get(String name) {
        final RouterPolicy routerPolicy = SERVICE_MAP.get(name);
        if (null == routerPolicy) {
            throw new YopClientException("ConfigProblem, RouterPolicy NotFound, name:" + name);
        }
        return routerPolicy;
    }

}
