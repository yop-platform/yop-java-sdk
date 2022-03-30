package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.exception.YopClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * title: 单例holder<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/2/2 15:21
 */
public class Holder<V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Holder.class);

    private final FutureTask<V> futureTask;

    public Holder(Callable<V> initCallable) {
        this.futureTask = new FutureTask<V>(initCallable);
    }

    public V getValue() {
        futureTask.run();
        try {
            return futureTask.get();
        } catch (InterruptedException e) {
            throw new YopClientException("InterruptedException occurred when get value from SingletonHolder.");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new YopClientException("UnexpectedException occurred when get value from SingletonHolder.", cause);
        }
    }

}
