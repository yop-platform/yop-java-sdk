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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

/**
 * title: 基于uri路由的调用器实现<br>
 * description: 分析&封装异常、路由、重试等功能<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/6
 */
public class UriRouteInvokerWrapper<Input, Output, Context extends RetryContext, Policy extends RetryPolicy,
        Exception extends AnalyzedException>
        implements Invoker<Input, Output, Context, Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UriRouteInvokerWrapper.class);

    private UriRouteInvoker<Input, Output, Context, Exception> invoker;

    private RetryPolicy retryPolicy;

    private Router<URI, Input, Context> uriRouter;

    public UriRouteInvokerWrapper(UriRouteInvoker<Input, Output, Context, Exception> invoker,
                                  RetryPolicy retryPolicy,
                                  Router<URI, Input, Context> uriRouter) {
        this.invoker = invoker;
        this.retryPolicy = retryPolicy;
        this.uriRouter = uriRouter;
    }

    @Override
    public Output invoke() {
        final long start = System.currentTimeMillis();

        List<URI> excludeServerRoots = Lists.newArrayList();
        URI lastServerRoot = uriRouter.route(getInput(), getContext(), excludeServerRoots);

        while (!excludeServerRoots.contains(lastServerRoot)) {
            try {
                invoker.setUri(lastServerRoot);
                final Output result = invoker.invoke();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Success ServerRoot, {}, elapsed:{}, retryCount:{}", invoker.getUri(),
                            System.currentTimeMillis() - start, getContext().retryCount());
                }
                return result;
            } catch (java.lang.Exception throwable) {
                // 客户端异常、业务异常
                if (throwable instanceof YopClientException) {
                    throw throwable;
                }

                // 其他异常，须具体分析
                final Exception analyzedEx = invoker.getLastException();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Fail ServerRoot, {}, exDetail:{}, elapsed:{}", invoker.getUri(),
                            analyzedEx.getExDetail(), System.currentTimeMillis() - start);
                }

                // 可重试异常，仅当配置路由时触发
                if (analyzedEx.isNeedRetry()
                        && null != retryPolicy
                        && retryPolicy.allowRetry(invoker, throwable)) {
                    excludeServerRoots.add(lastServerRoot);
                    getContext().markRetried(1);

                    // 切换uri重试
                    lastServerRoot = uriRouter.route(getInput(), getContext(), excludeServerRoots);
                    continue;
                }

                // 封装非预期的服务端异常
                if (analyzedEx.isNeedDegrade()) {
                    handleUnExpectedError(throwable);
                }

                // 非预期的客户端异常
                throw new YopClientException("Client Error, ex:", throwable);
            }
        }

        // 如果所有域名均熔断，则用最早熔断域名兜底
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("All ServerRoots Unavailable, Last Try, tried:{}, last:{}",
                    excludeServerRoots, lastServerRoot);
        }
        invoker.setUri(lastServerRoot);
        invoker.disableCircuitBreaker();
        return invoker.invoke();
    }

    private void handleUnExpectedError(Throwable ex) {
        if (ex instanceof YopUnknownException) {
            throw (YopUnknownException) ex;
        }
        throw new YopUnknownException("UnExpected Error, ", ex);
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
