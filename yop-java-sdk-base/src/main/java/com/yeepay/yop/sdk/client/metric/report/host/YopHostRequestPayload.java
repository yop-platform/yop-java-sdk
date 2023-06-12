/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report.host;

import com.yeepay.yop.sdk.client.metric.YopFailureList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

/**
 * title: 上报内容-YOP域名请求体<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/20
 */
public class YopHostRequestPayload implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 请求server host
     */
    private String serverHost;

    /**
     * 请求server ip
     */
    private String serverIp;

    /**
     * 成功笔数
     */
    private int successCount;

    /**
     * 失败笔数
     */
    private int failCount;

    /**
     * 最大耗时(毫秒)
     */
    private long maxElapsedMillis;//TODO 其他耗时指标

    /**
     * 失败明细
     */
    private List<YopFailureList> failDetails;

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public long getMaxElapsedMillis() {
        return maxElapsedMillis;
    }

    public void setMaxElapsedMillis(long maxElapsedMillis) {
        this.maxElapsedMillis = maxElapsedMillis;
    }

    public List<YopFailureList> getFailDetails() {
        return failDetails;
    }

    public void setFailDetails(List<YopFailureList> failDetails) {
        this.failDetails = failDetails;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
