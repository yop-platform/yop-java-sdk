/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.config.provider.file;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Set;

/**
 * title: 上报配置<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/30
 */
public class YopReportConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    public static YopReportConfig DEFAULT_YOP_REPORT_CONFIG = new YopReportConfig();

    /**
     * 上报总开关
     */
    @JsonProperty("enable")
    private boolean enable = true;

    /**
     * 是否上报成功调用
     */
    @JsonProperty("enable_success_report")
    private boolean enableSuccessReport = false;

    /**
     * 上报周期：毫秒
     */
    @JsonProperty("send_interval_ms")
    private int sendIntervalMs = 3000;

    /**
     * 统计周期：毫秒
     */
    @JsonProperty("stat_interval_ms")
    private int statIntervalMs = 5000;

    /**
     * 队列大小，丢弃旧数据
     */
    @JsonProperty("max_queue_size")
    private int maxQueueSize = 1000;

    /**
     * 异常次数触发上报
     */
    @JsonProperty("max_fail_count")
    private int maxFailCount = 10;

    /**
     * 单异常次数阈值
     */
    @JsonProperty("max_fail_count_per_exception")
    private int maxFailCountPerEx = 5;

    /**
     * 耗时阈值
     */
    @JsonProperty("max_elapsed_ms")
    private int maxElapsedMs = 15000;

    /**
     * 单次上报条数
     */
    @JsonProperty("max_packet_size")
    private int maxPacketSize = 50;

    @JsonProperty("exclude_resources")
    private Set<String> excludeResources;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnableSuccessReport() {
        return enableSuccessReport;
    }

    public void setEnableSuccessReport(boolean enableSuccessReport) {
        this.enableSuccessReport = enableSuccessReport;
    }

    public int getSendIntervalMs() {
        return sendIntervalMs;
    }

    public void setSendIntervalMs(int sendIntervalMs) {
        this.sendIntervalMs = sendIntervalMs;
    }

    public int getStatIntervalMs() {
        return statIntervalMs;
    }

    public void setStatIntervalMs(int statIntervalMs) {
        this.statIntervalMs = statIntervalMs;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public int getMaxFailCount() {
        return maxFailCount;
    }

    public void setMaxFailCount(int maxFailCount) {
        this.maxFailCount = maxFailCount;
    }

    public int getMaxFailCountPerEx() {
        return maxFailCountPerEx;
    }

    public void setMaxFailCountPerEx(int maxFailCountPerEx) {
        this.maxFailCountPerEx = maxFailCountPerEx;
    }

    public int getMaxElapsedMs() {
        return maxElapsedMs;
    }

    public void setMaxElapsedMs(int maxElapsedMs) {
        this.maxElapsedMs = maxElapsedMs;
    }

    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }

    public Set<String> getExcludeResources() {
        return excludeResources;
    }

    public void setExcludeResources(Set<String> excludeResources) {
        this.excludeResources = excludeResources;
    }
}
