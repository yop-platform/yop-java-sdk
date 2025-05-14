package com.yeepay.yop.sdk.circuit;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerRuleConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Circuit breaker manager that replaces Sentinel's DegradeRuleManager
 */
public class YopCircuitBreakerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopCircuitBreakerManager.class);
    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static volatile boolean initialized = false;
    
    // Store circuit breaker rules by resource
    private static final Map<String, Set<YopCircuitBreaker>> circuitBreakers = new ConcurrentHashMap<>();
    
    // Store resource state information
    private static final Map<String, ResourceState> resourceStates = new ConcurrentHashMap<>();
    
    /**
     * Initialize circuit breaker rules for the given resources
     *
     * @param serverRoots List of server root URIs
     * @param circuitBreakerConfig Circuit breaker configuration
     */
    public static void initCircuitBreakers(List<URI> serverRoots, YopCircuitBreakerConfig circuitBreakerConfig) {
        if (initialized) {
            return;
        }

        synchronized (YopCircuitBreakerManager.class) {
            if (initialized) {
                return;
            }

            if (CollectionUtils.isEmpty(serverRoots) || null == circuitBreakerConfig
                    || CollectionUtils.isEmpty(circuitBreakerConfig.getRules())) {
                LOGGER.warn("Empty Circuit Breaker Rules, Please Check Your Config And Try Again");
                initialized = true;
                return;
            }

            Set<YopCircuitBreaker> allRules = Sets.newHashSet();
            for (URI serverRoot : serverRoots) {
                if (null == serverRoot) {
                    continue;
                }

                String resource = serverRoot.toString();
                if (hasConfig(resource)) {
                    continue;
                }

                allRules.addAll(initCircuitBreakersForResource(resource, circuitBreakerConfig));
            }
            
            if (CollectionUtils.isNotEmpty(allRules)) {
                loadRules(new ArrayList<>(allRules));
            }
            
            initialized = true;
            LOGGER.info("Circuit Breaker Rules Initialized, rules:{}", allRules);
        }
    }

    /**
     * Create circuit breaker rules for a specific resource
     */
    private static Set<YopCircuitBreaker> initCircuitBreakersForResource(String resource, YopCircuitBreakerConfig circuitBreakerConfig) {
        Set<YopCircuitBreaker> result = Sets.newHashSet();
        if (CollectionUtils.isEmpty(circuitBreakerConfig.getRules())) {
            return result;
        }
        
        for (YopCircuitBreakerRuleConfig configRule : circuitBreakerConfig.getRules()) {
            YopCircuitBreaker circuitBreaker = new YopCircuitBreaker(resource)
                    .setGrade(configRule.getGrade())
                    .setCount(configRule.getCount())
                    .setStatIntervalMs(configRule.getStatIntervalMs())
                    .setTimeWindow(configRule.getTimeWindow());
                    
            if (circuitBreaker.getGrade() != YopCircuitBreakerStrategy.ERROR_COUNT.getType()) {
                circuitBreaker.setMinRequestAmount(configRule.getMinRequestAmount());
            } else {
                circuitBreaker.setMinRequestAmount(Double.valueOf(configRule.getCount()).intValue());
            }
            
            result.add(circuitBreaker);
        }
        
        return result;
    }

    /**
     * Load a list of circuit breaker rules
     */
    public static void loadRules(List<YopCircuitBreaker> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }
        
        Map<String, Set<YopCircuitBreaker>> newRuleMap = Maps.newHashMap();
        for (YopCircuitBreaker rule : rules) {
            if (!newRuleMap.containsKey(rule.getResource())) {
                newRuleMap.put(rule.getResource(), Sets.newHashSet());
            }
            newRuleMap.get(rule.getResource()).add(rule);
        }
        
        rwl.writeLock().lock();
        try {
            circuitBreakers.clear();
            circuitBreakers.putAll(newRuleMap);
            for (String resource : circuitBreakers.keySet()) {
                resourceStates.computeIfAbsent(resource, k -> new ResourceState());
            }
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Check if configuration exists for the resource
     */
    public static boolean hasConfig(String resource) {
        rwl.readLock().lock();
        try {
            return circuitBreakers.containsKey(resource);
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Add circuit breaker rule for a resource
     */
    public static boolean addCircuitBreaker(URI serverRoot, YopCircuitBreakerConfig circuitBreakerConfig) {
        if (null == serverRoot) {
            return false;
        }
        return addCircuitBreaker(serverRoot.toString(), circuitBreakerConfig);
    }

    /**
     * Add circuit breaker rule for a resource
     */
    public static boolean addCircuitBreaker(String resource, YopCircuitBreakerConfig circuitBreakerConfig) {
        if (null == resource) {
            return false;
        }

        if (hasConfig(resource)) {
            return false;
        }

        Set<YopCircuitBreaker> rules = initCircuitBreakersForResource(resource, circuitBreakerConfig);
        boolean ruleAdded = updateRulesForResource(resource, rules, false);
        if (YopConstants.SDK_DEBUG && ruleAdded) {
            LOGGER.info("Circuit Breaker Added, rules:{}", rules);
        }
        return ruleAdded;
    }

    /**
     * Update circuit breaker rule for a resource
     */
    public static boolean updateCircuitBreaker(String resource, YopCircuitBreakerConfig circuitBreakerConfig) {
        if (null == resource) {
            return false;
        }

        Set<YopCircuitBreaker> rules = initCircuitBreakersForResource(resource, circuitBreakerConfig);
        boolean ruleUpdated = updateRulesForResource(resource, rules, true);
        if (YopConstants.SDK_DEBUG && ruleUpdated) {
            LOGGER.info("Circuit Breaker Updated, rules:{}", rules);
        }
        return ruleUpdated;
    }

    /**
     * Update rules for a resource
     */
    private static boolean updateRulesForResource(String resource, Set<YopCircuitBreaker> rules, boolean forceUpdate) {
        if (null == resource) {
            return false;
        }
        
        rwl.writeLock().lock();
        try {
            if (circuitBreakers.containsKey(resource) && !forceUpdate) {
                return false;
            }
            
            if (CollectionUtils.isEmpty(rules)) {
                circuitBreakers.remove(resource);
                resourceStates.remove(resource);
            } else {
                circuitBreakers.put(resource, rules);
                resourceStates.computeIfAbsent(resource, k -> new ResourceState());
            }
            
            return true;
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Remove circuit breaker rule for a resource
     */
    public static boolean removeCircuitBreaker(String resource) {
        if (null == resource) {
            return false;
        }

        if (!hasConfig(resource)) {
            return true;
        }

        final boolean ruleRemoved = updateRulesForResource(resource, null, true);
        if (YopConstants.SDK_DEBUG && ruleRemoved) {
            LOGGER.info("Circuit Breaker Removed, resource:{}", resource);
        }
        return ruleRemoved;
    }
    
    /**
     * Try to acquire entry for the resource, may throw CircuitBreakerException
     */
    public static CircuitBreakerEntry entry(String resource) throws CircuitBreakerException {
        Set<YopCircuitBreaker> breakersForResource = null;
        
        rwl.readLock().lock();
        try {
            breakersForResource = circuitBreakers.get(resource);
        } finally {
            rwl.readLock().unlock();
        }
        
        if (breakersForResource == null || breakersForResource.isEmpty()) {
            return new CircuitBreakerEntry(resource);
        }
        
        ResourceState state = resourceStates.computeIfAbsent(resource, k -> new ResourceState());
        state.incrementAndGetTotal();
        
        // Check if any circuit breaker is triggered
        for (YopCircuitBreaker breaker : breakersForResource) {
            if (breaker.isTripped(state)) {
                state.incrementAndGetBlocked();
                throw new CircuitBreakerException(resource, breaker);
            }
        }
        
        return new CircuitBreakerEntry(resource, state);
    }
    
    /**
     * Record an error for the resource
     */
    public static void markError(String resource) {
        ResourceState state = resourceStates.get(resource);
        if (state != null) {
            state.incrementAndGetException();
            
            // Notify any circuit breaker state listeners
            Set<YopCircuitBreaker> breakersForResource = circuitBreakers.get(resource);
            if (breakersForResource != null) {
                for (YopCircuitBreaker breaker : breakersForResource) {
                    breaker.onError(state);
                }
            }
        }
    }
    
    /**
     * Record RT (response time) for the resource
     */
    public static void markResponseTime(String resource, long rt) {
        ResourceState state = resourceStates.get(resource);
        if (state != null) {
            state.addRt(rt);
            
            // Notify any circuit breaker state listeners
            Set<YopCircuitBreaker> breakersForResource = circuitBreakers.get(resource);
            if (breakersForResource != null) {
                for (YopCircuitBreaker breaker : breakersForResource) {
                    breaker.onResponseTime(state, rt);
                }
            }
        }
    }
} 