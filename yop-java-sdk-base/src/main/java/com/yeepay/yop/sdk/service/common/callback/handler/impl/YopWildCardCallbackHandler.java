package com.yeepay.yop.sdk.service.common.callback.handler.impl;

import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.service.common.callback.YopCallback;
import com.yeepay.yop.sdk.service.common.callback.handler.YopCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.yeepay.yop.sdk.YopConstants.DEFAULT_YOP_CALLBACK_HANDLER;

/**
 * title: 简单打印<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2019-05-14 16:38
 */
public class YopWildCardCallbackHandler implements YopCallbackHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopWildCardCallbackHandler.class);

    @Override
    public String getType() {
        return DEFAULT_YOP_CALLBACK_HANDLER;
    }

    @Override
    public void handle(YopCallback callback) {
        LOGGER.warn("you need to handle the new yop callback, received: {}.", callback);
        throw new YopClientException("no YopCallbackHandler found");
    }

}
