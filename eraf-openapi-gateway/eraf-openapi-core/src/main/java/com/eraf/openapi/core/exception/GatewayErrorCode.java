package com.eraf.openapi.core.exception;

import com.eraf.core.exception.ErrorCode;

/**
 * Gateway 전용 에러 코드
 * eraf-commons의 ErrorCode 인터페이스 구현
 */
public enum GatewayErrorCode implements ErrorCode {

    // Timeout
    GATEWAY_TIMEOUT("GATEWAY_TIMEOUT", "Gateway Timeout", 504),

    // Retry
    RETRY_EXHAUSTED("RETRY_EXHAUSTED", "All retry attempts exhausted", 502),

    // Rate Limit
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "Rate limit exceeded", 429),

    // Authentication
    API_KEY_INVALID("API_KEY_INVALID", "Invalid API key", 401),
    API_KEY_MISSING("API_KEY_MISSING", "API key is required", 401),
    JWT_INVALID("JWT_INVALID", "Invalid JWT token", 401),
    JWT_EXPIRED("JWT_EXPIRED", "JWT token has expired", 401),

    // Authorization
    IP_RESTRICTED("IP_RESTRICTED", "IP address is not allowed", 403),
    API_DISABLED("API_DISABLED", "API is currently disabled", 403),

    // Routing
    ROUTE_NOT_FOUND("ROUTE_NOT_FOUND", "No matching route found", 404),
    API_NOT_FOUND("API_NOT_FOUND", "API endpoint not registered", 404),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Upstream service is unavailable", 503),
    NO_HEALTHY_TARGET("NO_HEALTHY_TARGET", "No healthy upstream target available", 503),

    // Validation
    VALIDATION_FAILED("VALIDATION_FAILED", "Request validation failed", 400),
    INVALID_REQUEST_BODY("INVALID_REQUEST_BODY", "Invalid request body", 400),
    MISSING_REQUIRED_HEADER("MISSING_REQUIRED_HEADER", "Required header is missing", 400),

    // Circuit Breaker
    CIRCUIT_BREAKER_OPEN("CIRCUIT_BREAKER_OPEN", "Circuit breaker is open", 503),

    // Certificate
    CERTIFICATE_EXPIRED("CERTIFICATE_EXPIRED", "Certificate has expired: {}", 400),
    CERTIFICATE_INVALID("CERTIFICATE_INVALID", "Invalid certificate format: {}", 400),

    // Consumer Group
    CONSUMER_ALREADY_IN_GROUP("CONSUMER_ALREADY_IN_GROUP", "Consumer is already in group: {}", 409),
    CONSUMER_NOT_IN_GROUP("CONSUMER_NOT_IN_GROUP", "Consumer is not in group: {}", 404),

    // Admin CRUD ({} placeholder는 BusinessException args로 치환)
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Resource not found: {}", 404),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "Resource already exists: {}", 409);

    private final String code;
    private final String message;
    private final int status;

    GatewayErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return status;
    }
}
