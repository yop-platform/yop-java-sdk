package com.yeepay.yop.sdk.client.router;

import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.invoke.model.UriResource;

import java.net.URI;
import java.util.List;

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
     * @param appKey             应用
     * @param request            请求
     * @param excludeServerRoots 已失败列表
     * @return serverRoot URI
     */
    UriResource route(String appKey, Request<?> request, List<URI> excludeServerRoots);

}
