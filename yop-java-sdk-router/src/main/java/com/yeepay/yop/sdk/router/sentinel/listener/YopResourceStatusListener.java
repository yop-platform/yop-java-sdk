/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.sentinel.listener;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.router.sentinel.YopDegradeRuleHelper;
import com.yeepay.yop.sdk.router.sentinel.YopSentinelMetricsHelper;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStateChangeObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * title: 资源状态变更监听<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/3
 */
public class YopResourceStatusListener implements CircuitBreakerStateChangeObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopResourceStatusListener.class);

    private static final ThreadPoolExecutor BLOCKED_SWEEPER = new ThreadPoolExecutor(2, 20,
            3, TimeUnit.MINUTES, Queues.newLinkedBlockingQueue(1000),
            new ThreadFactoryBuilder().setNameFormat("yop-blocked-resource-sweeper-%d").setDaemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public void onStateChange(CircuitBreaker.State prevState, CircuitBreaker.State newState,
                              DegradeRule rule, Double snapshotValue) {

        try {
            final UriResource uriResource = UriResource.parseResourceKey(rule.getResource());
            final URI serverRoot = uriResource.getResource();
            LOGGER.info("ServerRoot Block State Changed, serverRoot:{}, old:{}, new:{}, rule:{}",
                    serverRoot, prevState, newState, rule);

            final String commonResourceKey = new UriResource(uriResource.getResourceGroup(), serverRoot).computeResourceKey();
            final boolean isBlockResource = UriResource.ResourceType.BLOCKED.equals(uriResource.getResourceType());
            if (newState.equals(CircuitBreaker.State.OPEN)) {
                YopSentinelMetricsHelper.onResourceBlocked(uriResource.getResourceGroup(), commonResourceKey, isBlockResource);
                if (isBlockResource) {
                    asyncDiscardOldServers(uriResource);
                }
            } else if (newState.equals(CircuitBreaker.State.CLOSED)) {
                YopSentinelMetricsHelper.onResourceAvailable(uriResource.getResourceGroup(), commonResourceKey);
            }
        } catch (Exception e) {
            LOGGER.warn("UnexpectedError, MonitorServerRoot ex:", e);
        }
    }

    // 异步清理过期资源
    private void asyncDiscardOldServers(UriResource uriResource) {
        BLOCKED_SWEEPER.submit(() -> {
            try {
                final String resource = uriResource.computeResourceKey();
                // 清理资源配置
                YopDegradeRuleHelper.removeDegradeRule(resource);
            } catch (Exception e) {
                LOGGER.warn("blocked sweeper failed, ex:", e);
            }
        });
    }
}
