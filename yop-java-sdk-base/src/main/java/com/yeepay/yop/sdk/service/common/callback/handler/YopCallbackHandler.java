/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common.callback.handler;

import com.yeepay.yop.sdk.service.common.callback.YopCallback;

/**
 * title: YOP 回调处理器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2019-05-14 16:32
 */
public interface YopCallbackHandler {

    /**
     * 回调类型
     */
    String getType();

    /**
     * 处理
     *
     * @param callback Yop回调
     * @return
     */
    void handle(YopCallback callback);

}
