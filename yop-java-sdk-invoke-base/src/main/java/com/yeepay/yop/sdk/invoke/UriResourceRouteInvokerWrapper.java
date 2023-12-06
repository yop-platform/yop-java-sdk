/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.exception.YopUnknownException;
import com.yeepay.yop.sdk.invoke.model.AnalyzedException;
import com.yeepay.yop.sdk.invoke.model.RetryContext;
import com.yeepay.yop.sdk.invoke.model.RetryPolicy;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

/**
 * title: 基于UriResource路由的调用器实现<br>
 * description: 分析&封装异常、路由、重试等功能<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/6
 */
public class UriResourceRouteInvokerWrapper<Input, Output, Context extends RetryContext, Policy extends RetryPolicy,
        Exception extends AnalyzedException>
        implements Invoker<Input, Output, Context, Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UriResourceRouteInvokerWrapper.class);

    private UriResourceRouteInvoker<Input, Output, Context, Exception> invoker;

    private RetryPolicy retryPolicy;

    private Router<UriResource, Input, Context> uriRouter;

    public UriResourceRouteInvokerWrapper(UriResourceRouteInvoker<Input, Output, Context, Exception> invoker,
                                          RetryPolicy retryPolicy,
                                          Router<UriResource, Input, Context> uriRouter) {
        this.invoker = invoker;
        this.retryPolicy = retryPolicy;
        this.uriRouter = uriRouter;
    }

    @Override
    public Output invoke() {
        final long start = System.currentTimeMillis();
        List<URI> excludeServerRoots = Lists.newArrayList();
        UriResource lastServerRoot = null;
        Throwable currentEx;
        boolean needRetry;
        do {
            try {
                lastServerRoot = uriRouter.route(getInput(), getContext(), excludeServerRoots);
                invoker.setUriResource(lastServerRoot);
                final Output result = invoker.invoke();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Success ServerRoot, {}, elapsed:{}, retryCount:{}", lastServerRoot,
                            System.currentTimeMillis() - start, getContext().retryCount());
                }
                return result;
            } catch (Throwable throwable) {
                currentEx = throwable;
                // 路由异常，客户端配置问题
                if (null == lastServerRoot || null == lastServerRoot.getResource()) {
                    throw new YopClientException("Config Error, No ServerRoot Found");
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
                    excludeServerRoots.add(lastServerRoot.getResource());
                    if (!analyzedException.isBlocked()) {
                        getContext().markRetried(1);
                    }
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Fail ServerRoot, {}, exDetail:{}, elapsed:{}, needRetry:{}", lastServerRoot,
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
