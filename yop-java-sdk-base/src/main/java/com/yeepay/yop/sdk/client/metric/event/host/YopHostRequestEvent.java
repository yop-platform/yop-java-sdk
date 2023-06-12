/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric.event.host;

import com.yeepay.yop.sdk.client.metric.YopStatus;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * title: YOP域名请求事件<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/29
 */
public class YopHostRequestEvent<T> implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 服务域名
     */
    private String serverHost;

    /**
     * 服务ip
     */
    private String serverIp;

    /**
     * success/fail
     */
    private YopStatus status;

    /**
     * 耗时
     */
    private long elapsedMillis;

    /**
     * 数据(不同状态可能不一样)
     */
    private T data;

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

    public YopStatus getStatus() {
        return status;
    }

    public void setStatus(YopStatus status) {
        this.status = status;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public void setElapsedMillis(long elapsedMillis) {
        this.elapsedMillis = elapsedMillis;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
