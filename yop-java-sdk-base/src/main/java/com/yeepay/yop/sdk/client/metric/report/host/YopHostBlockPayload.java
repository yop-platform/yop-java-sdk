/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report.host;

import java.io.Serializable;
import java.util.List;

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
public class YopHostBlockPayload implements Serializable {

    private static final long serialVersionUID = -1L;

    public YopHostBlockPayload(String blockHost, List<String> allHosts) {
        this.blockHost = blockHost;
        this.allHosts = allHosts;
    }

    private String blockHost;

    private List<String> allHosts;

    public String getBlockHost() {
        return blockHost;
    }

    public void setBlockHost(String blockHost) {
        this.blockHost = blockHost;
    }

    public List<String> getAllHosts() {
        return allHosts;
    }

    public void setAllHosts(List<String> allHosts) {
        this.allHosts = allHosts;
    }
}
