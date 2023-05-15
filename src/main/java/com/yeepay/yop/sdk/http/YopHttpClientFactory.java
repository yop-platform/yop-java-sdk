package com.yeepay.yop.sdk.http;

import com.yeepay.g3.core.yop.sdk.sample.client.ClientConfiguration;
import com.yeepay.g3.core.yop.sdk.sample.client.support.ClientParamsSupport;

/**
 * title: YopHttpClient工厂<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/8/6 16:26
 */
public class YopHttpClientFactory {

    private static volatile YopHttpClient defaultClient;

    public static YopHttpClient getDefaultClient() {
        if (defaultClient == null) {
            synchronized (YopHttpClientFactory.class) {
                if (defaultClient == null) {
                    ClientConfiguration clientConfiguration = ClientParamsSupport.getDefaultClientParams().getClientConfiguration();
                    defaultClient = getClient(clientConfiguration);
                }
            }
        }
        return defaultClient;
    }

    public static YopHttpClient getClient(ClientConfiguration clientConfiguration) {
        return new YopHttpClient(clientConfiguration);
    }
}
