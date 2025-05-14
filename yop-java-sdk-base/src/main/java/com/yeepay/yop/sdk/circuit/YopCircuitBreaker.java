package com.yeepay.yop.sdk.circuit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Circuit breaker rule implementation to replace Sentinel's DegradeRule
 */
public class YopCircuitBreaker {
    
    private String resource;
    
    /**
     * Circuit breaking strategy (0: slow request ratio, 1: exception ratio, 2: exception count)
     */
    private int grade = YopCircuitBreakerStrategy.ERROR_COUNT.getType();
    
    /**
     * For slow request ratio strategy, this represents the RT threshold (in ms)
     * For exception ratio strategy, this represents the exception ratio threshold (0.0-1.0)
     * For exception count strategy, this represents the exception count threshold
     */
    private double count = 1.0;
    
    /**
     * Minimum number of requests required before triggering circuit breaking
     */
    private int minRequestAmount = 5;
    
    /**
     * Time window for statistics (ms)
     */
    private int statIntervalMs = 1000;
    
    /**
     * Time window for circuit breaker recovery (in seconds)
     */
    private int timeWindow = 5;
    
    /**
     * Listener for state changes
     */
    private CircuitBreakerStateChangeListener stateChangeListener;
    
    /**
     * Current state of the circuit breaker
     */
    private final AtomicReference<State> currentState = new AtomicReference<>(State.CLOSED);
    
    /**
     * Timestamp when the circuit was last opened
     */
    private volatile long lastOpenTime = 0;
    
    /**
     * Tracking map of state change listeners (by resource)
     */
    private static final Map<String, Map<String, CircuitBreakerStateChangeListener>> stateChangeListeners = new ConcurrentHashMap<>();
    
    public enum State {
        /**
         * Circuit is closed, all requests pass through
         */
        CLOSED,
        
        /**
         * Circuit is open, all requests are rejected
         */
        OPEN,
        
        /**
         * Circuit is half-open, letting some requests through for testing
         */
        HALF_OPEN
    }
    
    public YopCircuitBreaker() {
    }
    
    public YopCircuitBreaker(String resource) {
        this.resource = resource;
    }
    
    /**
     * Check if the circuit breaker is triggered based on the resource state
     */
    public boolean isTripped(ResourceState state) {
        if (state.getTotalCount() < minRequestAmount) {
            return false;
        }
        
        // Check the current state
        if (State.OPEN == currentState.get()) {
            // In OPEN state, check if we should transition to HALF_OPEN
            if (System.currentTimeMillis() - lastOpenTime > timeWindow * 1000) {
                if (currentState.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                    notifyStateChange(State.OPEN, State.HALF_OPEN);
                }
                return false; // Let this request through for testing
            }
            return true; // Still in open state, reject the request
        }
        
        // In HALF_OPEN or CLOSED state, check the rule
        boolean triggered = false;
        YopCircuitBreakerStrategy strategy = YopCircuitBreakerStrategy.fromType(grade);
        
        // Check if we should trip the circuit breaker based on the statistics
        switch (strategy) {
            case SLOW_REQUEST_RATIO:
                triggered = state.getAvgRt() > count;
                break;
                
            case ERROR_RATIO:
                triggered = state.getExceptionRatio() > count;
                break;
                
            case ERROR_COUNT:
                triggered = state.getExceptionCount() >= count;
                break;
                
            default:
                break;
        }
        
        // If triggered and in CLOSED or HALF_OPEN state, trip the circuit
        if (triggered) {
            State oldState = currentState.get();
            if (currentState.compareAndSet(oldState, State.OPEN)) {
                lastOpenTime = System.currentTimeMillis();
                notifyStateChange(oldState, State.OPEN);
            }
        } else if (State.HALF_OPEN == currentState.get()) {
            // If not triggered and in HALF_OPEN, we're recovered
            if (currentState.compareAndSet(State.HALF_OPEN, State.CLOSED)) {
                notifyStateChange(State.HALF_OPEN, State.CLOSED);
            }
        }
        
        return triggered;
    }
    
