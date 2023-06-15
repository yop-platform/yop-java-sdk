/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * title: YOP异常<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/20
 */
public class YopFailureItem implements Serializable {

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date occurTime;

    public YopFailureItem(String exType, String exMsg) {
        this.exType = exType;
        this.exMsg = exMsg;
        this.occurTime = new Date();
    }

    public YopFailureItem(Throwable ex) {
        Throwable exCause = ExceptionUtils.getRootCause(ex);
        if (null == exCause) {
            exCause = ex;
        }
        this.exType = exCause.getClass().getCanonicalName();
        this.exMsg = StringUtils.abbreviate(StringUtils.defaultString(exCause.getMessage(), ""),
                300);// TODO 是否足够
        this.occurTime = new Date();
    }

    public String getExType() {
        return exType;
    }

    public String getExMsg() {
        return exMsg;
    }

    public Date getOccurTime() {
        return occurTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
