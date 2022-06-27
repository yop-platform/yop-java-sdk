/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http;

import static com.yeepay.yop.sdk.YopConstants.*;

/**
 * title: http 内容格式<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/12
 */
public enum YopContentType {
    FORM_URL_ENCODE(YOP_HTTP_CONTENT_TYPE_FORM),
    MULTIPART_FORM(YOP_HTTP_CONTENT_TYPE_MULTIPART_FORM),
    JSON(YOP_HTTP_CONTENT_TYPE_JSON),
    OCTET_STREAM(YOP_HTTP_CONTENT_TYPE_STREAM),
    TEXT_PLAIN(YOP_HTTP_CONTENT_TYPE_TEXT);

    private String value;

    YopContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
