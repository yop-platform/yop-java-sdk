/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http.impl.apache;

import com.yeepay.yop.sdk.client.ClientConfiguration;
import com.yeepay.yop.sdk.http.YopHttpClient;
import com.yeepay.yop.sdk.http.YopHttpClientProvider;

import static com.yeepay.yop.sdk.YopConstants.YOP_HTTP_CLIENT_IMPL_APACHE;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021/12/8
 */
public class YopApacheHttpClientProvider implements YopHttpClientProvider {

    @Override
    public String name() {
        return YOP_HTTP_CLIENT_IMPL_APACHE;
    }

    @Override
    public YopHttpClient get(ClientConfiguration clientConfig) {
        return new com.yeepay.yop.sdk.http.impl.apache.YopHttpClient(clientConfig);
    }
}
