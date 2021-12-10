package com.yeepay.yop.sdk.http.impl.ok;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.http.AbstractYopHttpResponse;
import kotlin.Pair;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Map;

/**
 * Represents an HTTP response returned by a YOP service in response to a service request.
 */
public class YopOkHttpResponse extends AbstractYopHttpResponse {

    private final Response httpResponse;

    public YopOkHttpResponse(Response httpResponse) throws IOException {
        this.httpResponse = httpResponse;
        final ResponseBody body = httpResponse.body();
        if (null != body) {
            super.content = body.byteStream();
        } else {
            super.content = null;
        }
    }

    @Override
    public String getHeader(String name) {
        return this.httpResponse.header(name);
    }

    @Override
    public String getStatusText() {
        return this.httpResponse.code() + "";// todo 构造
    }

    @Override
    public int getStatusCode() {
        return this.httpResponse.code();
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = Maps.newHashMap();
        for (Pair<? extends String, ? extends String> header : this.httpResponse.headers()) {
            headers.put(header.getFirst(), header.getSecond());
        }
        return headers;
    }

}
