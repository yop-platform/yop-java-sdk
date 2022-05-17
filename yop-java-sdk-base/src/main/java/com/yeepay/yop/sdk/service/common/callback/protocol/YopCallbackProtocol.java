/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common.callback.protocol;

import com.yeepay.yop.sdk.service.common.callback.YopCallback;

/**
 * title: Yop回调协议<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/16
 */
public interface YopCallbackProtocol {

    YopCallback parse();
}
