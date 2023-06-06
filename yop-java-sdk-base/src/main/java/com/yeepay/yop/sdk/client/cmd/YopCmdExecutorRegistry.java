/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.cmd;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.client.cmd.impl.YopDefaultCmdExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * title: Yop命令执行器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/6/6
 */
public class YopCmdExecutorRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopCmdExecutorRegistry.class);

    /**
     * 执行器Map
     * key: 命令类型(shell等)
     * value: 命令执行器
     */
    private static final Map<String, YopCmdExecutor> EXECUTOR_MAP = Maps.newHashMap();

    private static final YopCmdExecutor DEFAULT_EXECUTOR = new YopDefaultCmdExecutor();

    static {
        ServiceLoader<YopCmdExecutor> serviceLoader = ServiceLoader.load(YopCmdExecutor.class);
        for (YopCmdExecutor cmdExecutor : serviceLoader) {
            for (String alg : cmdExecutor.support()) {
                EXECUTOR_MAP.put(alg, cmdExecutor);
            }
        }
    }

    public static void register(String type, YopCmdExecutor digester) {
        EXECUTOR_MAP.put(type, digester);
    }

    public static YopCmdExecutor get(String type) {
        final YopCmdExecutor executor = EXECUTOR_MAP.get(type);
        if (null == executor) {
            LOGGER.warn("No YopCmdExecutor Found, type:{}", type);
            return DEFAULT_EXECUTOR;
        }
        return executor;
    }

}
