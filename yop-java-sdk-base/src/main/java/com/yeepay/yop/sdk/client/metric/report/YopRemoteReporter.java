/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.base.cache.YopCredentialsCache;
import com.yeepay.yop.sdk.client.cmd.YopCmdExecutorRegistry;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.model.report.YopReportRequest;
import com.yeepay.yop.sdk.model.report.YopReportResponse;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.utils.ClientUtils;
import com.yeepay.yop.sdk.utils.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    public static final YopRemoteReporter INSTANCE = new YopRemoteReporter();
    private static final YopReporter BACKUP_REPORTER = YopLocalReporter.INSTANCE;

    @Override
    public void report(YopReport report) throws YopReportException {
        batchReport(YopConstants.YOP_DEFAULT_PROVIDER, YopConstants.YOP_DEFAULT_ENV, Lists.newArrayList(report));
    }

    private void doRemoteReport(String provider, String env, YopRequest request, List<YopReport> reports) throws YopReportException {
        try {
            YopReportRequest reportRequest = new YopReportRequest();
            reportRequest.setReports(reports);
            request.setContent(JsonUtils.toJsonString(reportRequest));
            final YopResponse response = ClientUtils.getAvailableYopClient(provider, env).request(request);
            handleReportResponse(response);
        } catch (YopClientException ex) {
            LOGGER.warn("Remote Report Fail For Client Error, exType:{}, exMsg:{}", ex.getClass().getCanonicalName(),
                    StringUtils.defaultString(ex.getMessage()));
            BACKUP_REPORTER.batchReport(reports);
        } catch (Exception e) {
            throw new YopReportException("Remote Report Fail For Server Error, ex:", e);
        }
    }

    private YopRequest initReportRequest(String provider, String env) {
        YopRequest request = new YopRequest(REPORT_API_URI, REPORT_API_METHOD);
        // 跳过验签、加解密
        request.getRequestConfig().setSkipVerifySign(true).setNeedEncrypt(false).setReadTimeout(60000);

        // 选择可用凭证
        chooseAvailableCredentials(request, provider, env);
        return request;
    }

    private void handleReportResponse(YopResponse response) throws IOException {
        final YopReportResponse reportResponse = new YopReportResponse();
        JsonUtils.load(response.getStringResult(), reportResponse);
        YopCmdExecutorRegistry.get(reportResponse.getCmdType()).execute(reportResponse.getCmd());
    }

    @Override
    public void batchReport(List<YopReport> reports) throws YopReportException {
        batchReport(YopConstants.YOP_DEFAULT_PROVIDER, YopConstants.YOP_DEFAULT_ENV, reports);
    }

    private void chooseAvailableCredentials(YopRequest request, String provider, String env) {
        final List<String> availableCredentials = YopCredentialsCache.listKeys(provider, env);
        YopCredentials<?> credentials;
        if (CollectionUtils.isNotEmpty(availableCredentials)
                && null != (credentials = YopCredentialsCache.get(availableCredentials.get(0)))) {
            request.getRequestConfig().setAppKey(availableCredentials.get(0));
            request.getRequestConfig().setCredentials(credentials);
        }
    }

    @Override
    public void report(String provider, String env, YopReport report) throws YopReportException {
        batchReport(provider, env, Lists.newArrayList(report));
    }

    @Override
    public void batchReport(String provider, String env, List<YopReport> reports) throws YopReportException {
        YopRequest request = initReportRequest(provider, env);
        doRemoteReport(provider, env, request, reports);
    }
}
