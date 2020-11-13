package com.yeepay.yop.sdk.model.yos;

import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

/**
 * title: Yos下载流<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/3/28 15:48
 */
public class YosDownloadInputStream extends FilterInputStream {

    private final CloseableHttpResponse httpResponse;

    public YosDownloadInputStream(InputStream content, CloseableHttpResponse httpResponse) {
        super(content);
        this.httpResponse = httpResponse;
    }

    @Override
    public void close() throws IOException {
        this.httpResponse.close();
        try {
            super.close();
        } catch (SocketException e) {
            // expected from some implementations because the stream is closed
        }
    }
}
