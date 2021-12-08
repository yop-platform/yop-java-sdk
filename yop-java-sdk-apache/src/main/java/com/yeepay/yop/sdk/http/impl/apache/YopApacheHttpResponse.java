package com.yeepay.yop.sdk.http.impl.apache;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.http.AbstractYopHttpResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Represents an HTTP response returned by a YOP service in response to a service request.
 */
public class YopApacheHttpResponse extends AbstractYopHttpResponse {

    private final CloseableHttpResponse httpResponse;

    public YopApacheHttpResponse(CloseableHttpResponse httpResponse) throws IOException {
        this.httpResponse = httpResponse;
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null && entity.isStreaming()) {
            super.content = entity.getContent();
        } else {
            super.content = null;
        }
    }

    @Override
    public String getHeader(String name) {
        Header header = this.httpResponse.getFirstHeader(name);
        if (header == null) {
            return null;
        }
        return header.getValue();
    }

    @Override
    public String getStatusText() {
        return this.httpResponse.getStatusLine().getReasonPhrase();
    }

    @Override
    public int getStatusCode() {
        return this.httpResponse.getStatusLine().getStatusCode();
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = Maps.newHashMap();
        for (Header header : this.httpResponse.getAllHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        return headers;
    }

}
