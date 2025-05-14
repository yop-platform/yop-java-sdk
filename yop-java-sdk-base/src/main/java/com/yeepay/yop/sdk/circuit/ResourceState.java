package com.yeepay.yop.sdk.circuit;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Tracks metrics for a resource to make circuit breaker decisions
 */
public class ResourceState {
    
    // Total count of requests
    private final AtomicLong totalCount = new AtomicLong(0);
    
    // Count of blocked requests
    private final AtomicLong blockedCount = new AtomicLong(0);
    
    // Count of exceptions
    private final AtomicLong exceptionCount = new AtomicLong(0);
    
    // Tracks response time metrics
    private final LongAdder totalRt = new LongAdder();
    
    // Last reset time (for sliding window calculations)
    private volatile long lastResetTime = System.currentTimeMillis();
    
    /**
     * Increment the total request count
     * 
     * @return the new total count
     */
    public long incrementAndGetTotal() {
        return totalCount.incrementAndGet();
    }
    
    /**
     * Increment the blocked request count
     * 
     * @return the new blocked count
     */
    public long incrementAndGetBlocked() {
        return blockedCount.incrementAndGet();
    }
    
    /**
     * Increment the exception count
     * 
     * @return the new exception count
     */
    public long incrementAndGetException() {
        return exceptionCount.incrementAndGet();
    }
    
    /**
     * Add a response time value
     * 
     * @param rt response time in milliseconds
     */
    public void addRt(long rt) {
        totalRt.add(rt);
    }
    
    /**
     * Get the current total count
     * 
     * @return the total count
     */
    public long getTotalCount() {
        return totalCount.get();
    }
    
    /**
     * Get the current exception count
     * 
     * @return the exception count
     */
    public long getExceptionCount() {
        return exceptionCount.get();
    }
    
    /**
     * Get the total response time
     * 
     * @return the sum of all response times
     */
    public long getTotalRt() {
        return totalRt.sum();
    }
    
    /**
     * Calculate average response time
     * 
     * @return average RT or 0 if no requests
     */
    public double getAvgRt() {
        long count = getTotalCount();
        return count > 0 ? ((double) getTotalRt()) / count : 0;
    }
    
    /**
     * Calculate the exception ratio
     * 
     * @return ratio between 0.0 and 1.0
     */
    public double getExceptionRatio() {
        long total = getTotalCount();
        return total > 0 ? ((double) getExceptionCount()) / total : 0;
    }
    
    /**
     * Reset all counters and update the reset time
     */
    public void reset() {
        totalCount.set(0);
        blockedCount.set(0);
        exceptionCount.set(0);
        totalRt.reset();
        lastResetTime = System.currentTimeMillis();
    }
    
    /**
     * Get the last reset time
     * 
     * @return timestamp of when this state was last reset
     */
    public long getLastResetTime() {
        return lastResetTime;
    }
} 