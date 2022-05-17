package com.yeepay.yop.sdk.service.common.callback.handler;

import com.yeepay.yop.sdk.service.common.callback.handler.impl.YopWildCardCallbackHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.yeepay.yop.sdk.YopConstants.DEFAULT_YOP_CALLBACK_HANDLER;

/**
 * title: YOP 回调处理器工厂<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2019-05-14 16:45
 */
public class YopCallbackHandlerFactory {

    private static final Map<String, YopCallbackHandler> YOP_CALLBACK_HANDLERS = new ConcurrentHashMap<>();

    static {
        register(DEFAULT_YOP_CALLBACK_HANDLER, new YopWildCardCallbackHandler());
    }

    public static YopCallbackHandler getHandler(String type) {
        assert StringUtils.isNotEmpty(type);
        YopCallbackHandler handler = YOP_CALLBACK_HANDLERS.get(type);
        if (null == handler) {
            return YOP_CALLBACK_HANDLERS.get(DEFAULT_YOP_CALLBACK_HANDLER);
        } else {
            return handler;
        }
    }

    public static void register(String eventType, YopCallbackHandler yopCallbackHandler) {
        assert StringUtils.isNotEmpty(eventType);
        assert null != yopCallbackHandler;
        YOP_CALLBACK_HANDLERS.put(eventType, yopCallbackHandler);
    }

}
