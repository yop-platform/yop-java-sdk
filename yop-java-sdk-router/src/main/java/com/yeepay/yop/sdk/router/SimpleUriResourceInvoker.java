/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router;

import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.exception.YopHostBlockException;
import com.yeepay.yop.sdk.exception.YopHttpException;
import com.yeepay.yop.sdk.exception.YopUnknownException;
import com.yeepay.yop.sdk.invoke.AbstractUriResourceRouteInvoker;
import com.yeepay.yop.sdk.invoke.model.AnalyzedException;
import com.yeepay.yop.sdk.invoke.model.ExceptionAnalyzer;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.router.config.YopRouteConfig;
import com.yeepay.yop.sdk.router.config.YopRouteConfigProvider;
import com.yeepay.yop.sdk.router.sentinel.YopDegradeRuleHelper;
import com.yeepay.yop.sdk.router.sentinel.YopSph;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.Entry;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.Tracer;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * title: 基于Function调用器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/2/2
 */
public class SimpleUriResourceInvoker<Output>
        extends AbstractUriResourceRouteInvoker<Object, Output, SimpleContext, AnalyzedException> {

    private final SimpleUriResourceBusinessLogic<Output> businessLogic;

    private final YopRouteConfigProvider routeConfigProvider;

    public SimpleUriResourceInvoker(SimpleUriResourceBusinessLogic<Output> businessLogic,
                                    SimpleContext context,
                                    YopRouteConfigProvider routeConfigProvider) {
        this.businessLogic = businessLogic;
        this.routeConfigProvider = routeConfigProvider;
        setContext(context);
    }

    @Override
    public Output invoke() {
        final UriResource uriResource = getUriResource();
        Entry entry = null;
        boolean successInvoked = false;
        try {
            final String resource = uriResource.computeResourceKey();
            final YopRouteConfig routeConfig = findRouteConfig(uriResource.getResource());
            YopDegradeRuleHelper.addDegradeRule(resource, routeConfig.getCircuitBreakerConfig());
            entry = YopSph.getInstance().entry(resource);
            final Output output = doBusiness(uriResource);
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
        final UriResource uriResource = getUriResource();
        final YopRouteConfig routeConfig = findRouteConfig(uriResource.getResource());
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

    private YopRouteConfig findRouteConfig(URI uri) {
        String configKey = StringUtils.strip(uri.getHost().replaceAll("[^a-zA-Z0-9]", "_")
                + (uri.getPort() > 0 ? uri.getPort() : ""), "_");
        // 指定配置
        YopRouteConfig routeConfig = routeConfigProvider.getRouteConfig(configKey);
        // 默认配置
        if (null == routeConfig) {
            routeConfig = routeConfigProvider.getRouteConfig();
        }
        // 兜底配置
        return null == routeConfig ? YopRouteConfig.DEFAULT_CONFIG : routeConfig;
    }

    private Output doBusiness(UriResource targetServer){
        Throwable throwable = null;
        try {
            beforeBusiness();
            final Output result = businessLogic.doBusiness(targetServer, getContext());
            afterBusiness();
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

    protected void beforeBusiness() throws IOException {

    }

    protected void afterBusiness() throws IOException {

    }

    protected void afterFinish() throws IOException {

    }
}
