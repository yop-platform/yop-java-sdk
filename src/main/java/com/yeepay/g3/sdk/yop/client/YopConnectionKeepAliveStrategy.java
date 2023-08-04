/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.g3.sdk.yop.client;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2019-03-26 13:24
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class YopConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopConnectionKeepAliveStrategy.class);

    @Override
    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        try {
            HeaderElementIterator it = new BasicHeaderElementIterator
                    (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (null != value && param.equalsIgnoreCase
                        ("timeout")) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("KeepAliveDuration Parsed From Server, timeout:{}s.", value);
                    }
                    return Long.parseLong(value) * 1000;
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("KeepAliveDuration Parsed Fail, ex:{}", ExceptionUtils.getMessage(e));
        }
        return 60 * 1000;
    }

}
