package com.yeepay.yop.sdk.service.common;

import com.yeepay.yop.sdk.auth.req.AuthorizationReqRegistry;
import com.yeepay.yop.sdk.client.AbstractServiceClientBuilder;
import com.yeepay.yop.sdk.client.ClientParams;
import com.yeepay.yop.sdk.service.common.auth.MockAuthorizationReqRegistry;

/**
 * title: 通用ClientBuilder<br>
 * description: <br>
 * Copyright: Copyright (c) 2020<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2020-01-03 16:13
 */
public class YopClientBuilder extends AbstractServiceClientBuilder<YopClientBuilder, YopClientImpl> {

    private static final AuthorizationReqRegistry REGISTRY = new MockAuthorizationReqRegistry();

    @Override
    protected AuthorizationReqRegistry authorizationReqRegistry() {
        return REGISTRY;
    }

    @Override
    protected YopClientImpl build(ClientParams params) {
        return new YopClientImpl(params);
    }

    /**
     * @return Create new instance of builder with all defaults set.
     */
    public static YopClientBuilder builder() {
        return new YopClientBuilder();
    }
}
