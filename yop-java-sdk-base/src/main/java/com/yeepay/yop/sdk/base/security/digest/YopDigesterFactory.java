/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.digest;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.security.digest.YopDigester;
import com.yeepay.yop.sdk.exception.YopClientException;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * title: 摘要器工厂类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/11
 */
public class YopDigesterFactory {

    /**
     * 摘要器Map
     * key: 摘要算法
     * value: 摘要器
     */
    private static final Map<String, YopDigester> YOP_DIGESTER_MAP = Maps.newHashMap();

    static {
        ServiceLoader<YopDigester> serviceLoader = ServiceLoader.load(YopDigester.class);
        for (YopDigester digester : serviceLoader) {
            for (String alg : digester.supportedAlgs()) {
                YOP_DIGESTER_MAP.put(alg, digester);
            }
        }
    }

    /**
     * 扩展算法
     *
     * @param alg      算法名称
     * @param digester 摘要器
     */
    public static void registerDigester(String alg, YopDigester digester) {
        YOP_DIGESTER_MAP.put(alg, digester);
    }

    /**
     * 根据摘要算法获取摘要器
     *
     * @param alg 摘要算法
     * @return 摘要器
     */
    public static YopDigester getDigester(String alg) {
        final YopDigester yopDigester = YOP_DIGESTER_MAP.get(alg);
        if (null == yopDigester) {
            throw new YopClientException("YopDigester not found, alg:" + alg);
        }
        return yopDigester;
    }

}
