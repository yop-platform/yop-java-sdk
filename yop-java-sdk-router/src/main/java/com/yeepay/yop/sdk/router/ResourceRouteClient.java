/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router;

import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.exception.YopHostBlockException;
import com.yeepay.yop.sdk.exception.YopHttpException;
import com.yeepay.yop.sdk.exception.YopUnknownException;
import com.yeepay.yop.sdk.invoke.*;
import com.yeepay.yop.sdk.invoke.model.*;
import com.yeepay.yop.sdk.router.config.YopRouteConfig;
import com.yeepay.yop.sdk.router.config.YopRouteConfigProvider;
import com.yeepay.yop.sdk.router.config.YopRouteConfigProviderRegistry;
import com.yeepay.yop.sdk.router.policy.RouterPolicyFactory;
import com.yeepay.yop.sdk.router.sentinel.YopDegradeRuleHelper;
import com.yeepay.yop.sdk.router.sentinel.YopSph;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.Entry;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.Tracer;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * title: 资源路由客户端<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/8
 */
public class ResourceRouteClient {

    private RetryPolicy retryPolicy;
    private YopRouteConfigProvider routeConfigProvider;
    private Router<Resource, Object, SimpleContext> router;

    public ResourceRouteClient(List<String> targetResources) {
        this(targetResources, RouterPolicyFactory.get(YopRouterConstants.ROUTER_POLICY_DEFAULT));
    }

    public ResourceRouteClient(List<String> targetResources, RouterPolicy routerPolicy) {
        this(targetResources, routerPolicy, SimpleUriRetryPolicy.singleton());
    }

    public ResourceRouteClient(List<String> targetResources, RouterPolicy routerPolicy, RetryPolicy retryPolicy) {
        this(YopRouteConfigProviderRegistry.getProvider(),
                new ResourceRouter<>(targetResources, routerPolicy), retryPolicy);
    }

    public ResourceRouteClient(YopRouteConfigProvider routeConfigProvider,
                               Router<Resource, Object, SimpleContext> router, RetryPolicy retryPolicy) {
        this.routeConfigProvider = routeConfigProvider;
        this.router = router;
        this.retryPolicy = retryPolicy;
    }

    /**
     * 发起路由调用
     *
     * @param invocation 业务逻辑
     * @param <Output>      出参范型
     * @return 业务出参
     */
    public <Output> Output route(ResourceInvocation<Output> invocation) {
        return route(invocation, new SimpleContext());
    }

    /**
     * 发起路由调用
     *
     * @param invocation 业务逻辑
     * @param <Output>      出参范型
     * @return 业务出参
     */
    public <Output> Output route(ResourceInvocation<Output> invocation, SimpleContext context) {
        // 业务处理、熔断操作、异常分析封装
        ResourceRouteInvoker<Object, Output, SimpleContext, AnalyzedException> resourceRouteInvoker
                = new ResourceInvoker<>(invocation, context, this.routeConfigProvider);
        // 路由切换、重试策略封装
        return new ResourceRouteInvokerWrapper<>(resourceRouteInvoker, retryPolicy, router).invoke();
    }

