package com.yeepay.yop.sdk.circuit;

/**
 * Circuit breaking strategy types
 */
public enum YopCircuitBreakerStrategy {
    /**
     * Circuit breaking based on average response time
     */
    SLOW_REQUEST_RATIO(0),
    
    /**
     * Circuit breaking based on exception ratio
     */
    ERROR_RATIO(1),
    
    /**
     * Circuit breaking based on exception count
     */
    ERROR_COUNT(2);
    
    private final int type;
    
    YopCircuitBreakerStrategy(int type) {
        this.type = type;
    }
    
    public int getType() {
        return type;
    }
    
    public static YopCircuitBreakerStrategy fromType(int type) {
        for (YopCircuitBreakerStrategy strategy : values()) {
            if (strategy.type == type) {
                return strategy;
            }
        }
        return ERROR_COUNT; // Default
    }
} 