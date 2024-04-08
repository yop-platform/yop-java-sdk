/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

import com.yeepay.yop.sdk.invoke.model.AnalyzedException;
import com.yeepay.yop.sdk.invoke.model.ExceptionAnalyzer;
import com.yeepay.yop.sdk.invoke.model.Resource;

import java.util.LinkedList;
import java.util.List;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/8
 */
public abstract class AbstractResourceRouteInvoker<Input, Output, Context, Exception extends AnalyzedException>
        implements ResourceRouteInvoker<Input, Output, Context, Exception> {

    private Resource resource;
    private Input input;
    private Context context;
    private List<Exception> exceptions = new LinkedList<>();
    private Exception lastException;
    private ExceptionAnalyzer<Exception> exceptionAnalyzer;

    @Override
    public Resource getResource() {
        return resource;
    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
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
