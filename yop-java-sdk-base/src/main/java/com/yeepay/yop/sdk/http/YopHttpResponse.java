/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http;

import java.io.Closeable;
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
public interface YopHttpResponse extends Closeable {

    /**
     * 获取指定响应头(string)
     *
     * @param name
     * @return
     */
    String getHeader(String name);

    /**
     * 获取指定响应头(number)
     *
     * @param name
     * @return
     */
    long getHeaderAsLong(String name);

    /**
     * 获取指定响应头(date)
     *
     * @param name
     * @return
     */
    Date getHeaderAsRfc822Date(String name);

    /**
     * 获取响应体
     *
     * @return
     */
    InputStream getContent();

    /**
     * 读取响应体并转为字符串
     *
     * @return
     */
    String readContent();

    /**
     * 重写响应体
     *
     * @param content
     */
    void setContent(Object content);

    /**
     * 获取响应状态
     *
     * @return
     */
    String getStatusText();

    /**
     * 获取响应状态码
     *
     * @return
     */
    int getStatusCode();

    /**
     * 获取原始响应头
     *
     * @return
     */
    Map<String, String> getHeaders();

    /**
     * 获取标准响应头(k转小写)
     *
     * @return
     */
    Map<String, String> getCanonicalHeaders();
}
