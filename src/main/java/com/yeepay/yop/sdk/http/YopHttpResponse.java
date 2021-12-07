/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021/12/1
 */
public interface YopHttpResponse {

    String getHeader(String name);

    long getHeaderAsLong(String name);

    Date getHeaderAsRfc822Date(String name);

    InputStream getContent();

    String readContent();

    void setDecryptedContent(String decryptedContent);

    String getStatusText();

    int getStatusCode();

    Map<String, String> getHeaders();
}
