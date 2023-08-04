/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report.host;

import com.yeepay.yop.sdk.client.metric.report.AbstractYopReport;

/**
 * title: 上报内容-YOP域名请求<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/20
 */
public class YopHostRequestReport extends AbstractYopReport {

    private static final long serialVersionUID = -1L;

    private String type = "YopHostRequestReport";
    private YopHostRequestPayload payload;

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public YopHostRequestPayload getPayload() {
        return payload;
    }

    public void setPayload(YopHostRequestPayload payload) {
        this.payload = payload;
    }
}
