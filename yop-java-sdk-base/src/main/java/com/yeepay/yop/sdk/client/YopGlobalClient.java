/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client;

import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;

/**
 * title: Yop通用Client<br>
 * description: 主要内部用<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/4/1
 */
public class YopGlobalClient {

    public static YopClient getClient() {
        return YopClientHolder.INNER_GLOBAL_CLIENT;
    }

    private static class YopClientHolder {
        private static final YopClient INNER_GLOBAL_CLIENT = YopClientBuilder.builder().build();
    }
}
