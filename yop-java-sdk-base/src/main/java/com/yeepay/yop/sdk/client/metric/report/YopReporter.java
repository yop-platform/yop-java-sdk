/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report;

import java.util.List;

/**
 * title: 上报器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/20
 */
public interface YopReporter {

    void report(YopReport report) throws YopReportException;

    void batchReport(List<YopReport> report) throws YopReportException;

    default void report(String provider, String env, YopReport report) throws YopReportException {
        report(report);
    }

    default void batchReport(String provider, String env, List<YopReport> report) throws YopReportException {
        batchReport(report);
    }

}
