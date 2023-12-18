/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

import com.yeepay.yop.sdk.invoke.model.AnalyzedException;
import com.yeepay.yop.sdk.invoke.model.ExceptionAnalyzer;
import com.yeepay.yop.sdk.invoke.model.UriResource;

import java.util.LinkedList;
import java.util.List;

/**
 * title: 基于UriResource的抽象路由实现类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/9
 */
public abstract class AbstractUriResourceRouteInvoker<Input, Output, Context, Exception extends AnalyzedException>
        implements UriResourceRouteInvoker<Input, Output, Context, Exception> {

    private UriResource uriResource;
    private Input input;
    private Context context;

    private boolean enableCircuitBreaker = false;

    private List<Exception> exceptions = new LinkedList<>();

    private Exception lastException;

    private ExceptionAnalyzer<Exception> exceptionAnalyzer;

    @Override
    public UriResource getUriResource() {
        return uriResource;
    }

    @Override
    public void setUriResource(UriResource uri) {
        this.uriResource = uri;
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public void setInput(Input input) {
        this.input = input;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public boolean isCircuitBreakerEnable() {
        return enableCircuitBreaker;
    }

    @Override
    public void enableCircuitBreaker() {
        this.enableCircuitBreaker = true;
    }

    @Override
    public void disableCircuitBreaker() {
        this.enableCircuitBreaker = false;
    }

    @Override
    public void addException(Exception exception) {
        this.exceptions.add(exception);
        this.lastException = exception;
    }

    @Override
    public List<Exception> getExceptions() {
        return exceptions;
    }

    @Override
    public Exception getLastException() {
        return lastException;
    }

    @Override
    public ExceptionAnalyzer<Exception> getExceptionAnalyzer() {
        return exceptionAnalyzer;
    }

    @Override
    public void setExceptionAnalyzer(ExceptionAnalyzer<Exception> exceptionAnalyzer) {
        this.exceptionAnalyzer = exceptionAnalyzer;
    }
}
