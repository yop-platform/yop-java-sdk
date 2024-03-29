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
     * 服务方
     */
    private String provider;

    /**
     * 服务环境
     */
    private String env;

    /**
     * 应用
     */
    private String appKey;

    /**
     * 服务资源
     */
    private String serverResource;

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

    private boolean retry;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getServerResource() {
        return serverResource;
    }

    public void setServerResource(String serverResource) {
        this.serverResource = serverResource;
    }

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

    public boolean isRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
