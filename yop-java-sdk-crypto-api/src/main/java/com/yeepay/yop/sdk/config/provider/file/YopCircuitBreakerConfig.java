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
public class YopCircuitBreakerConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    public static final YopCircuitBreakerConfig DEFAULT_CONFIG = new YopCircuitBreakerConfig();

    // region 断路器

    /**
     * 启用断路器
     */
    @JsonProperty("enable")
    private boolean enable = true;

    /**
     * 请求量阈值(达到该数量，才会检查健康数据，进而熔断)
     * 默认10
     */
    @JsonProperty("min_request_count_threshold")
    private int minRequestCountThreshold = 10;

    /**
     * 错误率阈值(达到该值，进入熔断)
     */
    @JsonProperty("error_ratio_threshold")
    private double errorRatioThreshold = 0.5;

    /**
     * 错误量阈值(达到该值，进入熔断)
     */
    @JsonProperty("error_count_threshold")
    private int errorCountThreshold = 3;

    /**
     * 统计窗口时长(毫秒，失败率会在该窗口内汇总计算)
     */
    @JsonProperty("metrics_window_in_milliseconds")
    private int metricsWindowInMilliseconds = 1000;

    /**
     * 熔断时长(毫秒，该窗口期后，会进入半开)
     */
    @JsonProperty("sleep_window_in_milliseconds")
    private int sleepWindowInMilliseconds = 5000;
    // endregion

    // region yop扩展
    /**
     * 非短路异常
     */
    @JsonProperty("yop_exclude_exceptions")
    private Set<String> excludeExceptions = Sets.newHashSet("java.net.SocketTimeoutException:Read timed out");
    // endRegion

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getMinRequestCountThreshold() {
        return minRequestCountThreshold;
    }

    public void setMinRequestCountThreshold(int minRequestCountThreshold) {
        this.minRequestCountThreshold = minRequestCountThreshold;
    }

    public double getErrorRatioThreshold() {
        return errorRatioThreshold;
    }

    public void setErrorRatioThreshold(double errorRatioThreshold) {
        this.errorRatioThreshold = errorRatioThreshold;
    }

    public int getErrorCountThreshold() {
        return errorCountThreshold;
    }

    public void setErrorCountThreshold(int errorCountThreshold) {
        this.errorCountThreshold = errorCountThreshold;
    }

    public int getSleepWindowInMilliseconds() {
        return sleepWindowInMilliseconds;
    }

    public void setSleepWindowInMilliseconds(int sleepWindowInMilliseconds) {
        this.sleepWindowInMilliseconds = sleepWindowInMilliseconds;
    }

    public int getMetricsWindowInMilliseconds() {
        return metricsWindowInMilliseconds;
    }

    public void setMetricsWindowInMilliseconds(int metricsWindowInMilliseconds) {
        this.metricsWindowInMilliseconds = metricsWindowInMilliseconds;
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
