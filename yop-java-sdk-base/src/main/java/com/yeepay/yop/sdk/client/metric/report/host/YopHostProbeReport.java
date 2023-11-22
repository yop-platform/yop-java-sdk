/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report.host;

import com.yeepay.yop.sdk.client.metric.report.AbstractYopReport;

import java.util.Date;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/22
 */
public class YopHostProbeReport extends AbstractYopReport {

    private static final long serialVersionUID = -1L;

    private String type = "YopHostProbeReport";
    private YopHostProbeReportPayload payload;

    public YopHostProbeReport(YopHostProbeReportPayload payload) {
        this.payload = payload;
        setEndDate(new Date());
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public YopHostProbeReportPayload getPayload() {
        return payload;
    }
}
