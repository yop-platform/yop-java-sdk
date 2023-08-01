/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.model.report;

import com.yeepay.yop.sdk.client.metric.report.YopReport;

import java.io.Serializable;
import java.util.List;

/**
 * title: 上报请求<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/6/13
 */
public class YopReportRequest implements Serializable {

    private static final long serialVersionUID = -1L;

    private List<YopReport> reports;

    public List<YopReport> getReports() {
        return reports;
    }

    public void setReports(List<YopReport> reports) {
        this.reports = reports;
    }
}
