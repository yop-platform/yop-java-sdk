/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.cache;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.config.provider.file.YopHystrixConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * title: 降级wrapper<br>
 * description: 描述，封装DegradeRuleManager<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/4/27
 */
public class YopDegradeRuleWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopDegradeRuleWrapper.class);
    private volatile static boolean initialized = false;

    /**
     * 初始化降级配置
     *
     * @param serverRoots 域名列表
     * @param hystrixConfig 降级配置
     */
    public synchronized static void initDegradeRule(List<URI> serverRoots, YopHystrixConfig hystrixConfig) {
        if (initialized) {
            return;
        }

        if (CollectionUtils.isEmpty(serverRoots) || null == hystrixConfig) {
            return;
        }

        Set<DegradeRule> allRules = Sets.newHashSet();
        for (URI serverRoot : serverRoots) {
            if (null == serverRoot) {
                continue;
            }

            String resource = StringUtils.substringBefore(serverRoot.toString(), "?");
            if (DegradeRuleManager.hasConfig(resource)) {
                continue;
            }

            allRules.addAll(initDegradeRuleForResource(resource, hystrixConfig));
        }
        if (CollectionUtils.isNotEmpty(allRules)) {
            DegradeRuleManager.loadRules(new ArrayList<>(allRules));
        }
        initialized = true;
        LOGGER.info("DegradeRule Inited, rules:{}", allRules);
    }

    private static Set<DegradeRule> initDegradeRuleForResource(String resource, YopHystrixConfig hystrixConfig) {
        Set<DegradeRule> result = Sets.newHashSet();
        final double errRatio = ((double) hystrixConfig.getCircuitBreakerErrorThresholdPercentage()) / 100.00;
        DegradeRule errRatioRule = new DegradeRule(resource)
                .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
                .setCount(errRatio)
                .setMinRequestAmount(hystrixConfig.getCircuitBreakerRequestVolumeThreshold())
                .setStatIntervalMs(hystrixConfig.getCbMetricsRollingStatsTimeInMilliseconds())
                .setTimeWindow(hystrixConfig.getCircuitBreakerSleepWindowInMilliseconds());
        result.add(errRatioRule);

        DegradeRule exCountRule = new DegradeRule(resource)
                .setGrade(CircuitBreakerStrategy.ERROR_COUNT.getType())
                .setCount(hystrixConfig.getCircuitBreakerErrorThresholdCount())
                .setMinRequestAmount(hystrixConfig.getCircuitBreakerRequestVolumeThreshold())
                .setStatIntervalMs(hystrixConfig.getCbMetricsRollingStatsTimeInMilliseconds())
                .setTimeWindow(hystrixConfig.getCircuitBreakerSleepWindowInMilliseconds());
        result.add(exCountRule);
        return result;
    }

    /**
     * 添加降级配置
     *
     * @param serverRoot
     * @param hystrixConfig
     */
    public static void addDegradeRule(URI serverRoot, YopHystrixConfig hystrixConfig) {
        if (null == serverRoot) {
            return;
        }

        String resource = StringUtils.substringBefore(serverRoot.toString(), "?");
        if (DegradeRuleManager.hasConfig(resource)) {
            return;
        }

        Set<DegradeRule> rules = Sets.newHashSet();
        final double errRatio = ((double) hystrixConfig.getCircuitBreakerErrorThresholdPercentage()) / 100.00;
        DegradeRule errRatioRule = new DegradeRule(resource)
                .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
                .setCount(errRatio)
                .setMinRequestAmount(hystrixConfig.getCircuitBreakerRequestVolumeThreshold())
                .setStatIntervalMs(hystrixConfig.getCbMetricsRollingStatsTimeInMilliseconds())
                .setTimeWindow(hystrixConfig.getCircuitBreakerSleepWindowInMilliseconds());
        rules.add(errRatioRule);

        DegradeRule exCountRule = new DegradeRule(resource)
                .setGrade(CircuitBreakerStrategy.ERROR_COUNT.getType())
                .setCount(hystrixConfig.getCircuitBreakerErrorThresholdCount())
                .setMinRequestAmount(hystrixConfig.getCircuitBreakerRequestVolumeThreshold())
                .setStatIntervalMs(hystrixConfig.getCbMetricsRollingStatsTimeInMilliseconds())
                .setTimeWindow(hystrixConfig.getCircuitBreakerSleepWindowInMilliseconds());
        rules.add(exCountRule);
        DegradeRuleManager.setRulesForResource(resource, rules);
        LOGGER.info("DegradeRule Added, rules:{}", rules);
    }

}
