/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.base.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.client.metric.YopFailureList;
import com.yeepay.yop.sdk.client.metric.YopFailureItem;
import com.yeepay.yop.sdk.client.metric.YopStatus;
import com.yeepay.yop.sdk.client.metric.event.host.YopHostRequestEvent;
import com.yeepay.yop.sdk.client.metric.report.*;
import com.yeepay.yop.sdk.client.metric.report.host.*;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.file.YopReportConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
public class ClientReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientReporter.class);

    private static final YopReporter DEFAULT_REPORTER = YopRemoteReporter.INSTANCE;

    private static final ConcurrentMap<String, AtomicReference<YopHostRequestReport>> YOP_HOST_REQUEST_COLLECTION = new ConcurrentHashMap<>();

    // 打包上报事件
    private static final ThreadPoolExecutor COLLECT_POOL;

    // 将打包后的事件发送到远端
    private static final ScheduledThreadPoolExecutor SEND_SCHEDULE_POOL = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setNameFormat("client-report-sender-%d").setDaemon(true).build());

    private static final int REPORT_INTERVAL_MS;
    private static final int REPORT_MIN_INTERVAL_MS;
    private static final int STAT_INTERVAL_MS;
    private static final int MAX_QUEUE_SIZE;
    private static final int MAX_PACKET_SIZE;
    private static final int MAX_FAIL_COUNT;
    private static final int MAX_FAIL_COUNT_PER_EX;
    private static final int MAX_ELAPSED_MS;
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
            REPORT_SUCCESS = yopReportConfig.isEnableSuccessReport();
            REPORT_INTERVAL_MS = yopReportConfig.getIntervalMs();
            REPORT_MIN_INTERVAL_MS = yopReportConfig.getMinIntervalMs();
            STAT_INTERVAL_MS = yopReportConfig.getStatIntervalMs();
            MAX_QUEUE_SIZE = yopReportConfig.getMaxQueueSize();
            MAX_PACKET_SIZE = yopReportConfig.getMaxPacketSize();
            MAX_FAIL_COUNT = yopReportConfig.getMaxFailCount();
            MAX_FAIL_COUNT_PER_EX = yopReportConfig.getMaxFailCountPerEx();
            MAX_ELAPSED_MS = yopReportConfig.getMaxElapsedMs();
        } else {
            REPORT = true;
            REPORT_SUCCESS = false;
            REPORT_INTERVAL_MS = 30000;
            REPORT_MIN_INTERVAL_MS = 2000;
            STAT_INTERVAL_MS = 5000;
            MAX_QUEUE_SIZE = 500;
            MAX_PACKET_SIZE = 50;
            MAX_FAIL_COUNT = 10;
            MAX_FAIL_COUNT_PER_EX = 5;
            MAX_ELAPSED_MS = 15000;
        }
        COLLECT_POOL = new ThreadPoolExecutor(1, 1,
                30, TimeUnit.SECONDS, Queues.newLinkedBlockingQueue(MAX_QUEUE_SIZE),
                new ThreadFactoryBuilder().setNameFormat("client-report-sender-%d").setDaemon(true).build(), new ThreadPoolExecutor.DiscardOldestPolicy());
        SEND_SCHEDULE_POOL.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    sendHostReport();
                } catch (Throwable t) {
                    LOGGER.error("Unexpected Error, ex:", t);
                }
            }
        }, REPORT_INTERVAL_MS, REPORT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public static void reportHostRequest(YopHostRequestEvent<?> newEvent) {
        try {
            if (!REPORT) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignore ReportEvent, value:{}", newEvent);
                }
                return;
            }
            if (!REPORT_SUCCESS && YopStatus.SUCCESS.equals(newEvent.getStatus())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignore Success ReportEvent, value:{}", newEvent);
                }
                return;
            }
            collectEvents(newEvent);
        } catch (Exception exception) {
            LOGGER.error("Error Handle ReportEvent, value:" + newEvent, exception);
        }
    }

    private static void collectEvents(YopHostRequestEvent<?> event) {
        if (null == event) return;
        if (CLOSED) return;
        COLLECT_POOL.submit(new CollectTask(event));
    }

    private static class CollectTask implements Runnable {
        private final YopHostRequestEvent<?> event;

        public CollectTask(YopHostRequestEvent<?> event) {
            this.event = event;
        }

        @Override
        public void run() {
            try {
                final String serverHost = event.getServerHost();
                final String serverIp = event.getServerIp();
                final long elapsedMillis = event.getElapsedMillis();
                int successCount = 0;
                int failCount = 0;
                YopFailureItem failDetail = null;
                if (YopStatus.SUCCESS.equals(event.getStatus())) {
                    successCount = 1;
                } else {
                    failCount = 1;
                    failDetail = (YopFailureItem) event.getData();
                }

                final String reportKey = serverHost + serverIp;
                AtomicReference<YopHostRequestReport> reportReference =
                        YOP_HOST_REQUEST_COLLECTION.computeIfAbsent(reportKey, p -> new AtomicReference<>());

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
                            final YopFailureList yopFailDetail = new YopFailureList(failDetail.getExType(), failDetail.getExMsg());
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
                        final YopFailureItem failDetailItem = failDetail;
                        if (null != failDetail) {
                            final Optional<YopFailureList> yopFailDetail = payload.getFailDetails().stream().filter(p ->
                                            StringUtils.equals(p.getExType(), failDetailItem.getExType())
                                                    && StringUtils.equals(p.getExMsg(), failDetailItem.getExMsg()))
                                    .findAny();
                            if (yopFailDetail.isPresent()) {
                                yopFailDetail.get().getOccurTime().add(failDetailItem.getOccurTime());
                            } else {
                                final YopFailureList newYopFailDetail = new YopFailureList(failDetail.getExType(), failDetail.getExMsg());
                                newYopFailDetail.getOccurTime().add(failDetail.getOccurTime());
                                payload.getFailDetails().add(newYopFailDetail);
                            }
                        }
                    }
                } while (!reportReference.compareAndSet(current, update));
            } catch (Exception e) {
                LOGGER.warn("Error Collect ReportEvent, value:" + event, e);
            }
        }
    }

    private static void sendHostReport() throws InterruptedException {
        Date timestamp = new Date();
        List<YopReport> reports = Lists.newLinkedList();
        final Iterator<Map.Entry<String, AtomicReference<YopHostRequestReport>>> iterator =
                YOP_HOST_REQUEST_COLLECTION.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, AtomicReference<YopHostRequestReport>> entry = iterator.next();
            AtomicReference<YopHostRequestReport> reference = entry.getValue();
            YopHostRequestReport report = reference.get();
            if (null == report) {
                continue;
            }
            // 达到上报条件
            if (needReport(timestamp, report)) {
                report.setEndTime(new Date());
                reports.add(report);
                iterator.remove();
            }
        }
        if (CollectionUtils.isEmpty(reports)) {
            return;
        }
        final List<List<YopReport>> partitions = Lists.partition(reports, MAX_PACKET_SIZE);
        if (partitions.size() == 1) {
            doSendReport(reports);
            return;
        }

        for (int i = 0, total = partitions.size(); i < total; i++) {
            List<YopReport> reportsSending = partitions.get(i);
            doSendReport(reportsSending);
            if (i != total -1) {
                Thread.sleep(REPORT_MIN_INTERVAL_MS);
            }
        }
    }

    private static void doSendReport(List<YopReport> reports) {
        DEFAULT_REPORTER.batchReport(reports);
    }

    private static boolean needReport(Date currentTime, YopHostRequestReport report) {
        final YopHostRequestPayload payload = report.getPayload();
        final Date beginTime = report.getBeginTime();
        final int failCount = payload.getFailCount();
        final long maxElapsedMillis = payload.getMaxElapsedMillis();
        final List<YopFailureList> failDetails = payload.getFailDetails();
        if (currentTime.getTime() - beginTime.getTime() > STAT_INTERVAL_MS) {
            return true;
        }
        if (failCount > MAX_FAIL_COUNT) {
            return true;
        }
        if (maxElapsedMillis > MAX_ELAPSED_MS) {
            return true;
        }
        if (CollectionUtils.isNotEmpty(failDetails)) {
            for (YopFailureList failDetail : failDetails) {
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
        } catch (Exception exception) {
            LOGGER.error("Error When Close ClientReporter", exception);
        }
    }

    /**
     * 开启上报器
     */
    public static void open() {
        try {
            CLOSED = false;
        } catch (Exception exception) {
            LOGGER.error("Error When Open ClientReporter", exception);
        }
    }

}
