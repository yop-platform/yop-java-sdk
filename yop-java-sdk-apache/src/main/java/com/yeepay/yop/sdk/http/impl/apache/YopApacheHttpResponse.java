package com.yeepay.yop.sdk.http.impl.apache;

import com.yeepay.yop.sdk.http.AbstractYopHttpResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;

import java.io.IOException;

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
        for (Header header : this.httpResponse.getAllHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
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
    public void close() throws IOException {
        HttpClientUtils.closeQuietly(this.httpResponse);
    }
}
