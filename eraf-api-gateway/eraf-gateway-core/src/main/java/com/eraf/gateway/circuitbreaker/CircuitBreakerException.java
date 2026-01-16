package com.eraf.gateway.circuitbreaker;

import com.eraf.gateway.exception.GatewayErrorCode;
import com.eraf.gateway.exception.GatewayException;
import lombok.Getter;

/**
 * Circuit Breaker OPEN 상태 예외
 */
@Getter
public class CircuitBreakerException extends GatewayException {

    private final String circuitBreakerName;

    public CircuitBreakerException(String circuitBreakerName) {
        super(GatewayErrorCode.CIRCUIT_BREAKER_OPEN);
        this.circuitBreakerName = circuitBreakerName;
    }
}
