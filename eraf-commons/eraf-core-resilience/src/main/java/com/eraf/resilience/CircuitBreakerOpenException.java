package com.eraf.resilience;

/**
 * Circuit Breaker가 OPEN 상태일 때 발생하는 예외
 */
public class CircuitBreakerOpenException extends RuntimeException {

    private final String circuitBreakerName;
    private final CircuitBreaker.State state;

    public CircuitBreakerOpenException(String circuitBreakerName) {
        super("Circuit breaker '" + circuitBreakerName + "' is OPEN");
        this.circuitBreakerName = circuitBreakerName;
        this.state = CircuitBreaker.State.OPEN;
    }

    public CircuitBreakerOpenException(String circuitBreakerName, CircuitBreaker.State state) {
        super("Circuit breaker '" + circuitBreakerName + "' is " + state);
        this.circuitBreakerName = circuitBreakerName;
        this.state = state;
    }

    public String getCircuitBreakerName() {
        return circuitBreakerName;
    }

    public CircuitBreaker.State getState() {
        return state;
    }
}
