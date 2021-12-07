package com.yeepay.yop.sdk.model.yos;

import java.io.FilterInputStream;
import java.io.InputStream;

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

    public YosDownloadInputStream(InputStream content) {
        super(content);
    }
}
