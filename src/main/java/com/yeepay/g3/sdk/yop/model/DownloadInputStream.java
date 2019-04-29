package com.yeepay.g3.sdk.yop.model;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

/**
 * title:下载流 <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2019<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-04-29 16:08
 */
public class DownloadInputStream extends FilterInputStream {

    private final CloseableHttpResponse httpResponse;

    /**
     * Creates a <code>FilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param in           the underlying input stream, or <code>null</code> if
     *                     this instance is to be created without an underlying stream.
     * @param httpResponse http返回
     */
    public DownloadInputStream(InputStream in, CloseableHttpResponse httpResponse) {
        super(in);
        this.httpResponse = httpResponse;
    }

    @Override
    public void close() throws IOException {
        HttpClientUtils.closeQuietly(httpResponse);
        try {
            super.close();
        } catch (SocketException ex) {
            // expected from some implementations because the stream is closed
        }
    }
}
