/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.base.cache.YopCredentialsCache;
import com.yeepay.yop.sdk.client.YopGlobalClient;
import com.yeepay.yop.sdk.client.cmd.YopCmdExecutorRegistry;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.model.report.YopReportRequest;
import com.yeepay.yop.sdk.model.report.YopReportResponse;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.utils.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
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
public class YopRemoteReporter implements YopReporter, YopProbeReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopRemoteReporter.class);

    public static final YopRemoteReporter INSTANCE = new YopRemoteReporter();
    private static final YopReporter BACKUP_REPORTER = YopLocalReporter.INSTANCE;
    private static final YopClient YOP_CLIENT = YopGlobalClient.getClient();

    @Override
    public void report(YopReport report) throws YopReportException {
        batchReport(Lists.newArrayList(report));
    }

    private void doRemoteReport(YopRequest request, List<YopReport> reports) throws YopReportException {
        try {
            YopReportRequest reportRequest = new YopReportRequest();
            reportRequest.setReports(reports);
            request.setContent(JsonUtils.toJsonString(reportRequest));
            final YopResponse response = YOP_CLIENT.request(request);
            handleReportResponse(response);
        } catch (YopClientException ex) {
            LOGGER.warn("Remote Report Fail For Client Error, exType:{}, exMsg:{}", ex.getClass().getCanonicalName(),
                    StringUtils.defaultString(ex.getMessage()));
            BACKUP_REPORTER.batchReport(reports);
        } catch (Exception e) {
            throw new YopReportException("Remote Report Fail For Server Error, ex:", e);
        }
    }

    private YopRequest initReportRequest() {
        YopRequest request = new YopRequest(REPORT_API_URI, REPORT_API_METHOD);
        // 跳过验签、加解密
        request.getRequestConfig().setSkipVerifySign(true).setNeedEncrypt(false).setReadTimeout(60000);

        // 选择可用凭证
        chooseAvailableCredentials(request);
        return request;
    }

    private void handleReportResponse(YopResponse response) throws IOException {
        final YopReportResponse reportResponse = new YopReportResponse();
        JsonUtils.load(response.getStringResult(), reportResponse);
        YopCmdExecutorRegistry.get(reportResponse.getCmdType()).execute(reportResponse.getCmd());
    }

    @Override
    public void batchReport(List<YopReport> reports) throws YopReportException {
        YopRequest request = initReportRequest();
        doRemoteReport(request, reports);
    }

    @Override
    public void probeReport(String serverRoot, YopReport report) throws YopReportException {
        YopRequest request = initProbeReportRequest(serverRoot);
        doRemoteReport(request, Collections.singletonList(report));
    }

    private YopRequest initProbeReportRequest(String serverRoot) {
        YopRequest request = new YopRequest(REPORT_API_URI, REPORT_API_METHOD);
        // 跳过验签、加解密、禁用断路器
        request.getRequestConfig().setSkipVerifySign(true).setNeedEncrypt(false)
                .setReadTimeout(3000)
                .setEnableCircuitBreaker(false).setServerRoot(serverRoot);

        // 选择可用凭证
        chooseAvailableCredentials(request);
        return request;
    }

    private void chooseAvailableCredentials(YopRequest request) {
        final List<String> availableApps = YopCredentialsCache.listKeys();
        YopCredentials<?> credentials;
        if (CollectionUtils.isNotEmpty(availableApps)
                && null != (credentials = YopCredentialsCache.get(availableApps.get(0)))) {
            request.getRequestConfig().setAppKey(availableApps.get(0));
            request.getRequestConfig().setCredentials(credentials);
        }
    }
}
