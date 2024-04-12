/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router;

import com.yeepay.yop.sdk.invoke.*;
import com.yeepay.yop.sdk.invoke.model.AnalyzedException;
import com.yeepay.yop.sdk.invoke.model.RetryPolicy;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.router.config.YopFileRouteConfigProvider;
import com.yeepay.yop.sdk.router.config.YopRouteConfigProvider;
import com.yeepay.yop.sdk.router.policy.RouterPolicyFactory;
import com.yeepay.yop.sdk.router.utils.InvokeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * title: 域名切换客户端<br>
 * description: 请使用单例模式<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/2/5
 */
public class SimpleUriResourceRouteClient {
    private static final Map<String, InnerRouteClient> CACHED_ROUTERS = new ConcurrentHashMap<>();

    private final InnerRouteClient innerRouteClient;

    public SimpleUriResourceRouteClient(List<String> targetServers) {
        this(targetServers, RouterPolicyFactory.get(YopRouterConstants.ROUTER_POLICY_DEFAULT));
    }

    public SimpleUriResourceRouteClient(List<String> targetServers, RouterPolicy routerPolicy) {
        this(targetServers, routerPolicy, SimpleRetryPolicy.singleton());
    }

    public SimpleUriResourceRouteClient(List<String> targetServers, RouterPolicy routerPolicy, RetryPolicy retryPolicy) {
        String serverKey = StringUtils.join(targetServers, ",");
        synchronized (CACHED_ROUTERS) {
            if (!CACHED_ROUTERS.containsKey(serverKey)) {
                String resourceGroup = UUID.randomUUID().toString();
                this.innerRouteClient = new InnerRouteClient(YopFileRouteConfigProvider.INSTANCE,
                        new SimpleUriResourceRouter<>(resourceGroup, targetServers, routerPolicy), retryPolicy);
                CACHED_ROUTERS.put(serverKey, this.innerRouteClient);
            } else {
                this.innerRouteClient = CACHED_ROUTERS.get(serverKey);
            }
        }
    }

    /**
     * 发起路由调用
     *
     * @param businessLogic 业务逻辑
     * @param <Output>      出参范型
     * @return 业务出参
     */
    public <Output> Output route(SimpleUriResourceBusinessLogic<Output> businessLogic) {
        return this.innerRouteClient.route(businessLogic, new SimpleContext());
    }

    /**
     * 发起路由调用
     *
     * @param businessLogic 业务逻辑
     * @param <Output>      出参范型
     * @return 业务出参
     */
    public <Output> Output route(SimpleUriResourceBusinessLogic<Output> businessLogic, SimpleContext context) {
        return this.innerRouteClient.route(businessLogic, context);
    }

    private class InnerRouteClient {
        private YopRouteConfigProvider routeConfigProvider;
        private Router<UriResource, Object, SimpleContext> router;
        private RetryPolicy retryPolicy;

        public InnerRouteClient(YopRouteConfigProvider routeConfigProvider,
                                Router<UriResource, Object, SimpleContext> router,
                                RetryPolicy retryPolicy) {
            this.routeConfigProvider = routeConfigProvider;
            this.router = router;
            this.retryPolicy = retryPolicy;
        }

        /**
         * 发起路由调用
         *
         * @param businessLogic 业务逻辑
         * @param <Output>      出参范型
         * @return 业务出参
         */
        private <Output> Output route(SimpleUriResourceBusinessLogic<Output> businessLogic, SimpleContext context) {
            // 业务处理、熔断操作、异常分析封装
            UriResourceRouteInvoker<Object, Output, SimpleContext, AnalyzedException> uriResourceRouteInvoker
                    = new SimpleUriResourceInvoker<>(businessLogic, context, this.routeConfigProvider);
            return InvokeUtils.invoke(uriResourceRouteInvoker, this.router, this.retryPolicy);
        }

    }

}
