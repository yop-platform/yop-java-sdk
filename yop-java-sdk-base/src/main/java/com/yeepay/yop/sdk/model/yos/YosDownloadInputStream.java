package com.yeepay.yop.sdk.model.yos;

import com.yeepay.yop.sdk.http.YopHttpResponse;

import java.io.FilterInputStream;
import java.io.IOException;

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

    private final YopHttpResponse httpResponse;

    public YosDownloadInputStream(YopHttpResponse httpResponse) {
        super(httpResponse.getContent());
        this.httpResponse = httpResponse;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (null != httpResponse) {
            httpResponse.close();
        }
    }
}
