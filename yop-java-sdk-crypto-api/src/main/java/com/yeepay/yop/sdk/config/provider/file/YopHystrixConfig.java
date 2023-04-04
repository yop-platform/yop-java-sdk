/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.config.provider.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Set;

/**
 * title: 熔断配置<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/28
 */
public class YopHystrixConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    public static final YopHystrixConfig DEFAULT_CONFIG = new YopHystrixConfig();

    // 命令、分组、线程池
    /**
     * 命令分组
     */
    @JsonProperty("group_key")
    private String groupKey = "YOP_SERVER_ROOT";

    /**
     * 线程池
     */
    @JsonProperty("thread_pool_key")
    private String threadPoolKey = "YopClientExecutePool";

    // region 执行控制
    /**
     * 隔离策略：线程(与调用线程隔离)
     */
    @JsonProperty("execution_isolation_strategy")
    private String executionIsolationStrategy = "SEMAPHORE";

    /**
     * 启用超时熔断
     */
    @JsonProperty("execution_timeout_enabled")
    private boolean executionTimeoutEnabled = false;

    /**
     * 线程超时时长(毫秒)
     * 默认1000
     */
    @JsonProperty("execution_isolation_thread_timeout_in_milliseconds")
    private int executionIsolationThreadTimeoutInMilliseconds = 60000;// 总开关关闭后，该选项是否还有效

    /**
     * 是否允许超时中断
     */
    @JsonProperty("execution_isolation_thread_interrupt_on_timeout")
    private boolean executionIsolationThreadInterruptOnTimeout = false;// hystrix不做超时控制，交给httpClient

    /**
     * 是否允许取消中断
     */
    @JsonProperty("execution_isolation_thread_interrupt_on_cancel")
    private boolean executionIsolationThreadInterruptOnCancel = false;

    /**
     * 最大并发（针对隔离策略：SEMAPHORE）
     * 默认10
     */
    @JsonProperty("execution_isolation_semaphore_max_concurrent_requests")
    private int executionIsolationSemaphoreMaxConcurrentRequests = 2000;
    // endregion

    // region 断路器

    /**
     * 启用断路器
     */
    @JsonProperty("circuit_breaker_enabled")
    private boolean circuitBreakerEnabled = true;

    /**
     * 请求量阈值(达到该数量，才会检查健康数据，进而熔断)
     * 默认20
     */
    @JsonProperty("circuit_breaker_request_volume_threshold")
    private int circuitBreakerRequestVolumeThreshold = 10;

    /**
     * 错误率阈值(达到该值，进入熔断)
     */
    @JsonProperty("circuit_breaker_error_threshold_percentage")
    private int circuitBreakerErrorThresholdPercentage = 50;

    /**
     * 熔断时长(毫秒，该窗口期后，会进入半开)
     */
    @JsonProperty("circuit_breaker_sleep_window_in_milliseconds")
    private int circuitBreakerSleepWindowInMilliseconds = 5000;

    /**
     * 强制打开断路器
     */
    @JsonProperty("circuit_breaker_force_open")
    private boolean circuitBreakerForceOpen = false;

    /**
     * 强制关闭断路器
     */
    @JsonProperty("circuit_breaker_force_closed")
    private boolean circuitBreakerForceClosed = false;
    // endregion

    // region 断路器监控

    /**
     * 滑动窗口时长(毫秒)
     */
    @JsonProperty("cb_metrics_rolling_stats_time_in_milliseconds")
    private int cbMetricsRollingStatsTimeInMilliseconds = 10000;

    /**
     * 滑动窗口分桶
     */
    @JsonProperty("cb_metrics_rolling_stats_num_buckets")
    private int cbMetricsRollingStatsNumBuckets = 10;

    /**
     * 启用percentile
     */
    @JsonProperty("cb_metrics_rolling_percentile_enabled")
    private boolean cbMetricsRollingPercentileEnabled = false;

    /**
     * percentile窗口时长(毫秒)
     */
    @JsonProperty("cb_metrics_rolling_percentile_time_in_milliseconds")
    private int cbMetricsRollingPercentileTimeInMilliseconds = 60000;

    /**
     * percentile窗口分桶
     */
    @JsonProperty("cb_metrics_rolling_percentile_num_buckets")
    private int cbMetricsRollingPercentileNumBuckets = 6;

    /**
     * percentile桶容量最大值(注：超过将丢弃，值越大越耗费资源)
     */
    @JsonProperty("cb_metrics_rolling_percentile_bucket_size")
    private int cbMetricsRollingPercentileBucketSize = 100;

    /**
     * 健康快照窗口时长(毫秒)
     */
    @JsonProperty("cb_metrics_health_snapshot_interval_in_milliseconds")
    private int cbMetricsHealthSnapshotIntervalInMilliseconds = 500;
    // endregion

    // region 线程池参数
    @JsonProperty("core_size")
    private int coreSize = 10;
    // 默认 10
    @JsonProperty("maximum_size")
    private int maximumSize = 50;

    // 默认不排队-1
    @JsonProperty("max_queue_size")
    private int maxQueueSize = 50;

    /**
     * maxQueueSize > 0 时生效
     * 场景：maxQueueSize设定后，可以通过该参数动态模拟拒绝门槛
     * 默认5
     */
    @JsonProperty("queue_size_rejection_threshold")
    private int queueSizeRejectionThreshold = 50;

    /**
     * 线程存活时长(分钟)
     * 默认1分钟
     */
    @JsonProperty("keep_alive_time_minutes")
    private int keepAliveTimeMinutes = 1;

    /**
     * max线程数生效开关
     * 默认false
     */
    @JsonProperty("allow_maximum_size_to_diverge_from_core_size")
    private boolean allowMaximumSizeToDivergeFromCoreSize = true;

    /**
     * 线程池监控窗口时长(毫秒)
     */
    @JsonProperty("tp_metrics_rolling_stats_time_in_milliseconds")
    private int tpMetricsRollingStatsTimeInMilliseconds = 10000;

    /**
     * 线程池窗口分桶数量
     */
    @JsonProperty("tp_metrics_rolling_stats_num_buckets")
    private int tpMetricsRollingStatsNumBuckets = 10;
    // endregion

    // region yop扩展
    /**
     * 非短路异常
     */
    @JsonProperty("yop_exclude_exceptions")
    private Set<String> excludeExceptions = Sets.newHashSet("java.net.SocketTimeoutException:Read timed out");
    // endRegion

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getThreadPoolKey() {
        return threadPoolKey;
    }

    public void setThreadPoolKey(String threadPoolKey) {
        this.threadPoolKey = threadPoolKey;
    }

    public String getExecutionIsolationStrategy() {
        return executionIsolationStrategy;
    }

    public void setExecutionIsolationStrategy(String executionIsolationStrategy) {
        this.executionIsolationStrategy = executionIsolationStrategy;
    }

    public boolean isExecutionTimeoutEnabled() {
        return executionTimeoutEnabled;
    }

    public void setExecutionTimeoutEnabled(boolean executionTimeoutEnabled) {
        this.executionTimeoutEnabled = executionTimeoutEnabled;
    }

    public int getExecutionIsolationThreadTimeoutInMilliseconds() {
        return executionIsolationThreadTimeoutInMilliseconds;
    }

    public void setExecutionIsolationThreadTimeoutInMilliseconds(int executionIsolationThreadTimeoutInMilliseconds) {
        this.executionIsolationThreadTimeoutInMilliseconds = executionIsolationThreadTimeoutInMilliseconds;
    }

    public boolean isExecutionIsolationThreadInterruptOnTimeout() {
        return executionIsolationThreadInterruptOnTimeout;
    }

    public void setExecutionIsolationThreadInterruptOnTimeout(boolean executionIsolationThreadInterruptOnTimeout) {
        this.executionIsolationThreadInterruptOnTimeout = executionIsolationThreadInterruptOnTimeout;
    }

    public boolean isExecutionIsolationThreadInterruptOnCancel() {
        return executionIsolationThreadInterruptOnCancel;
    }

    public void setExecutionIsolationThreadInterruptOnCancel(boolean executionIsolationThreadInterruptOnCancel) {
        this.executionIsolationThreadInterruptOnCancel = executionIsolationThreadInterruptOnCancel;
    }

    public int getExecutionIsolationSemaphoreMaxConcurrentRequests() {
        return executionIsolationSemaphoreMaxConcurrentRequests;
    }

    public void setExecutionIsolationSemaphoreMaxConcurrentRequests(int executionIsolationSemaphoreMaxConcurrentRequests) {
        this.executionIsolationSemaphoreMaxConcurrentRequests = executionIsolationSemaphoreMaxConcurrentRequests;
    }

    public boolean isCircuitBreakerEnabled() {
        return circuitBreakerEnabled;
    }

    public void setCircuitBreakerEnabled(boolean circuitBreakerEnabled) {
        this.circuitBreakerEnabled = circuitBreakerEnabled;
    }

    public int getCircuitBreakerRequestVolumeThreshold() {
        return circuitBreakerRequestVolumeThreshold;
    }

    public void setCircuitBreakerRequestVolumeThreshold(int circuitBreakerRequestVolumeThreshold) {
        this.circuitBreakerRequestVolumeThreshold = circuitBreakerRequestVolumeThreshold;
    }

    public int getCircuitBreakerErrorThresholdPercentage() {
        return circuitBreakerErrorThresholdPercentage;
    }

    public void setCircuitBreakerErrorThresholdPercentage(int circuitBreakerErrorThresholdPercentage) {
        this.circuitBreakerErrorThresholdPercentage = circuitBreakerErrorThresholdPercentage;
    }

    public int getCircuitBreakerSleepWindowInMilliseconds() {
        return circuitBreakerSleepWindowInMilliseconds;
    }

    public void setCircuitBreakerSleepWindowInMilliseconds(int circuitBreakerSleepWindowInMilliseconds) {
        this.circuitBreakerSleepWindowInMilliseconds = circuitBreakerSleepWindowInMilliseconds;
    }

    public boolean isCircuitBreakerForceOpen() {
        return circuitBreakerForceOpen;
    }

    public void setCircuitBreakerForceOpen(boolean circuitBreakerForceOpen) {
        this.circuitBreakerForceOpen = circuitBreakerForceOpen;
    }

    public boolean isCircuitBreakerForceClosed() {
        return circuitBreakerForceClosed;
    }

    public void setCircuitBreakerForceClosed(boolean circuitBreakerForceClosed) {
        this.circuitBreakerForceClosed = circuitBreakerForceClosed;
    }

    public int getCbMetricsRollingStatsTimeInMilliseconds() {
        return cbMetricsRollingStatsTimeInMilliseconds;
    }

    public void setCbMetricsRollingStatsTimeInMilliseconds(int cbMetricsRollingStatsTimeInMilliseconds) {
        this.cbMetricsRollingStatsTimeInMilliseconds = cbMetricsRollingStatsTimeInMilliseconds;
    }

    public int getCbMetricsRollingStatsNumBuckets() {
        return cbMetricsRollingStatsNumBuckets;
    }

    public void setCbMetricsRollingStatsNumBuckets(int cbMetricsRollingStatsNumBuckets) {
        this.cbMetricsRollingStatsNumBuckets = cbMetricsRollingStatsNumBuckets;
    }

    public boolean isCbMetricsRollingPercentileEnabled() {
        return cbMetricsRollingPercentileEnabled;
    }

    public void setCbMetricsRollingPercentileEnabled(boolean cbMetricsRollingPercentileEnabled) {
        this.cbMetricsRollingPercentileEnabled = cbMetricsRollingPercentileEnabled;
    }

    public int getCbMetricsRollingPercentileTimeInMilliseconds() {
        return cbMetricsRollingPercentileTimeInMilliseconds;
    }

    public void setCbMetricsRollingPercentileTimeInMilliseconds(int cbMetricsRollingPercentileTimeInMilliseconds) {
        this.cbMetricsRollingPercentileTimeInMilliseconds = cbMetricsRollingPercentileTimeInMilliseconds;
    }

    public int getCbMetricsRollingPercentileNumBuckets() {
        return cbMetricsRollingPercentileNumBuckets;
    }

    public void setCbMetricsRollingPercentileNumBuckets(int cbMetricsRollingPercentileNumBuckets) {
        this.cbMetricsRollingPercentileNumBuckets = cbMetricsRollingPercentileNumBuckets;
    }

    public int getCbMetricsRollingPercentileBucketSize() {
        return cbMetricsRollingPercentileBucketSize;
    }

    public void setCbMetricsRollingPercentileBucketSize(int cbMetricsRollingPercentileBucketSize) {
        this.cbMetricsRollingPercentileBucketSize = cbMetricsRollingPercentileBucketSize;
    }

    public int getCbMetricsHealthSnapshotIntervalInMilliseconds() {
        return cbMetricsHealthSnapshotIntervalInMilliseconds;
    }

    public void setCbMetricsHealthSnapshotIntervalInMilliseconds(int cbMetricsHealthSnapshotIntervalInMilliseconds) {
        this.cbMetricsHealthSnapshotIntervalInMilliseconds = cbMetricsHealthSnapshotIntervalInMilliseconds;
    }

    public int getCoreSize() {
        return coreSize;
    }

    public void setCoreSize(int coreSize) {
        this.coreSize = coreSize;
    }

    public int getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public int getQueueSizeRejectionThreshold() {
        return queueSizeRejectionThreshold;
    }

    public void setQueueSizeRejectionThreshold(int queueSizeRejectionThreshold) {
        this.queueSizeRejectionThreshold = queueSizeRejectionThreshold;
    }

    public int getKeepAliveTimeMinutes() {
        return keepAliveTimeMinutes;
    }

    public void setKeepAliveTimeMinutes(int keepAliveTimeMinutes) {
        this.keepAliveTimeMinutes = keepAliveTimeMinutes;
    }

    public boolean isAllowMaximumSizeToDivergeFromCoreSize() {
        return allowMaximumSizeToDivergeFromCoreSize;
    }

    public void setAllowMaximumSizeToDivergeFromCoreSize(boolean allowMaximumSizeToDivergeFromCoreSize) {
        this.allowMaximumSizeToDivergeFromCoreSize = allowMaximumSizeToDivergeFromCoreSize;
    }

    public int getTpMetricsRollingStatsTimeInMilliseconds() {
        return tpMetricsRollingStatsTimeInMilliseconds;
    }

    public void setTpMetricsRollingStatsTimeInMilliseconds(int tpMetricsRollingStatsTimeInMilliseconds) {
        this.tpMetricsRollingStatsTimeInMilliseconds = tpMetricsRollingStatsTimeInMilliseconds;
    }

    public int getTpMetricsRollingStatsNumBuckets() {
        return tpMetricsRollingStatsNumBuckets;
    }

    public void setTpMetricsRollingStatsNumBuckets(int tpMetricsRollingStatsNumBuckets) {
        this.tpMetricsRollingStatsNumBuckets = tpMetricsRollingStatsNumBuckets;
    }

    public Set<String> getExcludeExceptions() {
        return excludeExceptions;
    }

    public void setExcludeExceptions(Set<String> excludeExceptions) {
        if (null != excludeExceptions && excludeExceptions.size() > 0) {
            this.excludeExceptions = excludeExceptions;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
