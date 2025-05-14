package com.yeepay.yop.sdk.circuit;

/**
 * Interface for listening to circuit breaker state changes
 */
public interface CircuitBreakerStateChangeListener {
    
    /**
     * Called when a circuit breaker changes state
     * 
     * @param oldState the previous state
     * @param newState the new state
     * @param rule the circuit breaker rule that changed
     */
    void onStateChange(YopCircuitBreaker.State oldState, YopCircuitBreaker.State newState, YopCircuitBreaker rule);
} 