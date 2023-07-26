/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http;

import org.apache.http.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * title: apache响应设置扩充server信息头<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/22
 */
public class YopServerResponseInterceptor implements HttpResponseInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopServerResponseInterceptor.class);

    public static final YopServerResponseInterceptor INSTANCE = new YopServerResponseInterceptor();

    private YopServerResponseInterceptor() {
    }

    @Override
    public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        try {
            final Header alreadyUsed = response.getFirstHeader(Headers.YOP_SERVER_IP);
            if (null == alreadyUsed) {
                response.addHeader(Headers.YOP_SERVER_IP,
                        ((HttpInetConnection) context.getAttribute(HttpCoreContext.HTTP_CONNECTION)).getRemoteAddress().getHostAddress());
            }
        } catch (Exception e) {
            LOGGER.error("error when get yop server info, ex:", e);
        }
    }
}
