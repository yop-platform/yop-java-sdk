package com.yeepay.yop.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * title: 请求基础类<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/10/27 11:02
 */
public abstract class BaseRequest implements Serializable, Cloneable {

    private RequestConfig requestConfig = RequestConfig.Builder.builder().build();

    @JsonIgnore
    public RequestConfig getRequestConfig() {
        return requestConfig;
    }

    public BaseRequest withRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
        return this;
    }

    public abstract String getOperationId();

}
