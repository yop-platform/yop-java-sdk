/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.exception.YopUnknownException;
import com.yeepay.yop.sdk.invoke.model.AnalyzedException;
import com.yeepay.yop.sdk.invoke.model.Resource;
import com.yeepay.yop.sdk.invoke.model.RetryContext;
import com.yeepay.yop.sdk.invoke.model.RetryPolicy;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * title: 资源路由调用器封装<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/8
 */
public class ResourceRouteInvokerWrapper<Input, Output, Context extends RetryContext, Policy extends RetryPolicy,
        Exception extends AnalyzedException>
        implements Invoker<Input, Output, Context, Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRouteInvokerWrapper.class);

    private ResourceRouteInvoker<Input, Output, Context, Exception> invoker;

    private Policy retryPolicy;

    private Router<Resource, Input, Context> router;

    public ResourceRouteInvokerWrapper(ResourceRouteInvoker<Input, Output, Context, Exception> invoker,
                                       Policy retryPolicy,
                                       Router<Resource, Input, Context> router) {
        this.invoker = invoker;
        this.retryPolicy = retryPolicy;
        this.router = router;
    }

    @Override
    public Output invoke() {
        final long start = System.currentTimeMillis();
        List<String> invokedResources = Lists.newArrayList();
        Resource lastInvokedResource = null;
        Throwable currentEx;
        boolean needRetry;
        do {
            try {
                lastInvokedResource = router.route(getInput(), getContext(), invokedResources);
                invoker.setResource(lastInvokedResource);
                final Output result = invoker.invoke();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Success ServerRoot, {}, elapsed:{}, retryCount:{}", lastInvokedResource,
                            System.currentTimeMillis() - start, getContext().retryCount());
                }
                return result;
            } catch (Throwable throwable) {
                currentEx = throwable;
                // 路由异常，客户端配置问题
                if (null == lastInvokedResource || null == lastInvokedResource.getResourceKey()) {
                    throw new YopClientException("Config Error, No RouteResource Found");
                }

                // 客户端异常、业务异常，直接抛给上层
                if (throwable instanceof YopClientException) {
                    throw (YopClientException) throwable;
                }

                // 其他已分析异常
                final Exception analyzedException = getLastException();
                if (null == analyzedException) {
                    throw handleUnExpectedError(currentEx);
                }

                // 重试准备
                needRetry = analyzedException.isNeedRetry()
                        && null != retryPolicy
                        && retryPolicy.allowRetry(this);
                if (needRetry) {
                    invokedResources.add(lastInvokedResource.getResourceKey());
                    if (!analyzedException.isBlocked()) {
                        getContext().markRetried(1);
                    }
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Fail ServerRoot, {}, exDetail:{}, elapsed:{}, needRetry:{}", lastInvokedResource,
                            ExceptionUtils.getMessage(currentEx), System.currentTimeMillis() - start, needRetry);
                }
            }
        } while (needRetry);

        // 非预期异常处理
        throw handleUnExpectedError(currentEx);

    }

    private RuntimeException handleUnExpectedError(Throwable ex) {
        if (ex instanceof YopUnknownException) {
            return (YopUnknownException) ex;
        }
        return new YopUnknownException("UnExpected Error, ", ex);
    }

    @Override
    public Input getInput() {
        return invoker.getInput();
    }

    @Override
    public void setInput(Input input) {
        invoker.setInput(input);
    }

    @Override
    public void setContext(Context context) {
        invoker.setContext(context);
    }

    @Override
    public Context getContext() {
        return invoker.getContext();
    }

    @Override
    public List<Exception> getExceptions() {
        return invoker.getExceptions();
    }

    @Override
    public void addException(Exception exception) {
        invoker.addException(exception);
    }

    @Override
    public Exception getLastException() {
        return invoker.getLastException();
    }
}
