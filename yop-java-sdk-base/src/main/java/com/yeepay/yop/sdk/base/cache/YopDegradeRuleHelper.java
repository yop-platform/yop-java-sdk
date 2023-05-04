/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.cache;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * title: 降级helper<br>
 * description: 描述，封装DegradeRuleManager<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/4/27
 */
public class YopDegradeRuleHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopDegradeRuleHelper.class);
    private volatile static boolean initialized = false;

    /**
     * 初始化降级配置
     *
     * @param serverRoots 域名列表
     * @param circuitBreakerConfig 降级配置
     */
    public static void initDegradeRule(List<URI> serverRoots, YopCircuitBreakerConfig circuitBreakerConfig) {
        if (initialized) {
            return;
        }

        if (CollectionUtils.isEmpty(serverRoots) || null == circuitBreakerConfig) {
            return;
        }

        synchronized (YopDegradeRuleHelper.class) {
            if (initialized) {
                return;
            }
            Set<DegradeRule> allRules = Sets.newHashSet();
            for (URI serverRoot : serverRoots) {
                if (null == serverRoot) {
                    continue;
                }

                String resource = serverRoot.toString();
                if (DegradeRuleManager.hasConfig(resource)) {
                    continue;
                }

                allRules.addAll(initDegradeRuleForResource(resource, circuitBreakerConfig));
            }
            if (CollectionUtils.isNotEmpty(allRules)) {
                DegradeRuleManager.loadRules(new ArrayList<>(allRules));
            }
            initialized = true;
            LOGGER.info("DegradeRule Inited, rules:{}", allRules);
        }
    }

    private static Set<DegradeRule> initDegradeRuleForResource(String resource, YopCircuitBreakerConfig circuitBreakerConfig) {
        Set<DegradeRule> result = Sets.newHashSet();
        final double errRatio = circuitBreakerConfig.getErrorRatioThreshold();
        DegradeRule errRatioRule = new DegradeRule(resource)
                .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
                .setCount(errRatio)
                .setMinRequestAmount(circuitBreakerConfig.getMinRequestCountThreshold())
                .setStatIntervalMs(circuitBreakerConfig.getMetricsWindowInMilliseconds())
                .setTimeWindow(circuitBreakerConfig.getSleepWindowInMilliseconds());
        result.add(errRatioRule);

        DegradeRule exCountRule = new DegradeRule(resource)
                .setGrade(CircuitBreakerStrategy.ERROR_COUNT.getType())
                .setCount(circuitBreakerConfig.getErrorCountThreshold())
                .setStatIntervalMs(circuitBreakerConfig.getMetricsWindowInMilliseconds())
                .setTimeWindow(circuitBreakerConfig.getSleepWindowInMilliseconds());
        result.add(exCountRule);
        return result;
    }

    /**
     * 添加降级配置
     *
     * @param serverRoot 域名
     * @param circuitBreakerConfig 域名降级配置
     */
    public static void addDegradeRule(URI serverRoot, YopCircuitBreakerConfig circuitBreakerConfig) {
        if (null == serverRoot) {
            return;
        }

        String resource = serverRoot.toString();
        if (DegradeRuleManager.hasConfig(resource)) {
            return;
        }

        Set<DegradeRule> rules = Sets.newHashSet();
        final double errRatio = ((double) circuitBreakerConfig.getErrorRatioThreshold()) / 100.00;
        DegradeRule errRatioRule = new DegradeRule(resource)
                .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
                .setCount(errRatio)
                .setMinRequestAmount(circuitBreakerConfig.getMinRequestCountThreshold())
                .setStatIntervalMs(circuitBreakerConfig.getMetricsWindowInMilliseconds())
                .setTimeWindow(circuitBreakerConfig.getSleepWindowInMilliseconds());
        rules.add(errRatioRule);

        DegradeRule exCountRule = new DegradeRule(resource)
                .setGrade(CircuitBreakerStrategy.ERROR_COUNT.getType())
                .setCount(circuitBreakerConfig.getErrorCountThreshold())
                .setStatIntervalMs(circuitBreakerConfig.getMetricsWindowInMilliseconds())
                .setTimeWindow(circuitBreakerConfig.getSleepWindowInMilliseconds());
        rules.add(exCountRule);
        DegradeRuleManager.setRulesForResource(resource, rules);
        LOGGER.info("DegradeRule Added, rules:{}", rules);
    }

}
