/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.g3.sdk.yop.utils.benchmark;

import com.yeepay.g3.core.yop.utils.concurrent.threadpool.ThreadPoolUtil;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2019-04-09 03:13
 */
public abstract class ConcurrentBenchmark {

    public static final String THREAD_COUNT_NAME = "thread.count";
    public static final String TOTAL_COUNT_NAME = "total.count";

    public final int threadCount;
    public final long loopCount;
    public final long totalCount;

    public CountDownLatch startLock;
    public CountDownLatch finishLock;

    public int intervalSeconds = 5;
    public Date startTime;
    private AtomicLong count = new AtomicLong(0);
    private long totalInvokedCount = 0L;
    private ScheduledExecutorService reportExecutor;

    public ConcurrentBenchmark() {
        this(200, 10000);
    }

    public ConcurrentBenchmark(int defaultThreadCount, long defaultTotalCount) {
        // merge default setting and system properties
        this.threadCount = Integer.parseInt(System.getProperty(THREAD_COUNT_NAME, String.valueOf(defaultThreadCount)));
        this.totalCount = Long.parseLong(System.getProperty(TOTAL_COUNT_NAME, String.valueOf(defaultTotalCount)));
        this.loopCount = totalCount / threadCount;

        startLock = new CountDownLatch(threadCount);
        finishLock = new CountDownLatch(threadCount);

        reportExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void execute() throws Exception {
        // override for connection & data setup
        setUp();

        // start threads
        ExecutorService threadPool = Executors.newFixedThreadPool(threadCount, ThreadPoolUtil.buildThreadFactory("ConcurrentBenchmark"));
        try {
            for (int i = 0; i < threadCount; i++) {
                BenchmarkTask task = createTask();
                task.taskSequence = i;
                task.parent = this;
                threadPool.execute(task);
            }

            // wait for all threads ready
            startLock.await();

            // print start message and start reporter
            startReporter();
            startTime = new Date();
            printStartMessage();

            // wait for all threads finish
            finishLock.await();

            // print finish summary message
            printFinishMessage();
        } finally {
            threadPool.shutdownNow();
            reportExecutor.shutdownNow();
            // override for connection & data cleanup
            tearDown();
        }
    }

    private void startReporter() {
        reportExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    printProgressMessage();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    protected void printProgressMessage() {
        long currentCount = count.getAndSet(0);
        totalInvokedCount += currentCount;
        long currentTps = currentCount / intervalSeconds;

        long totalTimeMillis = System.currentTimeMillis() - startTime.getTime();
        long totalTps = (totalInvokedCount * 1000) / totalTimeMillis;

        System.out.printf("Current perior is %,d , tps is %,d, Total is %,d , tps is %,d.%n", currentCount, currentTps,
                totalInvokedCount, totalTps);
    }

    protected void printStartMessage() {
        String className = this.getClass().getSimpleName();

        System.out.printf("%s started at %s.%n%d threads with %,d loops, totally %,d requests will be invoked.%n",
                className, startTime.toString(), threadCount, loopCount, totalCount);
    }

    protected void printFinishMessage() {
        Date endTime = new Date();
        String className = this.getClass().getSimpleName();
        long totalTimeMillis = endTime.getTime() - startTime.getTime();
        long totalTps = (totalCount * 1000) / totalTimeMillis;

        BigDecimal totalLatency = new BigDecimal(totalTimeMillis * threadCount).divide(new BigDecimal(totalCount), 2,
                BigDecimal.ROUND_HALF_UP);

        System.out.printf(
                "%s finished at %s.%n%d threads processed %,d requests after %,d ms, total tps/latency is %,d/%sms.%n",
                className, endTime.toString(), threadCount, totalCount, totalTimeMillis, totalTps,
                totalLatency.toString());
    }

    protected void incCounter() {
        count.incrementAndGet();
    }

    protected void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    /**
     * Override for connection & data setup.
     */
    protected void setUp() {
    }

    /**
     * Override to connection & data cleanup.
     */
    protected void tearDown() {
    }

    /**
     * create a new benchmark task.
     */
    protected abstract BenchmarkTask createTask();

}