    public static class ResourceInvoker<Output>
            extends AbstractResourceRouteInvoker<Object, Output, SimpleContext, AnalyzedException> {

        private final ResourceInvocation<Output> invocation;

        private final YopRouteConfigProvider routeConfigProvider;

        public ResourceInvoker(ResourceInvocation<Output> invocation,
                                        SimpleContext context,
                                        YopRouteConfigProvider routeConfigProvider) {
            this.invocation = invocation;
            this.routeConfigProvider = routeConfigProvider;
            setContext(context);
        }

        @Override
        public Output invoke() {
            final Resource resource = getResource();
            Entry entry = null;
            boolean successInvoked = false;
            try {
                final YopRouteConfig routeConfig = findRouteConfig(resource.getResourceKey());
                final String sentinelResourceKey = resource instanceof BlockResource
                        ? ((BlockResource) resource).getBlockResourceKey() : resource.getResourceKey();
                YopDegradeRuleHelper.addDegradeRule(sentinelResourceKey,
                        routeConfig.getCircuitBreakerConfig());
                entry = YopSph.getInstance().entry(sentinelResourceKey);
                final Output output = doInvoke(resource);
                successInvoked = true;
                return output;
            } catch (YopClientException | YopHttpException | YopUnknownException ex) {
                throw ex;
            } catch (Throwable ex) {
                if (BlockException.isBlockException(ex)) {
                    final YopHostBlockException hostBlockException = new YopHostBlockException("ServerRoot Blocked, ex:", ex);
                    addException(getExceptionAnalyzer().analyze(hostBlockException));
                    throw hostBlockException;
                }
                throw new YopUnknownException("UnExpected Error, ", ex);
            } finally {
                if (null != entry) {
                    final AnalyzedException lastException = getLastException();
                    if (!successInvoked && null != lastException && lastException.isNeedDegrade()) {
                        Tracer.trace(lastException.getException());
                    }
                    entry.exit();
                }
            }
        }

        @Override
        public ExceptionAnalyzer<AnalyzedException> getExceptionAnalyzer() {
            Set<String> excludeExceptions = Collections.emptySet();
            Set<String> retryExceptions = Collections.emptySet();
            final Resource resource = getResource();
            final YopRouteConfig routeConfig = findRouteConfig(resource.getResourceKey());
            if (null != routeConfig) {
                if (null != routeConfig.getCircuitBreakerConfig()
                        && null != routeConfig.getCircuitBreakerConfig().getExcludeExceptions()) {
                    excludeExceptions = routeConfig.getCircuitBreakerConfig().getExcludeExceptions();
                }
                if (null != routeConfig.getRetryExceptions()) {
                    retryExceptions = routeConfig.getRetryExceptions();
                }
            }
            return SimpleCustomExceptionAnalyzer.from(excludeExceptions, retryExceptions);
        }

        private YopRouteConfig findRouteConfig(String resourceKey) {
            // 指定配置
            YopRouteConfig routeConfig = routeConfigProvider.getRouteConfig(resourceKey);
            // 默认配置
            if (null == routeConfig) {
                routeConfig = routeConfigProvider.getRouteConfig();
            }
            // 兜底配置
            return null == routeConfig ? YopRouteConfig.DEFAULT_CONFIG : routeConfig;
        }

        private Output doInvoke(Resource resource){
            Throwable throwable = null;
            try {
                beforeInvoke();
                final Output result = invocation.doInvoke(resource, getContext());
                afterInvoke();
                return result;
            } catch (YopClientException clientError) {//客户端异常&业务异常
                throwable = clientError;
                throw clientError;
            } catch (YopHttpException httpException) {//HTTP调用异常
                throwable = httpException;
                throw httpException;
            } catch (Throwable ex) {// 非预期异常
                throwable = ex;
                throw new YopUnknownException("UnExpected Error, ", ex);
            } finally {
                if (null != throwable) {
                    addException(getExceptionAnalyzer().analyze(throwable));
                }
            }
        }

        protected void beforeInvoke() throws IOException {

        }

        protected void afterInvoke() throws IOException {

        }
    }

    public static class ResourceRouter<Context> implements Router<Resource, Object, Context> {

        private final String resourceGroup;
        private final List<String> availableResources;
        private final RouterPolicy routerPolicy;

        public ResourceRouter(List<String> availableResources, RouterPolicy routerPolicy) {
            this(UUID.randomUUID().toString(), availableResources, routerPolicy);
        }

        public ResourceRouter(String resourceGroup, List<String> availableResources, RouterPolicy routerPolicy) {
            this.resourceGroup = resourceGroup;
            if (CollectionUtils.isEmpty(availableResources)) {
                throw new YopClientException("availableResources is empty");
            }
            List<String> targetResources;
            if (routerPolicy instanceof RandomRouterPolicy) {
                targetResources = ((RandomRouterPolicy) routerPolicy).shuffle(availableResources);
            } else {
                targetResources = availableResources;
            }
            this.availableResources = targetResources;
            this.routerPolicy = routerPolicy;
        }

        @Override
        public Resource route(Object o, Context context, Object... args) {
            List<String> invokedResources = null == args[0] ? Collections.emptyList()
                    : ((List<?>) args[0]).stream().map(Object::toString).collect(Collectors.toList());

            return this.routerPolicy.select(new SimpleRouterParams(this.resourceGroup,
                    this.availableResources, invokedResources));
        }
    }

}
