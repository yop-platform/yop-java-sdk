package com.yeepay.yop.sdk.service.common.callback;

import com.yeepay.yop.sdk.http.YopContentType;
import com.yeepay.yop.sdk.service.common.callback.enums.YopCallbackHandleStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Map;

/**
 * title: 通知结果<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2019-06-13 22:55
 */
public class YopCallbackResponse implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 通知状态
     */
    private YopCallbackHandleStatus status;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 默认为text/plain，除此之外支持json，仅此两种
     */
    private YopContentType contentType = YopContentType.TEXT_PLAIN;

    /**
     * 响应头
     */
    private Map<String, String> headers;

    /**
     * 响应体
     */
    private String body;


    public YopCallbackResponse(YopCallbackHandleStatus status) {
        this.status = status;
    }

    public YopCallbackResponse(YopCallbackHandleStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public static YopCallbackResponse success() {
        return new YopCallbackResponse(YopCallbackHandleStatus.SUCCESS);
    }

    public static YopCallbackResponse fail(String message) {
        return new YopCallbackResponse(YopCallbackHandleStatus.FAIL, message);
    }

    public YopCallbackHandleStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public YopContentType getContentType() {
        return contentType;
    }

    public void setContentType(YopContentType contentType) {
        this.contentType = contentType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        // 自定义
        if (StringUtils.isNotBlank(body)) {
            return body;
        }

        // 默认
        String result;
        switch (status) {
            case SUCCESS:
                result = status.name();
                break;
            default:
                result = status.name() + ", cause:" + message;
        }
        if (YopContentType.JSON.equals(contentType)) {
            return String.format("{\"result\":\"%s\"}", result);
        }
        return result;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
