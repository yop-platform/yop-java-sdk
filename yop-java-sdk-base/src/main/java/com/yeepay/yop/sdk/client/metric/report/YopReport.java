/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.report;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * title: 上报内容<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/20
 */
public interface YopReport extends Serializable {

    /**
     * 内容类型
     *
     * @return string
     */
    String getType();

    /**
     * 内容版本
     *
     * @return string
     */
    int getVersion();

    /**
     * 内容明细
     *
     * @return obj
     */
    Object getPayload();

    /**
     * 开始时间
     *
     * @return long
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    Date getBeginDate();

    /**
     * 开始时间
     *
     * @return long
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "GMT+8")
    Date getEndDate();

}
