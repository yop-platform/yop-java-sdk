/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.utils.DateUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

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

    protected InputStream content;
    protected String contentStr;

    @Override
    public InputStream getContent() {
        return this.content;
    }

    @Override
    public String readContent() {
        if (contentStr != null) {
            return contentStr;
        }
        try(InputStream tmp = this.content) {
            contentStr = IOUtils.toString(tmp, YopConstants.DEFAULT_ENCODING);
            return contentStr;
        } catch (IOException ex) {
            throw new YopClientException("unable to read response content", ex);
        }
    }

    @Override
    public void setDecryptedContent(String decryptedContent) {
        this.contentStr = decryptedContent;
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
}
