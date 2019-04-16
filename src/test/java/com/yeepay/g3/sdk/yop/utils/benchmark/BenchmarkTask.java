/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.g3.sdk.yop.utils.benchmark;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2019-04-09 03:14
 */
public abstract class BenchmarkTask implements Runnable {

    protected int taskSequence;
    protected ConcurrentBenchmark parent;

    @Override
    public void run() {
        setUp();
        onThreadStart();
        try {
            for (int i = 1; i <= parent.loopCount; i++) {
                execute(i);
                parent.incCounter();
            }
        } finally {
            tearDown();
            onThreadFinish();
        }
    }

    abstract protected void execute(final int requestSequence);

    /**
     * Must be invoked when each thread after the setup().
     */
    protected void onThreadStart() {
        parent.startLock.countDown();
        // wait for all other threads ready
        try {
            parent.startLock.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Must be invoked when each thread finish loop, before the tearDown().
     */
    protected void onThreadFinish() {
        // notify test finish
        parent.finishLock.countDown();
    }

    /**
     * Override for thread local connection and data setup.
     */
    protected void setUp() {
    }

    /**
     * Override for thread local connection and data cleanup.
     */
    protected void tearDown() {
    }
}
