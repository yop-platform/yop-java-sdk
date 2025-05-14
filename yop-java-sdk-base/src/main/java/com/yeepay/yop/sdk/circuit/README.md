# YOP SDK Circuit Breaker

This package contains a custom implementation of a circuit breaker pattern that replaces the previous dependency on Alibaba Sentinel.

## Overview

The circuit breaker pattern is used to prevent cascading failures by temporarily blocking access to resources (like remote servers) that are experiencing issues. The implementation supports:

1. Domain fault circuit breaking
2. Monitoring and reporting functionality
3. Backwards compatibility with existing configuration habits
4. No circular module dependencies

## Main Components

- `YopCircuitBreakerManager`: Central manager that handles circuit breaker rules
- `YopCircuitBreaker`: The core circuit breaker implementation with state management
- `CircuitBreakerEntry`: Represents an entry to a protected resource
- `YopCircuitBreakerStrategy`: Defines circuit breaking strategies (RT/error ratio/error count)
- `ResourceState`: Tracks metrics for resources to make circuit breaking decisions
- `YopCircuitBreakerPool`: Manages server resources for circuit breaking

## Configuration

The circuit breaker is configured using the same `YopCircuitBreakerConfig` class as before, ensuring backward compatibility.

## Usage

The circuit breaker is used in the same way as before:

1. Resources are registered with the circuit breaker
2. When accessing a resource, a circuit breaker entry is obtained
3. If the circuit is open, access is denied with a CircuitBreakerException
4. Errors are tracked and can cause the circuit to open
5. Recovery is handled through half-open state testing 