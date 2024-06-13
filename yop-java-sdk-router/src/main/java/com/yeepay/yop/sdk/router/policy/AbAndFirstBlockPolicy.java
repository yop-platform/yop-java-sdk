/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePa~y)
 */
package com.yeepay.yop.sdk.router.policy;

import com.yeepay.yop.sdk.invoke.RandomRouterPolicy;
import com.yeepay.yop.sdk.invoke.model.BlockResource;
import com.yeepay.yop.sdk.invoke.model.Resource;
import com.yeepay.yop.sdk.invoke.model.RouterParams;
import com.yeepay.yop.sdk.invoke.model.SimpleResource;
import com.yeepay.yop.sdk.router.sentinel.YopSentinelMetricsHelper;
import com.yeepay.yop.sdk.utils.RandomUtils;

import java.util.Collections;
import java.util.List;

/**
 * title: 主->备->最早熔断<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/3/29
 */
public class AbAndFirstBlockPolicy extends BaseRouterPolicy implements RandomRouterPolicy {

    @Override
    public Resource select(RouterParams params) {
        validateParams(params);
        final List<String> availableResources = params.getAvailableResources(),
                invokedResources = null == params.getInvokedResources()
                        ? Collections.emptyList() : params.getInvokedResources();

        if (invokedResources.isEmpty()) {
            return new SimpleResource(params.getAvailableResources().get(0));
        }

        for (String availableResource : availableResources) {
            if (!invokedResources.contains(availableResource)) {
                return new SimpleResource(availableResource);
            }
        }

        final BlockResource firstBlockResourceByGroup = YopSentinelMetricsHelper.findFirstBlockResourceByGroup(params.getResourceGroup());
        // 熔断列表为空(说明其他线程已半开成功)，选主域名即可
        if (null == firstBlockResourceByGroup) {
            final String mainResource = availableResources.get(0);
            return YopSentinelMetricsHelper.findCurrentBlockResource(mainResource);
        }
        return firstBlockResourceByGroup;
    }

    @Override
    public List<String> shuffle(List<String> originResources) {
        // 随机选主
        return RandomUtils.randomList(originResources);
    }
}
