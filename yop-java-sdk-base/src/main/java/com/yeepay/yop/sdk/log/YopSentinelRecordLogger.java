/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.log;

import com.alibaba.csp.sentinel.log.LogTarget;
import com.alibaba.csp.sentinel.log.Logger;
import com.yeepay.yop.sdk.YopConstants;
import org.slf4j.LoggerFactory;

/**
 * title: <br>
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
        if (YopConstants.SDK_DEBUG) {
            LOGGER.info(format, arguments);
        }
    }

    @Override
    public void info(String msg, Throwable e) {
        if (YopConstants.SDK_DEBUG) {
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
