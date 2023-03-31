/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report.api;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

/**
 * title: 异常详情<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/20
 */
public class YopFailDetail implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 异常全类名
     */
    private String exType;

    /**
     * 异常消息
     */
    private String exMsg;

    /**
     * 发生时间戳
     */
    private List<Long> occurTime;

    public YopFailDetail(String exType, String exMsg) {
        this.exType = exType;
        this.exMsg = exMsg;
        this.occurTime = Lists.newLinkedList();
    }

    public YopFailDetail(String exType, String exMsg, List<Long> occurTime) {
        this.exType = exType;
        this.exMsg = exMsg;
        this.occurTime = occurTime;
    }

    public String getExType() {
        return exType;
    }

    public void setExType(String exType) {
        this.exType = exType;
    }

    public String getExMsg() {
        return exMsg;
    }

    public void setExMsg(String exMsg) {
        this.exMsg = exMsg;
    }

    public List<Long> getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(List<Long> occurTime) {
        this.occurTime = occurTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
