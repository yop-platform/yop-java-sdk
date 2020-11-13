package com.yeepay.yop.sdk.http;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.utils.DateUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * Represents an HTTP response returned by a YOP service in response to a service request.
 */
public class YopHttpResponse {

    private static final Logger logger = LoggerFactory.getLogger(YopHttpResponse.class);

    private final CloseableHttpResponse httpResponse;

    private final InputStream content;

    private String contentStr;

    public YopHttpResponse(CloseableHttpResponse httpResponse) throws IOException {
        this.httpResponse = httpResponse;
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null && entity.isStreaming()) {
            this.content = entity.getContent();
        } else {
            this.content = null;
        }
    }

    public String getHeader(String name) {
        Header header = this.httpResponse.getFirstHeader(name);
        if (header == null) {
            return null;
        }
        return header.getValue();
    }

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

    public InputStream getContent() {
        return this.content;
    }


    public String readContent() {
        if (contentStr != null) {
            return contentStr;
        }
        try {
            contentStr = IOUtils.toString(content, YopConstants.DEFAULT_ENCODING);
            return contentStr;
        } catch (IOException ex) {
            throw new YopClientException("unable to read response content", ex);
        } finally {
            IOUtils.closeQuietly(content);
        }
    }

    public void setDecryptedContent(String decryptedContent) {
        this.contentStr = decryptedContent;
    }

    public String getStatusText() {
        return this.httpResponse.getStatusLine().getReasonPhrase();
    }

    public int getStatusCode() {
        return this.httpResponse.getStatusLine().getStatusCode();
    }

    public CloseableHttpResponse getHttpResponse() {
        return this.httpResponse;
    }

    public Map<String, String> getHeaders() {
        Map<String, String> headers = Maps.newHashMap();
        for (Header header : this.httpResponse.getAllHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        return headers;
    }

}
