/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.base.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.client.metric.YopFailureItem;
import com.yeepay.yop.sdk.client.metric.YopFailureList;
import com.yeepay.yop.sdk.client.metric.YopStatus;
import com.yeepay.yop.sdk.client.metric.event.host.YopHostRequestEvent;
import com.yeepay.yop.sdk.client.metric.report.YopRemoteReporter;
import com.yeepay.yop.sdk.client.metric.report.YopReport;
import com.yeepay.yop.sdk.client.metric.report.YopReporter;
import com.yeepay.yop.sdk.client.metric.report.host.YopHostRequestPayload;
import com.yeepay.yop.sdk.client.metric.report.host.YopHostRequestReport;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.file.YopReportConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.yeepay.yop.sdk.YopConstants.REPORT_API_URI;

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

    private static final YopReporter REMOTE_REPORTER = YopRemoteReporter.INSTANCE;

    private static final ConcurrentMap<String, AtomicReference<YopHostRequestReport>> YOP_HOST_REQUEST_COLLECTION = new ConcurrentHashMap<>();

    private static final Set<String> EXCLUDE_REPORT_RESOURCES = Sets.newHashSet(REPORT_API_URI);

    private static final Deque<YopReport> YOP_HOST_REQUEST_QUEUE;

    // 打包上报事件
    private static final ThreadPoolExecutor COLLECT_POOL;

    private static final int REPORT_INTERVAL_MS;
    private static final int STAT_INTERVAL_MS;
    private static final int MAX_QUEUE_SIZE;
    private static final int MAX_PACKET_SIZE;
    private static final int MAX_FAIL_COUNT;
    private static final int MAX_FAIL_COUNT_PER_EX;
    private static final int MAX_ELAPSED_MS;
    private static final boolean ENABLE_REPORT;
    private static final boolean ENABLE_SUCCESS_REPORT;

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
            ENABLE_REPORT = yopReportConfig.isEnable();
            ENABLE_SUCCESS_REPORT = yopReportConfig.isEnableSuccessReport();
            REPORT_INTERVAL_MS = yopReportConfig.getSendIntervalMs();
            STAT_INTERVAL_MS = yopReportConfig.getStatIntervalMs();
            MAX_QUEUE_SIZE = yopReportConfig.getMaxQueueSize();
            MAX_PACKET_SIZE = yopReportConfig.getMaxPacketSize();
            MAX_FAIL_COUNT = yopReportConfig.getMaxFailCount();
            MAX_FAIL_COUNT_PER_EX = yopReportConfig.getMaxFailCountPerEx();
            MAX_ELAPSED_MS = yopReportConfig.getMaxElapsedMs();
            if (CollectionUtils.isNotEmpty(yopReportConfig.getExcludeResources())) {
                EXCLUDE_REPORT_RESOURCES.addAll(yopReportConfig.getExcludeResources());
            }
        } else {
            ENABLE_REPORT = true;
            ENABLE_SUCCESS_REPORT = false;
            REPORT_INTERVAL_MS = 3000;
            STAT_INTERVAL_MS = 5000;
            MAX_QUEUE_SIZE = 500;
            MAX_PACKET_SIZE = 50;
            MAX_FAIL_COUNT = 10;
            MAX_FAIL_COUNT_PER_EX = 5;
            MAX_ELAPSED_MS = 15000;
        }
        YOP_HOST_REQUEST_QUEUE = new LinkedBlockingDeque<>(MAX_QUEUE_SIZE);
        // 事件接收
        COLLECT_POOL = new ThreadPoolExecutor(1, 1,
                30, TimeUnit.SECONDS, Queues.newLinkedBlockingQueue(MAX_QUEUE_SIZE),
                new ThreadFactoryBuilder().setNameFormat("client-report-event-collector-%d").setDaemon(true).build(), new ThreadPoolExecutor.DiscardOldestPolicy());

        // 定时扫描
        startSweeperThread();

        // 定时发送
        startSenderThread();
    }

    private static void startSenderThread() {
        final Thread reportSendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!CLOSED) {
                    try {
                        sendHostReport();
                    } catch (Throwable t) {
                        LOGGER.error("Unexpected Error, ex:", t);
                    }
                    try {
                        Thread.sleep(REPORT_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                }
            }
        });
        reportSendThread.setName("client-report-sender");
        reportSendThread.setDaemon(true);
        reportSendThread.start();
    }

    private static void startSweeperThread() {
        final Thread reportSweeperThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!CLOSED) {
                    try {
                        sweepReports();
                    } catch (Throwable t) {
                        LOGGER.error("Unexpected Error, ex:", t);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                }
            }
        });
        reportSweeperThread.setName("client-report-sweeper");
        reportSweeperThread.setDaemon(true);
        reportSweeperThread.start();
    }

    private static void sweepReports() {
        final Set<String> collectReports = YOP_HOST_REQUEST_COLLECTION.keySet();
        if (CollectionUtils.isEmpty(collectReports)) {
            return;
        }
        for (String reportKey : collectReports) {
            final AtomicReference<YopHostRequestReport> collectReport = YOP_HOST_REQUEST_COLLECTION.get(reportKey);
            if (null == collectReport) {
                continue;
            }
            checkAndReport(reportKey, collectReport.get());
        }
    }

    private static void checkAndReport(String reportKey, YopHostRequestReport yopHostRequestReport) {
        YopHostRequestReport reportToBeQueue = null;
        if (needReport(new Date(), yopHostRequestReport)) {
            final AtomicReference<YopHostRequestReport> removed = YOP_HOST_REQUEST_COLLECTION.remove(reportKey);
            if (null != removed) {
                reportToBeQueue = removed.get();
            }
        }
        if (null != reportToBeQueue) {
            reportToBeQueue.setEndDate(new Date());

            while (!YOP_HOST_REQUEST_QUEUE.offer(reportToBeQueue)) {
                YopReport oldReport = YOP_HOST_REQUEST_QUEUE.poll();
                if (oldReport != null) {
                    LOGGER.info("Discard Old ReportEvent, value:{}", oldReport);
                }
            }
        }
    }

    public static void reportHostRequest(YopHostRequestEvent<?> newEvent) {
        try {
            if (!ENABLE_REPORT) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignore ReportEvent, value:{}", newEvent);
                }
                return;
            }

            if (!ENABLE_SUCCESS_REPORT && YopStatus.SUCCESS.equals(newEvent.getStatus())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignore Success ReportEvent, value:{}", newEvent);
                }
                return;
            }

            if (StringUtils.isBlank(newEvent.getServerResource()) ||
                    EXCLUDE_REPORT_RESOURCES.contains(newEvent.getServerResource())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignore ReportEvent For Resource Excluded, value:{}", newEvent);
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

                final String reportKey = serverHost + "###" + serverIp;
                AtomicReference<YopHostRequestReport> reportReference =
                        YOP_HOST_REQUEST_COLLECTION.computeIfAbsent(reportKey, p -> new AtomicReference<>());

                YopHostRequestReport current;
                YopHostRequestReport update = new YopHostRequestReport();
                YopHostRequestPayload payload = new YopHostRequestPayload();
                payload.setServerIp(serverIp);
                payload.setServerHost(serverHost);
                update.setPayload(payload);

                // CompareAndSet并发加入统计数据
                do {
                    current = reportReference.get();
                    if (null == current) {
                        payload.setSuccessCount(successCount);
                        payload.setFailCount(failCount);
                        payload.setMaxElapsedMillis(elapsedMillis);
                        payload.setFailDetails(Lists.newLinkedList());
                        if (null != failDetail) {
                            final YopFailureList yopFailDetail = new YopFailureList(failDetail.getExType(), failDetail.getExMsg());
                            yopFailDetail.getOccurDate().add(failDetail.getOccurDate());
                            payload.getFailDetails().add(yopFailDetail);
                        }
                    } else {
                        update.setBeginDate(current.getBeginDate());
                        YopHostRequestPayload oldPayload = current.getPayload();
                        payload.setSuccessCount(oldPayload.getSuccessCount() + successCount);
                        payload.setFailCount(oldPayload.getFailCount() + failCount);
                        payload.setMaxElapsedMillis(Math.max(elapsedMillis, oldPayload.getMaxElapsedMillis()));
                        payload.setFailDetails(oldPayload.cloneFailDetails());
                        final YopFailureItem failDetailItem = failDetail;
                        if (null != failDetail) {
                            final Optional<YopFailureList> yopFailDetail = payload.getFailDetails().stream().filter(p ->
                                            StringUtils.equals(p.getExType(), failDetailItem.getExType())
                                                    && StringUtils.equals(p.getExMsg(), failDetailItem.getExMsg()))
                                    .findAny();
                            if (yopFailDetail.isPresent()) {
                                yopFailDetail.get().getOccurDate().add(failDetailItem.getOccurDate());
                            } else {
                                final YopFailureList newYopFailDetail = new YopFailureList(failDetail.getExType(), failDetail.getExMsg());
                                newYopFailDetail.getOccurDate().add(failDetail.getOccurDate());
                                payload.getFailDetails().add(newYopFailDetail);
                            }
                        }
                    }
                } while (!reportReference.compareAndSet(current, update));

                checkAndReport(reportKey, reportReference.get());
            } catch (Exception e) {
                LOGGER.warn("Error Collect ReportEvent, value:" + event, e);
            }
        }
    }

    private static void sendHostReport() throws InterruptedException {
        List<YopReport> reports = Lists.newLinkedList();
        int packetSize = 0;
        YopReport report;
        while ((packetSize++ < MAX_PACKET_SIZE) && null != (report = YOP_HOST_REQUEST_QUEUE.poll())) {
            reports.add(report);
        }
        if (CollectionUtils.isEmpty(reports)) {
            return;
        }
        sendWithRetry(reports);
    }

    private static void sendWithRetry(List<YopReport> reports) {
        try {
            REMOTE_REPORTER.batchReport(reports);
        } catch (Exception ex) {
            LOGGER.warn("Remote Report Fail, exType:{}, exMsg:{}, But Will Retry.", ex.getClass().getCanonicalName(),
                    ExceptionUtils.getMessage(ex));
            tryEnqueue(reports);
        }
    }

    private static void tryEnqueue(List<YopReport> reports) {
        if (CollectionUtils.isEmpty(reports)) {
            return;
        }
        for (int i = reports.size() - 1; i >= 0; i--) {
            try {
                YOP_HOST_REQUEST_QUEUE.push(reports.get(i));
            } catch (Exception ex) {
                LOGGER.warn("Report ReEnqueue Fail, exType:{}, exMsg:{}, ", ex.getClass().getCanonicalName(),
                        ExceptionUtils.getMessage(ex));
            }
        }
    }

    private static boolean needReport(Date currentTime, YopHostRequestReport report) {
        if (null == report) {
            return false;
        }
        final YopHostRequestPayload payload = report.getPayload();
        final Date beginTime = report.getBeginDate();
        final int failCount = payload.getFailCount();
        final long maxElapsedMillis = payload.getMaxElapsedMillis();
        final List<YopFailureList> failDetails = payload.getFailDetails();
        if (currentTime.getTime() - beginTime.getTime() >= STAT_INTERVAL_MS) {
            return true;
        }
        if (failCount >= MAX_FAIL_COUNT) {
            return true;
        }
        if (maxElapsedMillis >= MAX_ELAPSED_MS) {
            return true;
        }
        if (CollectionUtils.isNotEmpty(failDetails)) {
            for (YopFailureList failDetail : failDetails) {
                if (CollectionUtils.size(failDetail.getOccurDate()) >= MAX_FAIL_COUNT_PER_EX) {
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
