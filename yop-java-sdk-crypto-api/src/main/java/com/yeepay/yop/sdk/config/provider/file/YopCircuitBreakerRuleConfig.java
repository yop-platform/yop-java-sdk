/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.config.provider.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * title: 熔断规则配置<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/28
 */
public class YopCircuitBreakerRuleConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    public static final YopCircuitBreakerRuleConfig DEFAULT_CONFIG = new YopCircuitBreakerRuleConfig();
    public static final YopCircuitBreakerRuleConfig DEFAULT_ERROR_COUNT_CONFIG;
    public static final YopCircuitBreakerRuleConfig DEFAULT_ERROR_RATIO_CONFIG;

    static {
        DEFAULT_ERROR_COUNT_CONFIG = new YopCircuitBreakerRuleConfig(5 * 60 * 1000);
        DEFAULT_ERROR_COUNT_CONFIG.setGrade(2);
        DEFAULT_ERROR_COUNT_CONFIG.setCount(1.0);
        DEFAULT_ERROR_COUNT_CONFIG.setTimeWindow(15 * 60);
        DEFAULT_ERROR_RATIO_CONFIG = new YopCircuitBreakerRuleConfig(5 * 1000);
        DEFAULT_ERROR_RATIO_CONFIG.setGrade(1);
        DEFAULT_ERROR_RATIO_CONFIG.setCount(0.2);
        DEFAULT_ERROR_RATIO_CONFIG.setTimeWindow(15 * 60);
        DEFAULT_ERROR_RATIO_CONFIG.setMinRequestAmount(5);
    }

    public YopCircuitBreakerRuleConfig() {
    }

    public YopCircuitBreakerRuleConfig(int statIntervalMs) {
        this.statIntervalMs = statIntervalMs;
    }


    // region 断路器

    /**
     * 降级策略：Circuit breaking strategy (0: average RT, 1: exception ratio, 2: exception count
     */
    @JsonProperty("grade")
    private int grade = 2;


    /**
     * Threshold count. The exact meaning depends on the field of grade.
     * <ul>
     *     <li>In average RT mode, it means the maximum response time(RT) in milliseconds.</li>
     *     <li>In exception ratio mode, it means exception ratio which between 0.0 and 1.0.</li>
     *     <li>In exception count mode, it means exception count</li>
     * <ul/>
     */
    @JsonProperty("count")
    private double count = 2.0;

    /**
     * 熔断间歇时长(秒，该窗口期后，会进入半开)
     * Recovery timeout (in seconds) when circuit breaker opens. After the timeout, the circuit breaker will
     * transform to half-open state for trying a few requests.
     */
    @JsonProperty("time_window")
    private int timeWindow = 5;

    /**
     * 请求量阈值(达到该数量，才会检查健康数据，进而熔断)
     * Minimum number of requests (in an active statistic time span) that can trigger circuit breaking.
     *
     * @since 1.7.0
     */
    @JsonProperty("min_request_amount")
    private int minRequestAmount = 5;

    /**
     * 慢请求比例阈值
     * The threshold of slow request ratio in RT mode.
     *
     * @since 1.8.0
     */
    @JsonProperty("slow_ratio_threshold")
    private double slowRatioThreshold = 1.0d;

    /**
     * 统计窗口时长(毫秒，失败率会在该窗口内汇总计算)
     * The interval statistics duration in millisecond.
     *
     * @since 1.8.0
     */
    @JsonProperty("stat_interval_ms")
    private int statIntervalMs = 1000;

    // endregion


    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public int getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
    }

    public int getMinRequestAmount() {
        return minRequestAmount;
    }

    public void setMinRequestAmount(int minRequestAmount) {
        this.minRequestAmount = minRequestAmount;
    }

    public double getSlowRatioThreshold() {
        return slowRatioThreshold;
    }

    public void setSlowRatioThreshold(double slowRatioThreshold) {
        this.slowRatioThreshold = slowRatioThreshold;
    }

    public int getStatIntervalMs() {
        return statIntervalMs;
    }

    public void setStatIntervalMs(int statIntervalMs) {
        this.statIntervalMs = statIntervalMs;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
