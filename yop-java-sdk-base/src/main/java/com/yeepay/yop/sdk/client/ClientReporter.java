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
import com.yeepay.yop.sdk.constants.CharacterConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.yeepay.yop.sdk.YopConstants.REPORT_API_URI;
import static com.yeepay.yop.sdk.config.provider.file.YopReportConfig.DEFAULT_YOP_REPORT_CONFIG;

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
    private static final Map<String, Map<String, AtomicReference<YopHostRequestReport>>>
            YOP_HOST_REQUEST_COLLECTION_MAP = new ConcurrentHashMap<>();

    private static final Map<String, Thread> DAEMON_THREADS = new ConcurrentHashMap<>();

    private static final Set<String> EXCLUDE_REPORT_RESOURCES = Sets.newHashSet(REPORT_API_URI);

    private static final Map<String, Deque<YopReport>> YOP_HOST_REQUEST_QUEUE_MAP = new ConcurrentHashMap<>();

    // 打包上报事件
    private static final ThreadPoolExecutor COLLECT_POOL;

    private static volatile boolean CLOSED = false;

    private ClientReporter() {
    }

    static {
        // 事件接收
        COLLECT_POOL = new ThreadPoolExecutor(1, 1,
                30, TimeUnit.SECONDS, Queues.newLinkedBlockingQueue(500),
                new ThreadFactoryBuilder().setNameFormat("client-report-event-collector-%d").setDaemon(true).build(),
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    private static YopReportConfig getReportConfig(String provider, String env) {
        final YopSdkConfig sdkConfig = YopSdkConfigProviderRegistry.getProvider().getConfig(provider, env);
        if (null != sdkConfig) {
            final YopReportConfig yopReportConfig = sdkConfig.getYopReportConfig();
            if (null != yopReportConfig) {
                return yopReportConfig;
            }
        }
        return DEFAULT_YOP_REPORT_CONFIG;
    }

    private static void startSenderThread(String provider, String env) {
        final String threadName = getThreadName("client-report-sender", provider, env);
        DAEMON_THREADS.computeIfAbsent(threadName, p -> {
            final Thread reportSendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!CLOSED) {
                        try {
                            sendHostReport(provider, env);
                        } catch (Throwable t) {
                            LOGGER.error("Unexpected Error, ex:", t);
                        }
                        try {
                            Thread.sleep(getReportConfig(provider, env).getSendIntervalMs());
                        } catch (InterruptedException e) {
                            //ignore
                        }
                    }
                }
            });
            reportSendThread.setName(threadName);
            reportSendThread.setDaemon(true);
            reportSendThread.start();
            return reportSendThread;
        });
    }

    private static String getThreadName(String prefix, String provider, String env) {
        return prefix + (StringUtils.isNotBlank(provider) ? ("-" + provider) : "")
                + (StringUtils.isNotBlank(env) ? ("-" + env) : "");
    }


    private static void startSweeperThread(String provider, String env) {
        final String threadName = getThreadName("client-report-sweeper", provider, env);
        DAEMON_THREADS.computeIfAbsent(threadName, p -> {
            final Thread reportSweeperThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!CLOSED) {
                        try {
                            sweepReports(provider, env);
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
            reportSweeperThread.setName(threadName);
            reportSweeperThread.setDaemon(true);
            reportSweeperThread.start();
            return reportSweeperThread;
        });
    }

    private static void sweepReports(String provider, String env) {
        final Map<String, AtomicReference<YopHostRequestReport>> requestCollections =
                YOP_HOST_REQUEST_COLLECTION_MAP.computeIfAbsent(getMapKey(provider, env), p -> new ConcurrentHashMap<>());
        final Set<String> collectReports = requestCollections.keySet();
        if (CollectionUtils.isEmpty(collectReports)) {
            return;
        }
        for (String reportKey : collectReports) {
            final AtomicReference<YopHostRequestReport> collectReport = requestCollections.get(reportKey);
            if (null == collectReport) {
                continue;
            }
            checkAndReport(reportKey, collectReport.get(), requestCollections);
        }
    }

    private static Map<String, AtomicReference<YopHostRequestReport>> currentReportCollection(String provider, String env) {
        return YOP_HOST_REQUEST_COLLECTION_MAP.computeIfAbsent(getMapKey(provider, env), p -> new ConcurrentHashMap<>());
    }

    private static String getMapKey(String provider, String env) {
        return provider + CharacterConstants.COLON + env;
    }

    private static void checkAndReport(String reportKey, YopHostRequestReport yopHostRequestReport,
                                       Map<String, AtomicReference<YopHostRequestReport>> requestCollections) {
        YopHostRequestReport reportToBeQueue = null;
        if (needReport(new Date(), yopHostRequestReport)) {
            final AtomicReference<YopHostRequestReport> removed = requestCollections.remove(reportKey);
            if (null != removed) {
                reportToBeQueue = removed.get();
            }
        }
        if (null != reportToBeQueue) {
            reportToBeQueue.setEndDate(new Date());
            syncReportToQueue(reportToBeQueue);
        }
    }

    public static void syncReportToQueue(YopReport report) {
        if (shouldIgnoreTheReport(report)) {
            return;
        }
        final Deque<YopReport> yopReportsQueue = currentReportQueue(report.getProvider(), report.getEnv());
        while (!yopReportsQueue.offer(report)) {
            YopReport oldReport = yopReportsQueue.poll();
            if (oldReport != null) {
                LOGGER.info("Discard Old ReportEvent, value:{}", oldReport);
            }
        }
    }

    private static Deque<YopReport> currentReportQueue(String provider, String env) {
        return YOP_HOST_REQUEST_QUEUE_MAP.computeIfAbsent(getMapKey(provider, env),
                p -> new LinkedBlockingDeque<>(getReportConfig(provider, env).getMaxQueueSize()));
    }


    public static void asyncReportToQueue(YopReport report) {
        if (shouldIgnoreTheReport(report)) {
            return;
        }
        COLLECT_POOL.submit(() -> syncReportToQueue(report));
    }

    private static boolean shouldIgnoreTheReport(YopReport report) {
        if (CLOSED || null == report || !getReportConfig(report.getProvider(), report.getEnv()).isEnable()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ignore ReportEvent, value:{}", report);
            }
            return true;
        }
        return false;
    }

    public static void asyncReportToQueue(YopReport report, ThreadPoolExecutor executor) {
        if (shouldIgnoreTheReport(report)) {
            return;
        }
        executor.submit(() -> syncReportToQueue(report));
    }

    public static void reportHostRequest(YopHostRequestEvent<?> newEvent) {
        try {
            if (CLOSED || null == newEvent) {
                return;
            }
            final YopReportConfig reportConfig = getReportConfig(newEvent.getProvider(), newEvent.getEnv());
            if (!reportConfig.isEnable()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignore ReportEvent, value:{}", newEvent);
                }
                return;
            }

            if (!reportConfig.isEnableSuccessReport() && YopStatus.SUCCESS.equals(newEvent.getStatus())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignore Success ReportEvent, value:{}", newEvent);
                }
                return;
            }

            if (StringUtils.isBlank(newEvent.getServerResource())
                    || EXCLUDE_REPORT_RESOURCES.contains(newEvent.getServerResource())
                    || (null != reportConfig.getExcludeResources()
                        && reportConfig.getExcludeResources().contains(newEvent.getServerResource()))) {
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
        final String provider = event.getProvider(),
                env = event.getEnv();
        startSweeperThread(provider, env);
        startSenderThread(provider, env);
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
                final String provider = event.getProvider();
                final String env = event.getEnv();
                final String appKey = event.getAppKey();
                final String serverHost = event.getServerHost();
                final String serverIp = event.getServerIp();
                final long elapsedMillis = event.getElapsedMillis();
                int successCount = 0;
                int retrySuccessCount = 0;
                int failCount = 0;
                YopFailureItem failDetail = null;
                if (YopStatus.SUCCESS.equals(event.getStatus())) {
                    successCount = 1;
                    if (event.isRetry()) {
                        retrySuccessCount = 1;
                    }
                } else {
                    failCount = 1;
                    failDetail = (YopFailureItem) event.getData();
                }
                final Map<String, AtomicReference<YopHostRequestReport>> currentReportCollection = currentReportCollection(provider, env);
                final String reportKey = StringUtils.joinWith(CharacterConstants.COLON, appKey, serverHost, serverIp);
                AtomicReference<YopHostRequestReport> reportReference =
                        currentReportCollection.computeIfAbsent(reportKey, p -> new AtomicReference<>());

                YopHostRequestReport current;
                YopHostRequestReport update = new YopHostRequestReport();
                update.setProvider(provider);
                update.setEnv(env);
                YopHostRequestPayload payload = new YopHostRequestPayload();
                payload.setAppKey(appKey);
                payload.setServerIp(serverIp);
                payload.setServerHost(serverHost);
                update.setPayload(payload);

                // CompareAndSet并发加入统计数据
                do {
                    current = reportReference.get();
                    if (null == current) {
                        payload.setSuccessCount(successCount);
                        payload.setRetrySuccessCount(retrySuccessCount);
                        payload.setFailCount(failCount);
                        payload.setMinElapsedMillis(elapsedMillis);
                        payload.setMaxElapsedMillis(elapsedMillis);
                        payload.setAvgElapsedMillis(elapsedMillis);
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
                        payload.setRetrySuccessCount(oldPayload.getRetrySuccessCount() + retrySuccessCount);
                        payload.setFailCount(oldPayload.getFailCount() + failCount);
                        payload.setMinElapsedMillis(Math.min(elapsedMillis, oldPayload.getMinElapsedMillis()));
                        payload.setMaxElapsedMillis(Math.max(elapsedMillis, oldPayload.getMaxElapsedMillis()));
                        payload.setAvgElapsedMillis(((oldPayload.getAvgElapsedMillis() * oldPayload.getTotalCount()) + elapsedMillis) / (oldPayload.getTotalCount() + 1));
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

                checkAndReport(reportKey, reportReference.get(), currentReportCollection);
            } catch (Exception e) {
                LOGGER.warn("Error Collect ReportEvent, value:" + event, e);
            }
        }
    }

    private static void sendHostReport(String provider, String env) throws InterruptedException {
        final Deque<YopReport> yopReportsQueue = currentReportQueue(provider, env);
        List<YopReport> reports = new LinkedList<>();
        YopReport report;
        while (null != (report = yopReportsQueue.poll())) {
            reports.add(report);
            if (reports.size() >= getReportConfig(provider, env).getMaxPacketSize()) {
                sendWithRetry(provider, env, reports);
                reports = new LinkedList<>();
            }
        }
        if (CollectionUtils.isNotEmpty(reports)) {
            sendWithRetry(provider, env, reports);
        }
    }

    private static void sendWithRetry(String provider, String env, List<YopReport> reports) {
        try {
            REMOTE_REPORTER.batchReport(provider, env, reports);
        } catch (Exception ex) {
            LOGGER.warn("Remote Report Fail, exType:{}, exMsg:{}, But Will Retry.", ex.getClass().getCanonicalName(),
                    StringUtils.defaultString(ex.getMessage()));
            tryEnqueue(provider, env, reports);
        }
    }

    private static void tryEnqueue(String provider, String env, List<YopReport> reports) {
        if (CollectionUtils.isEmpty(reports)) {
            return;
        }
        for (int i = reports.size() - 1; i >= 0; i--) {
            try {
                currentReportQueue(provider, env).push(reports.get(i));
            } catch (Exception ex) {
                LOGGER.warn("Report ReEnqueue Fail, exType:{}, exMsg:{}, ", ex.getClass().getCanonicalName(),
                        StringUtils.defaultString(ex.getMessage()));
            }
        }
    }

    private static boolean needReport(Date currentTime, YopHostRequestReport report) {
        if (null == report) {
            return false;
        }
        final YopReportConfig reportConfig = getReportConfig(report.getProvider(), report.getEnv());
        final YopHostRequestPayload payload = report.getPayload();
        final Date beginTime = report.getBeginDate();
        final int failCount = payload.getFailCount();
        final long maxElapsedMillis = payload.getMaxElapsedMillis();
        final List<YopFailureList> failDetails = payload.getFailDetails();
        if (currentTime.getTime() - beginTime.getTime() >= reportConfig.getStatIntervalMs()) {
            return true;
        }
        if (failCount >= reportConfig.getMaxFailCount()) {
            return true;
        }
        if (maxElapsedMillis >= reportConfig.getMaxElapsedMs()) {
            return true;
        }
        if (CollectionUtils.isNotEmpty(failDetails)) {
            for (YopFailureList failDetail : failDetails) {
                if (CollectionUtils.size(failDetail.getOccurDate()) >= reportConfig.getMaxFailCountPerEx()) {
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