    /**
     * Called when a response time is recorded
     */
    public void onResponseTime(ResourceState state, long rt) {
        // Nothing to do for now, may implement statistics gathering here if needed
    }
    
    /**
     * Called when an error occurs
     */
    public void onError(ResourceState state) {
        // If we're in HALF_OPEN state and an error occurs, go back to OPEN
        if (State.HALF_OPEN == currentState.get() && 
            currentState.compareAndSet(State.HALF_OPEN, State.OPEN)) {
            lastOpenTime = System.currentTimeMillis();
            notifyStateChange(State.HALF_OPEN, State.OPEN);
        }
    }
    
    /**
     * Add a state change listener for this circuit breaker
     */
    public static void addStateChangeListener(String name, CircuitBreakerStateChangeListener listener) {
        if (listener != null && name != null) {
            stateChangeListeners.computeIfAbsent(name, k -> new ConcurrentHashMap<>())
                .put(name, listener);
        }
    }
    
    /**
     * Notify all listeners of a state change
     */
    private void notifyStateChange(State oldState, State newState) {
        stateChangeListeners.forEach((name, listeners) -> {
            listeners.forEach((listenerName, listener) -> {
                try {
                    listener.onStateChange(oldState, newState, this);
                } catch (Exception e) {
                    // Ignore exceptions in listeners
                }
            });
        });
        
        if (stateChangeListener != null) {
            try {
                stateChangeListener.onStateChange(oldState, newState, this);
            } catch (Exception e) {
                // Ignore exceptions in listener
            }
        }
    }
    
    public String getResource() {
        return resource;
    }
    
    public YopCircuitBreaker setResource(String resource) {
        this.resource = resource;
        return this;
    }
    
    public int getGrade() {
        return grade;
    }
    
    public YopCircuitBreaker setGrade(int grade) {
        this.grade = grade;
        return this;
    }
    
    public double getCount() {
        return count;
    }
    
    public YopCircuitBreaker setCount(double count) {
        this.count = count;
        return this;
    }
    
    public int getMinRequestAmount() {
        return minRequestAmount;
    }
    
    public YopCircuitBreaker setMinRequestAmount(int minRequestAmount) {
        this.minRequestAmount = minRequestAmount;
        return this;
    }
    
    public int getStatIntervalMs() {
        return statIntervalMs;
    }
    
    public YopCircuitBreaker setStatIntervalMs(int statIntervalMs) {
        this.statIntervalMs = statIntervalMs;
        return this;
    }
    
    public int getTimeWindow() {
        return timeWindow;
    }
    
    public YopCircuitBreaker setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
        return this;
    }
    
    public CircuitBreakerStateChangeListener getStateChangeListener() {
        return stateChangeListener;
    }
    
    public YopCircuitBreaker setStateChangeListener(CircuitBreakerStateChangeListener stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
        return this;
    }
    
    public State getCurrentState() {
        return currentState.get();
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof YopCircuitBreaker)) {
            return false;
        }
        
        YopCircuitBreaker rule = (YopCircuitBreaker) o;
        
        if (grade != rule.grade) {
            return false;
        }
        if (Double.compare(rule.count, count) != 0) {
            return false;
        }
        if (minRequestAmount != rule.minRequestAmount) {
            return false;
        }
        if (statIntervalMs != rule.statIntervalMs) {
            return false;
        }
        if (timeWindow != rule.timeWindow) {
            return false;
        }
        return resource != null ? resource.equals(rule.resource) : rule.resource == null;
    }
    
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = resource != null ? resource.hashCode() : 0;
        result = 31 * result + grade;
        temp = Double.doubleToLongBits(count);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + minRequestAmount;
        result = 31 * result + statIntervalMs;
        result = 31 * result + timeWindow;
        return result;
    }
} 