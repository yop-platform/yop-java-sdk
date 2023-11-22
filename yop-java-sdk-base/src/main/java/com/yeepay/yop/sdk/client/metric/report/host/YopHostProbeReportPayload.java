/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report.host;

import java.io.Serializable;
import java.util.List;

/**
 * title: 探测上报<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/22
 */
public class YopHostProbeReportPayload implements Serializable {

    private static final long serialVersionUID = -1L;

    public YopHostProbeReportPayload(String hostType, List<String> blockedHosts,
                                     List<String> candidateHosts) {
        this.hostType = hostType;
        this.blockedHosts = blockedHosts;
        this.candidateHosts = candidateHosts;
    }

    /**
     * 域名类型
     */
    private String hostType;

    /**
     * 已熔断域名列表
     */
    private List<String> blockedHosts;

    /**
     * 备选域名列表
     */
    private List<String> candidateHosts;

    public String getHostType() {
        return hostType;
    }

    public void setHostType(String hostType) {
        this.hostType = hostType;
    }

    public List<String> getBlockedHosts() {
        return blockedHosts;
    }

    public void setBlockedHosts(List<String> blockedHosts) {
        this.blockedHosts = blockedHosts;
    }

    public List<String> getCandidateHosts() {
        return candidateHosts;
    }

    public void setCandidateHosts(List<String> candidateHosts) {
        this.candidateHosts = candidateHosts;
    }
}
