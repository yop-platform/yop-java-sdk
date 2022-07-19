/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.utils.DateUtils;
import com.yeepay.yop.sdk.utils.StreamUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
 * @since 2021/12/7
 */
public abstract class AbstractYopHttpResponse implements YopHttpResponse {

    private static final Logger logger = LoggerFactory.getLogger(AbstractYopHttpResponse.class);

    private InputStream content;
    private String contentStr;
    private final Map<String, String> headers = Maps.newHashMap();
    private final Map<String, String> canonicalHeaders = Maps.newHashMap();

    @Override
    public String getHeader(String name) {
        return canonicalHeaders.get(StringUtils.lowerCase(name));
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public Map<String, String> getCanonicalHeaders() {
        return canonicalHeaders;
    }

    @Override
    public InputStream getContent() {
        return this.content;
    }

    @Override
    public String readContent() {
        if (contentStr != null) {
            return contentStr;
        }
        try {
            contentStr = IOUtils.toString(this.content, YopConstants.DEFAULT_ENCODING);
            return contentStr;
        } catch (IOException ex) {
            throw new YopClientException("unable to read response content", ex);
        } finally {
            StreamUtils.closeQuietly(this.content);
        }
    }

    @Override
    public void setContent(Object content) {
        if (content instanceof String) {
            this.contentStr = (String) content;
        } else {
            this.content = (InputStream) content;
        }
    }

    @Override
    public long getHeaderAsLong(String name) {
        String value = this.getHeader(name);
        if (value == null) {
            return -1;
        }
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            logger.warn("Invalid " + name + ":" + value, e);
            return -1;
        }
    }

    @Override
    public Date getHeaderAsRfc822Date(String name) {
        String value = this.getHeader(name);
        if (value == null) {
            return null;
        }
        try {
            return DateUtils.parseRfc822Date(value);
        } catch (Exception e) {
            logger.warn("Invalid " + name + ":" + value, e);
            return null;
        }
    }

    protected void fillHeader(String name, String value) {
        if (StringUtils.isNotEmpty(name)) {
            headers.put(name, value);
            canonicalHeaders.put(name.trim().toLowerCase(), value);
        }
    }
}
