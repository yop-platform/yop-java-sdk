/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report;

import java.util.List;

/**
 * title: 远程上报器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/21
 */
public class YopRemoteReporter implements YopReporter {

    public static final YopReporter INSTANCE = new YopRemoteReporter();
    private static final YopReporter BACKUP_REPORTER = YopLocalReporter.INSTANCE;

    @Override
    public void report(YopReport report) throws YopReportException {
        // TODO 调用远端
        BACKUP_REPORTER.report(report);
    }

    @Override
    public void batchReport(List<YopReport> report) throws YopReportException {
        // TODO 调用远端
        BACKUP_REPORTER.batchReport(report);
    }
}
