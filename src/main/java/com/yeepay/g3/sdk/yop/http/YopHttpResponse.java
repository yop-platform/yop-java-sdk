package com.yeepay.g3.sdk.yop.http;

import com.google.common.collect.Maps;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Represents an HTTP response returned by a YOP service in response to a service request.
 */
public class YopHttpResponse {
    private static final Logger logger = LoggerFactory.getLogger(YopHttpResponse.class);

    private CloseableHttpResponse httpResponse;

    private InputStream content;

    public YopHttpResponse(CloseableHttpResponse httpResponse) throws IOException {
        this.httpResponse = httpResponse;
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null && entity.isStreaming()) {
            this.content = entity.getContent();
        }
    }

    public String getHeader(String name) {
        Header header = this.httpResponse.getFirstHeader(name);
        if (header == null) {
            return null;
        }
        return header.getValue();
    }

    public InputStream getContent() {
        return this.content;
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
