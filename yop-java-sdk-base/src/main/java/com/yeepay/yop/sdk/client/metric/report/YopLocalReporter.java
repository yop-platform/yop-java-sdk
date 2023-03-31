/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * title: 本地上报器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/21
 */
public class YopLocalReporter implements YopReporter {

    private YopLocalReporter() {
    }

    public static final YopReporter INSTANCE = new YopLocalReporter();

    private static final Logger LOGGER = LoggerFactory.getLogger(YopLocalReporter.class);

    @Override
    public void report(YopReport report) throws YopReportException {
        LOGGER.info("YopReport Received, value:{}", report);
    }

    @Override
    public void batchReport(List<YopReport> report) throws YopReportException {
        if (CollectionUtils.isEmpty(report)) {
            return;
        }
        for (YopReport yopReport : report) {
            report(yopReport);
        }
    }
}
