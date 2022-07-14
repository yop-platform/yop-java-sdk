package com.yeepay.yop.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

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

    private YopRequestConfig requestConfig = YopRequestConfig.Builder.builder().build();

    /**
     * 商户自定义请求头
     */
    private final Map<String, String> headers = Maps.newHashMap();

    public BaseRequest() {
    }

    public BaseRequest(YopRequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }

    @JsonIgnore
    public YopRequestConfig getRequestConfig() {
        return requestConfig;
    }

    public BaseRequest addHeader(String name, String value) {
        validateParameter(name, value);
        headers.put(StringUtils.lowerCase(name), value);
        return this;
    }

    public BaseRequest addEncryptHeader(String name, String value) {
        addHeader(name, value);
        requestConfig.addEncryptHeader(name);
        return this;
    }

    public void setSubMerchantNo(String value) {
        addHeader(Headers.YOP_SUB_CUSTOMER_ID, value);
    }

    public void setEncryptSubMerchantNo(String value) {
        setSubMerchantNo(value);
        requestConfig.addEncryptHeader(Headers.YOP_SUB_CUSTOMER_ID);
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public abstract String getOperationId();

    protected void validateParameter(String name, Object value) {
        if (StringUtils.isEmpty(name)) {
            throw new YopClientException("parameter name:" + name + " should not be empty.");
        }
        if (value == null) {
            throw new YopClientException("parameter value for name:" + name + " can't be null.");
        }
        if (value instanceof File && !((File) value).exists()) {
            throw new YopClientException("file does not exist.");
        }
    }

}
