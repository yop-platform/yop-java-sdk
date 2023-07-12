/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.client.YopGlobalClient;
import com.yeepay.yop.sdk.client.cmd.YopCmdExecutorRegistry;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.model.report.YopReportRequest;
import com.yeepay.yop.sdk.model.report.YopReportResponse;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.utils.JsonUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.yeepay.yop.sdk.YopConstants.REPORT_API_METHOD;
import static com.yeepay.yop.sdk.YopConstants.REPORT_API_URI;

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

    @Override
    public void report(YopReport report) throws YopReportException {
        batchReport(Lists.newArrayList(report));
    }

    private void doRemoteReport(List<YopReport> reports) throws YopReportException {
        try {
            YopRequest request = new YopRequest(REPORT_API_URI, REPORT_API_METHOD);
            // 跳过验签、加解密，使用默认appKey发起请求
            request.getRequestConfig().setSkipVerifySign(true).setNeedEncrypt(false).setReadTimeout(60000);
            YopReportRequest reportRequest = new YopReportRequest();
            reportRequest.setReports(reports);
            request.setContent(JsonUtils.toJsonString(reportRequest));
            final YopResponse response = YOP_CLIENT.request(request);
            handleReportResponse(response);
        } catch (YopClientException ex) {
            LOGGER.warn("Remote Report Fail For Client Error, exType:{}, exMsg:{}", ex.getClass().getCanonicalName(), ExceptionUtils.getMessage(ex));
            BACKUP_REPORTER.batchReport(reports);
        } catch (Exception e) {
            throw new YopReportException("Remote Report Fail For Server Error, ex:", e);
        }
    }

    private void handleReportResponse(YopResponse response) throws IOException {
        final YopReportResponse reportResponse = new YopReportResponse();
        JsonUtils.load(response.getStringResult(), reportResponse);
        YopCmdExecutorRegistry.get(reportResponse.getCmdType()).execute(reportResponse.getCmd());
    }

    @Override
    public void batchReport(List<YopReport> reports) throws YopReportException {
        doRemoteReport(reports);
    }
}