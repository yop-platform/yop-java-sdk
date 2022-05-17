/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common;

import com.yeepay.yop.sdk.service.common.callback.YopCallback;
import com.yeepay.yop.sdk.service.common.callback.YopCallbackRequest;
import com.yeepay.yop.sdk.service.common.callback.YopCallbackResponse;
import com.yeepay.yop.sdk.service.common.callback.handler.YopCallbackHandlerFactory;
import com.yeepay.yop.sdk.service.common.callback.protocol.YopCallbackProtocolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: Yop商户回调处理引擎<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/12
 */
public class YopCallbackEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopCallbackEngine.class);

    /**
     * 解析Yop回调请求
     * 1.解析协议
     * 2.解密数字信封
     *
     * @param request 原始请求
     * @return 解密后的通知参数
     */
    public static YopCallback parse(YopCallbackRequest request) {
        return YopCallbackProtocolFactory.fromRequest(request).parse();
    }

    /**
     * 处理Yop回调请求
     * 1.@see #parse
     * 2.查找并处理商户业务
     * 3.组装响应参数
     *
     * @param request 原始请求
     * @return Yop响应报文
     */
    public static YopCallbackResponse handle(YopCallbackRequest request) {
        final YopCallback callback = parse(request);

        // 业务处理(是否可以异步？)
        try {
            YopCallbackHandlerFactory.getHandler(callback.getType()).handle(callback);
        } catch (Throwable e) {
            LOGGER.error("error when handle YopCallbackRequest, ex:", e);
            return YopCallbackResponse.fail(e.getMessage());
        }

        // 返回通知状态码(通知规则中有定义)
        return YopCallbackResponse.success();
    }
}
