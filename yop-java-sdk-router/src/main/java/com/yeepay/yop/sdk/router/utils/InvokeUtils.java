/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.utils;

import com.yeepay.yop.sdk.invoke.Router;
import com.yeepay.yop.sdk.invoke.SimpleRetryPolicy;
import com.yeepay.yop.sdk.invoke.UriResourceRouteInvoker;
import com.yeepay.yop.sdk.invoke.UriResourceRouteInvokerWrapper;
import com.yeepay.yop.sdk.invoke.model.AnalyzedException;
import com.yeepay.yop.sdk.invoke.model.RetryPolicy;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.router.SimpleContext;
import com.yeepay.yop.sdk.router.SimpleUriResourceBusinessLogic;
import com.yeepay.yop.sdk.router.SimpleUriResourceInvoker;
import com.yeepay.yop.sdk.router.config.YopFileRouteConfigProvider;
import com.yeepay.yop.sdk.router.config.YopRouteConfigProvider;
import com.yeepay.yop.sdk.utils.RandomUtils;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * title: 工具类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/2/2
 */
public class InvokeUtils {

    /**
     * 发起调用
     *
     * @param businessLogic 业务逻辑
     * @param router        自实现域名路由
     * @param <Output>      出参范型
     * @return 业务出参
     */
    public static <Output> Output invoke(SimpleUriResourceBusinessLogic<Output> businessLogic,
                                         Router<UriResource, Object, SimpleContext> router) {
        return invoke(businessLogic, router, YopFileRouteConfigProvider.INSTANCE, new SimpleRetryPolicy(3));
    }

    /**
     * 发起调用
     *
     * @param businessLogic       业务逻辑
     * @param router              自实现域名路由
     * @param routeConfigProvider 自实现路由配置加载
     * @param retryPolicy         自实现重试策略
     * @param <Output>            出参范型
     * @return 业务出参
     */
    public static <Output> Output invoke(SimpleUriResourceBusinessLogic<Output> businessLogic,
                                         Router<UriResource, Object, SimpleContext> router,
                                         YopRouteConfigProvider routeConfigProvider, RetryPolicy retryPolicy) {
        // 业务处理、熔断操作、异常分析封装
        UriResourceRouteInvoker<Object, Output, SimpleContext, AnalyzedException> uriResourceRouteInvoker
                = new SimpleUriResourceInvoker<>(businessLogic, new SimpleContext(), routeConfigProvider);
        return invoke(uriResourceRouteInvoker, router, retryPolicy);
    }

    /**
     * 发起调用
     *
     * @param invoker     自实现调用逻辑
     * @param router      自实现域名路由
     * @param retryPolicy 自实现重试策略
     * @param <Output>    出参范型
     * @return 业务出参
     */
    public static <Output> Output invoke(UriResourceRouteInvoker<Object, Output, SimpleContext, AnalyzedException> invoker,
                                         Router<UriResource, Object, SimpleContext> router,
                                         RetryPolicy retryPolicy) {
        // 路由切换、重试策略封装
        return new UriResourceRouteInvokerWrapper<>(invoker, retryPolicy, router).invoke();

    }

    /**
     * 发起调用(模拟故障，测试用)
     *
     * @param businessLogic     业务逻辑
     * @param router            自实现域名路由
     * @param mockFailureConfig 模拟故障配置
     * @param retrySuccessStats 重试结果统计
     * @param <Output>          出参范型
     * @return 业务出参
     */
    public static <Output> Output mockInvoke(SimpleUriResourceBusinessLogic<Output> businessLogic,
                                             Router<UriResource, Object, SimpleContext> router,
                                             Map<String, Integer> mockFailureConfig, AtomicLong retrySuccessStats) {
        final YopRouteConfigProvider routeConfigProvider = YopFileRouteConfigProvider.INSTANCE;
        final UriResourceRouteInvoker<Object, Output, SimpleContext, AnalyzedException> uriResourceRouteInvoker =
                new SimpleUriResourceInvoker<Output>(businessLogic, new SimpleContext(), routeConfigProvider) {

                    // 模拟域名故障
                    @Override
                    protected void beforeBusiness() throws IOException {
                        final String targetServer = getUriResource().getResource().toString();
                        if (RandomUtils.randomFailure(mockFailureConfig.get(targetServer))) {
                            throw new NoRouteToHostException("mock failure:" + targetServer);
                        }
                        super.beforeBusiness();
                    }

                    // 统计重试成功数
                    @Override
                    protected void afterBusiness() throws IOException {
                        super.afterBusiness();
                        if (getContext().getRetryCount() > 0) {
                            retrySuccessStats.addAndGet(1);
                        }
                    }
                };

        // 路由切换、重试策略封装
        return new UriResourceRouteInvokerWrapper<>(uriResourceRouteInvoker, new SimpleRetryPolicy(3), router).invoke();
    }

}
