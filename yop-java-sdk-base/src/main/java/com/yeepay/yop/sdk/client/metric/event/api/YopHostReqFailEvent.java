/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.event.api;

import com.yeepay.yop.sdk.client.metric.report.api.YopHostRequestStatus;
import com.yeepay.yop.sdk.client.metric.report.api.YopFailDetailItem;

/**
 * title: 失败事件<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/29
 */
public class YopHostReqFailEvent extends YopHostRequestEvent<YopFailDetailItem> {

    @Override
    public YopHostRequestStatus getStatus() {
        return YopHostRequestStatus.FAIL;
    }
}
