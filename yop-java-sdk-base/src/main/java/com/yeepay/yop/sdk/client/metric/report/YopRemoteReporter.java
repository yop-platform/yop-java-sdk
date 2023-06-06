/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.client.YopGlobalClient;
import com.yeepay.yop.sdk.client.cmd.YopCmdExecutorRegistry;
import com.yeepay.yop.sdk.model.report.YopReportResponse;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(YopRemoteReporter.class);
    public static final YopReporter INSTANCE = new YopRemoteReporter();
    private static final YopReporter BACKUP_REPORTER = YopLocalReporter.INSTANCE;
    private static final YopClient YOP_CLIENT = YopGlobalClient.getClient();
    private static final String REPORT_API_URI = "/rest/v1.0/yop/client/report", REPORT_API_METHOD = "POST";

    @Override
    public void report(YopReport report) throws YopReportException {
        batchReport(Lists.newArrayList(report));
    }

    private void doRemoteReport(List<YopReport> report) throws Exception {
        YopRequest request = new YopRequest(REPORT_API_URI, REPORT_API_METHOD);
        // 跳过验签、加解密，使用默认appKey发起请求
        request.getRequestConfig().setSkipVerifySign(true).setNeedEncrypt(false);
        request.setContent(JsonUtils.toJsonString(report));
        final YopResponse response = YOP_CLIENT.request(request);
        handleReportResponse(response);
    }

    private void handleReportResponse(YopResponse response) throws IOException {
        final YopReportResponse reportResponse = new YopReportResponse();
        JsonUtils.load(response.getStringResult(), reportResponse);
        YopCmdExecutorRegistry.get(reportResponse.getCmdType()).execute(reportResponse.getCmd());
    }

    @Override
    public void batchReport(List<YopReport> reports) throws YopReportException {
        try {
            doRemoteReport(reports);
        } catch (Exception e) {
            LOGGER.error("Fail To Report Remote, ex:", e);
            BACKUP_REPORTER.batchReport(reports);
        }
    }
}
