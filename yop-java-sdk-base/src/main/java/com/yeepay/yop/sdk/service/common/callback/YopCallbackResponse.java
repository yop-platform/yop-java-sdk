package com.yeepay.yop.sdk.service.common.callback;

import com.yeepay.yop.sdk.http.YopContentType;
import com.yeepay.yop.sdk.service.common.callback.enums.YopCallbackHandleStatus;

import java.io.Serializable;

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

    private static final YopCallbackResponse SUCCESS_RESULT = new YopCallbackResponse(YopCallbackHandleStatus.Success);

    /**
     * 通知状态
     */
    private YopCallbackHandleStatus status;

    /**
     * 错误消息
     */
    private String message;

    public YopCallbackResponse(YopCallbackHandleStatus status) {
        this.status = status;
    }

    public YopCallbackResponse(YopCallbackHandleStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public static YopCallbackResponse success() {
        return SUCCESS_RESULT;
    }

    public static YopCallbackResponse fail(String message) {
        return new YopCallbackResponse(YopCallbackHandleStatus.Fail, message);
    }

    public YopCallbackHandleStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        switch (status) {
            case Success:
                return status.name();
            default:
                return status.name() + ", cause:" + message;
        }
    }
}
