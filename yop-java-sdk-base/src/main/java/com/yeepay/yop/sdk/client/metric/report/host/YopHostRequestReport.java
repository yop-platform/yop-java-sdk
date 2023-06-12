/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report.host;

import com.yeepay.yop.sdk.client.metric.report.YopReport;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * title: 上报内容-YOP域名请求<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/20
 */
public class YopHostRequestReport implements YopReport {

    private static final long serialVersionUID = -1L;

    private String type = "YopHostRequestReport";

    private int version = 1;

    private YopHostRequestPayload payload;

    private Date beginTime = new Date();

    private Date endTime;

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public YopHostRequestPayload getPayload() {
        return payload;
    }

    public void setPayload(YopHostRequestPayload payload) {
        this.payload = payload;
    }

    @Override
    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
