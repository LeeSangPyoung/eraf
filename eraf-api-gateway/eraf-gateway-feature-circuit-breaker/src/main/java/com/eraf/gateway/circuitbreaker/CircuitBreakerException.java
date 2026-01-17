package com.eraf.gateway.circuitbreaker;

import com.eraf.gateway.common.exception.GatewayErrorCode;
import com.eraf.gateway.common.exception.GatewayException;
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
