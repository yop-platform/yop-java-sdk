/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.sentinel;

import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.log.LogTarget;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.log.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: yop定制，record-log输出控制<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/12/5
 */
@LogTarget
public class YopSentinelRecordLogger implements Logger {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(YopSentinelRecordLogger.class);

    @Override
    public void info(String format, Object... arguments) {
        if (YopSentinelConstants.SDK_ROUTER_SENTINEL_DEBUG) {
            LOGGER.info(format, arguments);
        }
    }

    @Override
    public void info(String msg, Throwable e) {
        if (YopSentinelConstants.SDK_ROUTER_SENTINEL_DEBUG) {
            LOGGER.info(msg, e);
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        LOGGER.warn(format, arguments);
    }

    @Override
    public void warn(String msg, Throwable e) {
        LOGGER.info(msg, e);
    }

    @Override
    public void trace(String format, Object... arguments) {
        LOGGER.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable e) {
        LOGGER.trace(msg, e);
    }

    @Override
    public void debug(String format, Object... arguments) {
        LOGGER.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable e) {
        LOGGER.debug(msg, e);
    }

    @Override
    public void error(String format, Object... arguments) {
        LOGGER.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable e) {
        LOGGER.error(msg, e);
    }
}
