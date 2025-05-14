package com.yeepay.yop.sdk.circuit;

/**
 * Exception thrown when a circuit breaker is triggered
 * This class replaces Sentinel's BlockException
 */
public class CircuitBreakerException extends Exception {

    private static final long serialVersionUID = -1L;
    
    private final String resource;
    private final YopCircuitBreaker rule;
    
    public CircuitBreakerException(String resource, YopCircuitBreaker rule) {
        super("Resource: " + resource + " is circuit broken, rule: " + rule);
        this.resource = resource;
        this.rule = rule;
    }
    
    public CircuitBreakerException(String resource, YopCircuitBreaker rule, Throwable cause) {
        super("Resource: " + resource + " is circuit broken, rule: " + rule, cause);
        this.resource = resource;
        this.rule = rule;
    }
    
    public String getResource() {
        return resource;
    }
    
    public YopCircuitBreaker getRule() {
        return rule;
    }
    
    /**
     * Check if an exception is a circuit breaker exception
     */
    public static boolean isCircuitBreakerException(Throwable t) {
        return t instanceof CircuitBreakerException;
    }
} 