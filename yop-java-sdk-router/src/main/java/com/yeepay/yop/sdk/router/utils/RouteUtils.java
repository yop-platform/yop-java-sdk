/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.utils;

import com.yeepay.yop.sdk.utils.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * title: 路由工具类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/3/21
 */
public class RouteUtils {


    /**
     * 资源监控标识
     *
     * @param resource  资源标识
     * @param separator 资源分组分隔符
     * @param prefixes  资源分组前缀
     * @return String
     */
    public static String resourceMonitorKey(String resource, String separator, String... prefixes) {
        if (null != prefixes) {
            return StringUtils.joinWith(separator, StringUtils.join(prefixes, separator), resource);
        }
        return resource;
    }

    /**
     * 随机散列列表
     *
     * @param origin 原列表
     * @param <T>    元素
     * @return List
     */
    public static <T> List<T> randomList(List<T> origin) {
        List<T> tmp = new ArrayList<>(origin);
        Collections.shuffle(tmp, RandomUtils.secureRandom());
        return tmp;
    }

    /**
     * 随机选一个
     *
     * @param origin 原列表
     * @param <T>    列表元素
     * @return T
     */
    public static <T> T randomOne(List<T> origin) {
        return origin.get(RandomUtils.secureRandom().nextInt(origin.size()));
    }
}
