/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.exception.param;

import com.yeepay.yop.sdk.exception.YopClientBizException;

import static com.yeepay.yop.sdk.constants.CharacterConstants.COLON;
import static com.yeepay.yop.sdk.constants.ExceptionConstants.SDK_INVOKE_REQUEST_PARAM_FORMAT_PREFIX;

/**
 * title: 请求参数非法<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2025/2/24
 */
public class IllegalParamFormatException extends YopClientBizException {

    public IllegalParamFormatException(String field, String message) {
        super(SDK_INVOKE_REQUEST_PARAM_FORMAT_PREFIX + COLON + field, message);
    }

    public IllegalParamFormatException(String field, String message, Throwable cause) {
        super(SDK_INVOKE_REQUEST_PARAM_FORMAT_PREFIX + COLON + field, message, cause);
    }
}
