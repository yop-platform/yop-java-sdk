/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.g3.sdk.yop.client.metric.report;

import com.google.common.collect.Lists;
import com.yeepay.g3.sdk.yop.cache.YopCredentialsCache;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import com.yeepay.g3.sdk.yop.client.YopRsaClient;
import com.yeepay.g3.sdk.yop.client.cmd.YopCmdExecutorRegistry;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.model.report.YopReportRequest;
import com.yeepay.g3.sdk.yop.model.report.YopReportResponse;
import com.yeepay.g3.sdk.yop.utils.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.yeepay.g3.sdk.yop.client.YopConstants.REPORT_API_URI;


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

    @Override
    public void report(YopReport report) throws YopReportException {
        batchReport(Lists.newArrayList(report));
    }

    private void doRemoteReport(List<YopReport> reports) throws YopReportException {
        try {
            YopRequest request;
            // 选择可用凭证
            final List<String> availableApps = YopCredentialsCache.listKeys();
            YopCredentialsCache.AppSecretItem credentials;
            if (CollectionUtils.isNotEmpty(availableApps)
                    && null != (credentials = YopCredentialsCache.get(availableApps.get(0)))) {
                if (StringUtils.isNotBlank(credentials.getSecretKey())) {
                    request = new YopRequest(credentials.getAppKey(), credentials.getSecretKey());
                } else {
                    request = new YopRequest(credentials.getAppKey());
                }
            } else {
                // 选择默认凭证
                request = new YopRequest();
            }

            // 跳过验签、加解密，使用默认appKey发起请求
            YopReportRequest reportRequest = new YopReportRequest();
            reportRequest.setReports(reports);
            request.setJsonParam(JsonUtils.toJsonString(reportRequest));
            final YopResponse response = YopRsaClient.post(REPORT_API_URI, request);
            handleReportResponse(response);
        } catch (YopClientException ex) {
            LOGGER.warn("Remote Report Fail For Client Error, exType:{}, exMsg:{}", ex.getClass().getCanonicalName(),
                    StringUtils.defaultString(ex.getMessage()));
            BACKUP_REPORTER.batchReport(reports);
        } catch (Exception e) {
            throw new YopReportException("Remote Report Fail For Server Error, ex:", e);
        }
    }

    private void handleReportResponse(YopResponse response) throws IOException {
        final YopReportResponse reportResponse = JsonUtils.fromJsonString(response.getStringResult(), YopReportResponse.class);
        YopCmdExecutorRegistry.get(reportResponse.getCmdType()).execute(reportResponse.getCmd());
    }

    @Override
    public void batchReport(List<YopReport> reports) throws YopReportException {
        doRemoteReport(reports);
    }
}
