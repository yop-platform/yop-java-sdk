/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.base.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.client.metric.event.api.YopHostRequestEvent;
import com.yeepay.yop.sdk.client.metric.report.YopRemoteReporter;
import com.yeepay.yop.sdk.client.metric.report.YopReport;
import com.yeepay.yop.sdk.client.metric.report.YopReporter;
import com.yeepay.yop.sdk.client.metric.report.api.*;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.file.YopReportConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * title: 客户端上报<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/29
 */
public class ClientReporter extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientReporter.class);

    private static final YopReporter DEFAULT_REPORTER = YopRemoteReporter.INSTANCE;

    private static final LinkedBlockingQueue<YopHostRequestEvent<?>> YOP_HOST_REQUEST_EVENT_QUEUE;

    private static final ConcurrentMap<String, AtomicReference<YopHostRequestReport>> YOP_HOST_REQUEST_REPORTS = new ConcurrentHashMap<>();
    private static final ScheduledThreadPoolExecutor SEND_SCHEDULE_POOL = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setNameFormat("client-report-sender-%d").setDaemon(true).build());

    private static final ClientReporter REPORTER_PROXY = new ClientReporter();

    private static final int REPORT_INTERVAL_MILLISECONDS;
    private static final int REPORT_MIN_INTERVAL_MILLISECONDS;
    private static final int STAT_INTERVAL_MILLISECONDS;
    private static final int MAX_QUEUE_SIZE;
    private static final int MAX_PACKET_SIZE;
    private static final int MAX_FAIL_COUNT;
    private static final int MAX_FAIL_COUNT_PER_EX;
    private static final int MAX_ELAPSED_MILLISECONDS;
    private static final boolean REPORT;
    private static final boolean REPORT_SUCCESS;

    private static volatile boolean CLOSED = false;

    private ClientReporter() {
    }

    static {
        final YopSdkConfig sdkConfig = YopSdkConfigProviderRegistry.getProvider().getConfig();
        if (null == sdkConfig) {
            throw new YopClientException("Sdk Config Not Found");
        }
        final YopReportConfig yopReportConfig = sdkConfig.getYopReportConfig();
        if (null != yopReportConfig) {
            REPORT = yopReportConfig.isEnable();
            REPORT_SUCCESS = yopReportConfig.isEnableSuccess();
            REPORT_INTERVAL_MILLISECONDS = yopReportConfig.getIntervalInMilliseconds();
            REPORT_MIN_INTERVAL_MILLISECONDS = yopReportConfig.getMinIntervalInMilliseconds();
            STAT_INTERVAL_MILLISECONDS = yopReportConfig.getStatIntervalInMilliseconds();
            MAX_QUEUE_SIZE = yopReportConfig.getMaxQueueSize();
            MAX_PACKET_SIZE = yopReportConfig.getMaxPacketSize();
            MAX_FAIL_COUNT = yopReportConfig.getMaxFailCount();
            MAX_FAIL_COUNT_PER_EX = yopReportConfig.getMaxFailCountPerEx();
            MAX_ELAPSED_MILLISECONDS = yopReportConfig.getMaxElapsedTimeMillis();
        } else {
            REPORT = true;
            REPORT_SUCCESS = false;
            REPORT_INTERVAL_MILLISECONDS = 30000;
            REPORT_MIN_INTERVAL_MILLISECONDS = 2000;
            STAT_INTERVAL_MILLISECONDS = 5000;
            MAX_QUEUE_SIZE = 500;
            MAX_PACKET_SIZE = 50;
            MAX_FAIL_COUNT = 10;
            MAX_FAIL_COUNT_PER_EX = 5;
            MAX_ELAPSED_MILLISECONDS = 15000;
        }
        YOP_HOST_REQUEST_EVENT_QUEUE = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
        REPORTER_PROXY.setName("ClientReporter");
        REPORTER_PROXY.setDaemon(true);
        REPORTER_PROXY.start();
        SEND_SCHEDULE_POOL.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    sendReport();
                } catch (Throwable t) {
                    LOGGER.error("Unexpected Error, ex:", t);
                }
            }
        }, REPORT_INTERVAL_MILLISECONDS, REPORT_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    public static void reportHostRequest(YopHostRequestEvent<?> newEvent) {
        try {
            if (!REPORT) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignore Report Event, value:{}", newEvent);
                }
                return;
            }
            if (!REPORT_SUCCESS && YopHostRequestStatus.SUCCESS.equals(newEvent.getStatus())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignore Success Report Event, value:{}", newEvent);
                }
                return;
            }
            if (!YOP_HOST_REQUEST_EVENT_QUEUE.offer(newEvent)) {
                YopHostRequestEvent<?> oldEvent = YOP_HOST_REQUEST_EVENT_QUEUE.poll();
                if (oldEvent != null) {
                    LOGGER.info("Discard Old Event, value:{}", newEvent);
                }
                YOP_HOST_REQUEST_EVENT_QUEUE.offer(newEvent);
            }
        } catch (Exception exception) {
            LOGGER.error("Error Handle ReportEvent, value:" + newEvent, exception);
        }
    }

    @Override
    public void run() {
        while (!CLOSED) {
            try {
                aggregateEvents();
            } catch (Exception e) {
                LOGGER.warn("Error Happened, ex:", e);
            }
        }
    }

    private static void aggregateEvents() {
        final YopHostRequestEvent<?> event = YOP_HOST_REQUEST_EVENT_QUEUE.poll();
        if (null == event) return;

        final String serverHost = event.getServerHost();
        final String serverIp = event.getServerIp();
        final long elapsedMillis = event.getElapsedMillis();
        int successCount = 0;
        int failCount = 0;
        YopFailDetailItem failDetail = null;
        if (YopHostRequestStatus.SUCCESS.equals(event.getStatus())) {
            successCount = 1;
        } else {
            failCount = 1;
            failDetail = (YopFailDetailItem) event.getData();
        }

        final String reportKey = serverHost + serverIp;
        AtomicReference<YopHostRequestReport> reportReference = YOP_HOST_REQUEST_REPORTS.get(reportKey);

        if (null == reportReference) {
            YOP_HOST_REQUEST_REPORTS.putIfAbsent(reportKey, new AtomicReference<>());
            reportReference = YOP_HOST_REQUEST_REPORTS.get(reportKey);
        }

        // CompareAndSet并发加入统计数据
        YopHostRequestReport current;
        YopHostRequestReport update = new YopHostRequestReport();
        do {
            current = reportReference.get();
            if (current == null) {
                YopHostRequestPayload payload = new YopHostRequestPayload();
                update.setPayload(payload);
                payload.setServerIp(serverIp);
                payload.setServerHost(serverHost);
                payload.setSuccessCount(successCount);
                payload.setFailCount(failCount);
                payload.setMaxElapsedMillis(elapsedMillis);
                payload.setFailDetails(Lists.newLinkedList());
                if (null != failDetail) {
                    final YopFailDetail yopFailDetail = new YopFailDetail(failDetail.getExType(), failDetail.getExMsg());
                    yopFailDetail.getOccurTime().add(failDetail.getOccurTime());
                    payload.getFailDetails().add(yopFailDetail);
                }
            } else {
                YopHostRequestPayload oldPayload = current.getPayload();
                YopHostRequestPayload payload = new YopHostRequestPayload();
                update.setPayload(payload);
                payload.setServerIp(serverIp);
                payload.setServerHost(serverHost);
                payload.setSuccessCount(oldPayload.getSuccessCount() + successCount);
                payload.setFailCount(oldPayload.getFailCount() + failCount);
                payload.setMaxElapsedMillis(Math.max(elapsedMillis, oldPayload.getMaxElapsedMillis()));
                payload.setFailDetails(Lists.newLinkedList(oldPayload.getFailDetails()));
                final YopFailDetailItem failDetailItem = failDetail;
                if (null != failDetail) {
                    final Optional<YopFailDetail> yopFailDetail = payload.getFailDetails().stream().filter(p ->
                            StringUtils.equals(p.getExType(), failDetailItem.getExType())
                                    && StringUtils.equals(p.getExMsg(), failDetailItem.getExMsg()))
                            .findAny();
                    if (yopFailDetail.isPresent()) {
                        yopFailDetail.get().getOccurTime().add(failDetailItem.getOccurTime());
                    } else {
                        final YopFailDetail newYopFailDetail = new YopFailDetail(failDetail.getExType(), failDetail.getExMsg());
                        newYopFailDetail.getOccurTime().add(failDetail.getOccurTime());
                        payload.getFailDetails().add(newYopFailDetail);
                    }
                }
            }
        } while (!reportReference.compareAndSet(current, update));
    }

    private static void sendReport() {
        long timestamp = System.currentTimeMillis();
        List<YopReport> reports = Lists.newLinkedList();
        final Iterator<Map.Entry<String, AtomicReference<YopHostRequestReport>>> iterator =
                YOP_HOST_REQUEST_REPORTS.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, AtomicReference<YopHostRequestReport>> entry = iterator.next();
            AtomicReference<YopHostRequestReport> reference = entry.getValue();
            YopHostRequestReport report = reference.get();
            if (null == report) {
                continue;
            }
            // 达到上报条件
            if (needReport(timestamp, report)) {
                report.setEndTime(System.currentTimeMillis());
                reports.add(report);
                iterator.remove();
            }
        }
        if (CollectionUtils.isEmpty(reports)) {
            return;
        }
        for (List<YopReport> reportsSending : Lists.partition(reports, MAX_PACKET_SIZE)) {
            try {
                DEFAULT_REPORTER.batchReport(reportsSending);
                Thread.sleep(REPORT_MIN_INTERVAL_MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private static boolean needReport(long currentTime, YopHostRequestReport report) {
        final YopHostRequestPayload payload = report.getPayload();
        final long beginTime = report.getBeginTime();
        final int failCount = payload.getFailCount();
        final long maxElapsedMillis = payload.getMaxElapsedMillis();
        final List<YopFailDetail> failDetails = payload.getFailDetails();
        if (currentTime - beginTime > STAT_INTERVAL_MILLISECONDS) {
            return true;
        }
        if (failCount > MAX_FAIL_COUNT) {
            return true;
        }
        if (maxElapsedMillis > MAX_ELAPSED_MILLISECONDS) {
            return true;
        }
        if (CollectionUtils.isNotEmpty(failDetails)) {
            for (YopFailDetail failDetail : failDetails) {
                if (CollectionUtils.size(failDetail.getOccurTime()) > MAX_FAIL_COUNT_PER_EX) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 关闭上报器
     */
    public static void close() {
        try {
            CLOSED = true;
            REPORTER_PROXY.interrupt();
        } catch (Exception exception) {
            LOGGER.error("Error When Close ClientReporter", exception);
        }
    }

}
