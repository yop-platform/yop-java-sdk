/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.metric;

import com.yeepay.yop.sdk.client.ClientReporter;
import com.yeepay.yop.sdk.client.metric.report.host.YopHostStatusChangePayload;
import com.yeepay.yop.sdk.client.metric.report.host.YopHostStatusChangeReport;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStateChangeObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * title: 资源熔断上报<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/3
 */
public class YopResourceBlockReportListener implements CircuitBreakerStateChangeObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopResourceBlockReportListener.class);

    @Override
    public void onStateChange(CircuitBreaker.State prevState, CircuitBreaker.State newState, DegradeRule rule, Double snapshotValue) {
        try {
            // 异步上报
            final UriResource uriResource = UriResource.parseResourceKey(rule.getResource());
            final URI serverRoot = uriResource.getResource();
            final String[] resourceGroupSplit = uriResource.parseResourceGroup();
            String provider = resourceGroupSplit[0], env = resourceGroupSplit[1];

            final YopHostStatusChangeReport report = new YopHostStatusChangeReport(
                    new YopHostStatusChangePayload(serverRoot.toString(), prevState.name(), newState.name(), rule.toString()));
            report.setProvider(provider);
            report.setEnv(env);
            ClientReporter.asyncReportToQueue(report);
        } catch (Exception e) {
            LOGGER.warn("UnexpectedError, ResourceBLockReport, rule:{}, prev:{}, current:{}", rule, prevState, newState, e);
        }
    }

}
