package com.yeepay.g3.sdk.yop.client.router;

import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.invoke.model.UriResource;

import java.net.URI;
import java.util.List;

/**
 * title: 网关路由<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2019<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-03-12 11:01
 */
public interface GateWayRouter {

    /**
     * 路由
     *
     * @param apiUri             apiUri
     * @param request            请求
     * @param excludeServerRoots 已失败列表
     * @return 路由地址
     */
    UriResource route(String apiUri, YopRequest request, List<URI> excludeServerRoots);

}
