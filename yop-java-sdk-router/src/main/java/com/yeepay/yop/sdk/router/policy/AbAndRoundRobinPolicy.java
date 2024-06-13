/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.policy;

import com.yeepay.yop.sdk.invoke.model.Resource;
import com.yeepay.yop.sdk.invoke.model.RouterParams;
import com.yeepay.yop.sdk.invoke.model.SimpleResource;
import com.yeepay.yop.sdk.router.sentinel.YopSentinelMetricsHelper;

import java.util.Collections;
import java.util.List;

/**
 * title: 主-备-主-备-主-备……如此循环<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/1
 */
public class AbAndRoundRobinPolicy extends BaseRouterPolicy {

    @Override
    public Resource select(RouterParams params) {
        validateParams(params);
        final List<String> availableResources = params.getAvailableResources(),
                invokedResources = null == params.getInvokedResources()
                        ? Collections.emptyList() : params.getInvokedResources();
        if (invokedResources.size() < availableResources.size()) {
            return new SimpleResource(availableResources.get(invokedResources.size()));
        }

        final String lastInvokedResource = invokedResources.get(invokedResources.size() - 1);
        final int i = availableResources.indexOf(lastInvokedResource);
        if (i < (availableResources.size() - 1)) {
            return YopSentinelMetricsHelper.findCurrentBlockResource(availableResources.get(i + 1));
        } else {
            return YopSentinelMetricsHelper.findCurrentBlockResource(availableResources.get(0));
        }
    }
}
