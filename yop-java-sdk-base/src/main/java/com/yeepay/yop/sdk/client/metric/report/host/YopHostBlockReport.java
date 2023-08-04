/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report.host;

import com.yeepay.yop.sdk.client.metric.report.AbstractYopReport;

import java.util.Date;

/**
 * title: 域名切换<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/8/4
 */
public class YopHostBlockReport extends AbstractYopReport {

    private static final long serialVersionUID = -1L;

    private String type = "YopHostBlockReport";
    private YopHostBlockPayload payload;

    public YopHostBlockReport(YopHostBlockPayload payload) {
        this.payload = payload;
        setEndDate(new Date());
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public YopHostBlockPayload getPayload() {
        return payload;
    }

    public void setPayload(YopHostBlockPayload payload) {
        this.payload = payload;
    }
}
