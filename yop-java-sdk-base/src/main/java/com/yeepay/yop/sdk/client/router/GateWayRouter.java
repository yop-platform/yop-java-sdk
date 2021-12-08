package com.yeepay.yop.sdk.client.router;

import com.yeepay.yop.sdk.internal.Request;

import java.net.URI;

/**
 * title: 网关路由<br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-03-12 17:20
 */
public interface GateWayRouter {

    /**
     * 路由
     *
     * @param appKey  appKey
     * @param request 请求
     * @return serverRoot URI
     */
    URI route(String appKey, Request request);

}
