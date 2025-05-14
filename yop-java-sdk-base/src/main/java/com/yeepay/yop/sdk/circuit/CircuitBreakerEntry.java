package com.yeepay.yop.sdk.circuit;

/**
 * Represents an entry to a monitored resource.
 * This class replaces Sentinel's Entry interface.
 */
public class CircuitBreakerEntry {
    
    private final String resource;
    private final ResourceState resourceState;
    
    public CircuitBreakerEntry(String resource) {
        this.resource = resource;
        this.resourceState = null;
    }
    
    public CircuitBreakerEntry(String resource, ResourceState resourceState) {
        this.resource = resource;
        this.resourceState = resourceState;
    }
    
    /**
     * Exit the entry. This records the invocation's result (success or error).
     */
    public void exit() {
        // Nothing to do for a default exit (success path)
    }
    
    /**
     * Exit the entry with an exception. This records the error for statistic purposes.
     * 
     * @param error the exception that occurred
     */
    public void exit(Throwable error) {
        if (error != null && resourceState != null) {
            YopCircuitBreakerManager.markError(resource);
        }
    }
    
    /**
     * Get the resource associated with this entry
     * 
     * @return the resource name
     */
    public String getResource() {
        return resource;
    }
} 