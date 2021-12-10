/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http;

import com.yeepay.yop.sdk.client.ClientConfiguration;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021/12/7
 */
public interface YopHttpClientProvider {

    /**
     * the name of impl of YopHttpClient
     * @return name
     */
    String name();

    /**
     * produce a YopHttpClient by the given config
     * @param clientConfig Configuration options specifying how this client will communicate with YOP (ex: proxy settings,
     *      retry count, etc.).
     * @return a YopHttpClient impl
     */
    YopHttpClient get(ClientConfiguration clientConfig);
}
