/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.sentinel;

import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerRuleConfig;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();



    /**
     * 初始化降级配置
     *
     * @param circuitBreakerConfigMap 降级规则Map<资源标识，熔断规则列表>
     */
    public static void initDegradeRule(Map<String, YopCircuitBreakerConfig> circuitBreakerConfigMap) {
        if (initialized) {
            return;
        }

        synchronized (YopDegradeRuleHelper.class) {
            if (initialized) {
                return;
            }

            if (MapUtils.isEmpty(circuitBreakerConfigMap)) {
                LOGGER.warn("Empty DegradeRule, Please Check Your Config And Try Again");
                initialized = true;
                return;
            }

            Set<DegradeRule> allRules = Sets.newHashSet();
            circuitBreakerConfigMap.forEach((resource, circuitBreakerConfig) -> {
                if (StringUtils.isBlank(resource) || null == circuitBreakerConfig
                        || !circuitBreakerConfig.isEnable() || CollectionUtils.isEmpty(circuitBreakerConfig.getRules())
                        || DegradeRuleManager.hasConfig(resource)) {
                    return;
                }
                allRules.addAll(initDegradeRuleForResource(resource, circuitBreakerConfig));
            });

            if (CollectionUtils.isNotEmpty(allRules)) {
                DegradeRuleManager.loadRules(new ArrayList<>(allRules));
            }
            initialized = true;
            LOGGER.info("DegradeRule Inited, rules:{}", allRules);
        }
    }

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

        synchronized (YopDegradeRuleHelper.class) {
            if (initialized) {
                return;
            }

            if (CollectionUtils.isEmpty(serverRoots) || null == circuitBreakerConfig
                    || CollectionUtils.isEmpty(circuitBreakerConfig.getRules())) {
                LOGGER.warn("Empty DegradeRule, Please Check Your Config And Try Again");
                initialized = true;
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
        if (CollectionUtils.isEmpty(circuitBreakerConfig.getRules())) {
            return result;
        }
        for (YopCircuitBreakerRuleConfig configRule : circuitBreakerConfig.getRules()) {
            DegradeRule degradeRule = new DegradeRule(resource)
                    .setGrade(configRule.getGrade())
                    .setCount(configRule.getCount())
                    .setStatIntervalMs(configRule.getStatIntervalMs())
                    .setTimeWindow(configRule.getTimeWindow());
            if (CircuitBreakerStrategy.ERROR_COUNT.getType() != configRule.getGrade()) {
                degradeRule.setMinRequestAmount(configRule.getMinRequestAmount());
            } else {
                degradeRule.setMinRequestAmount(Double.valueOf(configRule.getCount()).intValue());
            }
            result.add(degradeRule);
        }
        return result;
    }

    /**
     * 添加降级配置
     *
     * @param serverRoot 域名
     * @param circuitBreakerConfig 域名降级配置
     */
    public static boolean addDegradeRule(URI serverRoot, YopCircuitBreakerConfig circuitBreakerConfig) {
        if (null == serverRoot) {
            return false;
        }
        return addDegradeRule(serverRoot.toString(), circuitBreakerConfig);
    }

    /**
     * 添加降级配置
     *
     * @param resource 资源名称
     * @param circuitBreakerConfig 域名降级配置
     */
    public static boolean addDegradeRule(String resource, YopCircuitBreakerConfig circuitBreakerConfig) {
        if (null == resource) {
            return false;
        }

        if (existsResource(resource)) {
            return false;
        }

        Set<DegradeRule> rules = initDegradeRuleForResource(resource, circuitBreakerConfig);
        boolean ruleAdded = updateRulesForResource(resource, rules, false);
        if (YopSentinelConstants.SDK_ROUTER_SENTINEL_DEBUG && ruleAdded) {
            LOGGER.info("DegradeRule Added, rules:{}", rules);
        }
        return ruleAdded;
    }

    /**
     * 更新降级配置
     *
     * @param resource 资源名称
     * @param circuitBreakerConfig 域名降级配置
     */
    public static boolean updateDegradeRule(String resource, YopCircuitBreakerConfig circuitBreakerConfig) {
        if (null == resource) {
            return false;
        }

        Set<DegradeRule> rules = initDegradeRuleForResource(resource, circuitBreakerConfig);
        boolean ruleUpdated = updateRulesForResource(resource, rules, true);
        if (YopSentinelConstants.SDK_ROUTER_SENTINEL_DEBUG && ruleUpdated) {
            LOGGER.info("DegradeRule Updated, rules:{}", rules);
        }
        return ruleUpdated;
    }

    private static boolean updateRulesForResource(String resource, Set<DegradeRule> rules, boolean forceUpdate) {
        if (null == resource) {
            return false;
        }
        Set<DegradeRule> updateRules = CollectionUtils.isNotEmpty(rules) ? rules : null;
        rwl.writeLock().lock();
        try {
            if (DegradeRuleManager.hasConfig(resource) && !forceUpdate) {
                return false;
            }
            return DegradeRuleManager.setRulesForResource(resource, updateRules);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    private static boolean existsResource(String resource) {
        rwl.readLock().lock();
        try {
            return DegradeRuleManager.hasConfig(resource);
        } finally {
            rwl.readLock().unlock();
        }
    }


    /**
     * 移除降级配置
     *
     * @param resource 资源名称
     */
    public static boolean removeDegradeRule(String resource) {
        if (null == resource) {
            return false;
        }

        if (!existsResource(resource)) {
            return true;
        }

        final boolean ruleRemoved = updateRulesForResource(resource, null, true);
        if (YopSentinelConstants.SDK_ROUTER_SENTINEL_DEBUG && ruleRemoved) {
            LOGGER.info("DegradeRule Removed, resource:{}", resource);
        }
        return ruleRemoved;
    }

}
