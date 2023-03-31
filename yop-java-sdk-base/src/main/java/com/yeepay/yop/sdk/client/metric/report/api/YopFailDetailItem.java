/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report.api;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.Serializable;

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
public class YopFailDetailItem implements Serializable {

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
    private Long occurTime;

    public YopFailDetailItem(String exType, String exMsg) {
        this.exType = exType;
        this.exMsg = exMsg;
        this.occurTime = System.currentTimeMillis();
    }

    public YopFailDetailItem(Throwable ex) {
        Throwable exCause = ExceptionUtils.getRootCause(ex);
        if (null == exCause) {
            exCause = ex;
        }
        this.exType = exCause.getClass().getCanonicalName();
        this.exMsg = StringUtils.abbreviate(StringUtils.defaultString(exCause.getMessage(), ""),
                300);// TODO 是否足够
        this.occurTime = System.currentTimeMillis();
    }

    public String getExType() {
        return exType;
    }

    public String getExMsg() {
        return exMsg;
    }

    public Long getOccurTime() {
        return occurTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
