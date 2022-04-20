package com.yeepay.yop.sdk.http.impl.ok;

import com.yeepay.yop.sdk.http.AbstractYopHttpResponse;
import kotlin.Pair;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

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
        for (Pair<? extends String, ? extends String> header : this.httpResponse.headers()) {
            headers.put(header.getFirst(), header.getSecond());
        }
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
    public void close() throws IOException {
        if (null != httpResponse) {
            httpResponse.close();
        }
    }
}
