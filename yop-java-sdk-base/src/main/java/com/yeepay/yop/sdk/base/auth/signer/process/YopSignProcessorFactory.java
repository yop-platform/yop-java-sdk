/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.auth.signer.process;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.exception.YopClientException;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * title: YopSignProcessorFactory<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/7/6 下午6:57
 */
public class YopSignProcessorFactory {

    private static final Map<String, YopSignProcessor> YOP_SIGN_PROCESSOR_MAP = Maps.newHashMapWithExpectedSize(4);

    static {
        ServiceLoader<YopSignProcessor> yopSignProcessorLoader = ServiceLoader.load(YopSignProcessor.class);
        for (YopSignProcessor yopSignProcessor : yopSignProcessorLoader) {
            YOP_SIGN_PROCESSOR_MAP.put(yopSignProcessor.name(), yopSignProcessor);
        }
    }

    public static YopSignProcessor getSignProcessor(String certType) {
        final YopSignProcessor yopSignProcessor = YOP_SIGN_PROCESSOR_MAP.get(certType);
        if (null == yopSignProcessor) {
            throw new YopClientException("YopSignProcessor not found, certType:" + certType);
        }
        return yopSignProcessor;
    }

    @Deprecated
    public static YopSignProcessor getYopSignProcess(String certType) {
        return getSignProcessor(certType);
    }

    public static void registerSignProcessor(String certType, YopSignProcessor yopSignProcessor) {
        YOP_SIGN_PROCESSOR_MAP.put(certType, yopSignProcessor);
    }

    @Deprecated
    public static void registeYopSignProcess(String certType, YopSignProcessor yopSignProcessor) {
        registerSignProcessor(certType, yopSignProcessor);
    }
}
