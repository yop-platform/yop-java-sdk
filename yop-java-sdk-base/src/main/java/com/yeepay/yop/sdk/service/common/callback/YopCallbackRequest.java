/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common.callback;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.http.YopContentType;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * title: YOP商户回调原始请求<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/12
 */
public class YopCallbackRequest implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 请求路径
     */
    private String httpPath;

    /**
     * 请求方法
     */
    private String httpMethod;

    /**
     * 内容格式
     */
    private YopContentType contentType;

    /**
     * 请求header
     */
    private Map<String, String> headers;

    /**
     * 请求规范header
     */
    private Map<String, String> canonicalHeaders;

    /**
     * 请求参数(query、form)
     */
    private Map<String, List<String>> params = Maps.newHashMap();

    /**
     * 请求体(json、multipart、stream。。。)
     */
    private Object content;

    public YopCallbackRequest(String httpPath, String httpMethod) {
        this.httpPath = httpPath;
        this.httpMethod = httpMethod;
    }

    public static YopCallbackRequest fromYopRequest(Request<YopRequest> yopRequest) {
        final YopRequest originRequest = yopRequest.getOriginalRequestObject();
        final YopCallbackRequest callbackRequest = new YopCallbackRequest(originRequest.getApiUri(), originRequest.getHttpMethod());
        callbackRequest.setContentType(yopRequest.getContentType());
        callbackRequest.setHeaders(yopRequest.getHeaders());
        callbackRequest.setContent(yopRequest.getContent());
        return callbackRequest;
    }

    public String getHttpPath() {
        return httpPath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public YopContentType getContentType() {
        return contentType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getCanonicalHeaders() {
        return canonicalHeaders;
    }

    public Map<String, List<String>> getParams() {
        return params;
    }

    public Object getContent() {
        return content;
    }

    public String getParam(String name) {
        if (MapUtils.isNotEmpty(params)) {
            final List<String> values = params.get(name);
            if (CollectionUtils.isNotEmpty(values)) {
                return values.get(0);
            }
        }
        return null;
    }

    public YopCallbackRequest setHttpPath(String httpPath) {
        this.httpPath = httpPath;
        return this;
    }

    public YopCallbackRequest setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    public YopCallbackRequest setContentType(YopContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public YopCallbackRequest setHeaders(Map<String, String> headers) {
        this.headers = headers;
        if (MapUtils.isNotEmpty(headers)) {
            Map<String, String> canonicalHeaders = Maps.newHashMapWithExpectedSize(headers.size());
            headers.forEach((k,v) -> canonicalHeaders.put(k.trim().toLowerCase(), v));
            this.canonicalHeaders = canonicalHeaders;
        }
        return this;
    }

    public YopCallbackRequest addParam(String name, String value) {
        this.params.computeIfAbsent(name, p -> Lists.newLinkedList()).add(value);
        return this;
    }

    public YopCallbackRequest setParams(Map<String, List<String>> params) {
        this.params = params;
        return this;
    }

    public YopCallbackRequest setContent(Object content) {
        this.content = content;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
