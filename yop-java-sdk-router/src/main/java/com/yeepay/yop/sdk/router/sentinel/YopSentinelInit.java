/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.sentinel;

import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.init.InitFunc;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStateChangeObserver;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;

import java.util.ServiceLoader;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/3
 */
public class YopSentinelInit implements InitFunc {

    @Override
    public void init() throws Exception {
        ServiceLoader<CircuitBreakerStateChangeObserver> serviceLoader = ServiceLoader.load(CircuitBreakerStateChangeObserver .class);
        for (CircuitBreakerStateChangeObserver  item : serviceLoader) {
            EventObserverRegistry.getInstance().addStateChangeObserver(item.getClass().getCanonicalName(), item);
        }
    }
}
