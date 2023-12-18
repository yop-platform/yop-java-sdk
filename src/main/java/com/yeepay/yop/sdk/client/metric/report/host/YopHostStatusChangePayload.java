/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report.host;

import java.io.Serializable;

/**
 * title: 域名熔断-上报内容<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/8/4
 */

/**
 * title: 域名熔断-上报内容<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/8/4
 */
public class YopHostStatusChangePayload implements Serializable {

    private static final long serialVersionUID = -1L;

    public YopHostStatusChangePayload(String host, String prevStatus, String status) {
        this.host = host;
        this.prevStatus = prevStatus;
        this.status = status;
    }

    public YopHostStatusChangePayload(String host, String prevStatus, String status, String rule) {
        this.host = host;
        this.prevStatus = prevStatus;
        this.status = status;
        this.rule = rule;
    }

    private String host;

    private String prevStatus;

    private String status;

    private String rule = "";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPrevStatus() {
        return prevStatus;
    }

    public void setPrevStatus(String prevStatus) {
        this.prevStatus = prevStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }
}
