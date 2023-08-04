/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.event.host;

import com.yeepay.yop.sdk.client.metric.YopStatus;

/**
 * title: YOP域名请求成功事件<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/29
 */
public class YopHostSuccessEvent extends YopHostRequestEvent<Object> {

    @Override
    public YopStatus getStatus() {
        return YopStatus.SUCCESS;
    }
}
