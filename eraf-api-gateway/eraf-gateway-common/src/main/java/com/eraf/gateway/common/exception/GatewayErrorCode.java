package com.eraf.gateway.common.exception;

import com.eraf.core.exception.ErrorCode;

/**
 * API Gateway 에러 코드
 * 공통으로 사용되는 에러 코드만 정의
 * 각 기능별 세부 에러 코드는 각 기능 모듈에서 정의
 */
public enum GatewayErrorCode implements ErrorCode {

    // General
    GATEWAY_ERROR("GATEWAY_ERROR", "게이트웨이 오류가 발생했습니다", 500),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "서비스를 사용할 수 없습니다", 503),
    BAD_REQUEST("BAD_REQUEST", "잘못된 요청입니다", 400),
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다", 401),
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다", 403),

    // Rate Limiting
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "요청 한도를 초과했습니다", 429),

    // IP Restriction
    IP_BLOCKED("IP_BLOCKED", "차단된 IP 주소입니다", 403),
    IP_NOT_ALLOWED("IP_NOT_ALLOWED", "허용되지 않은 IP 주소입니다", 403),

    // API Key
    API_KEY_MISSING("API_KEY_MISSING", "API Key가 필요합니다", 401),
    API_KEY_INVALID("API_KEY_INVALID", "유효하지 않은 API Key입니다", 401),
    API_KEY_EXPIRED("API_KEY_EXPIRED", "만료된 API Key입니다", 401),
    API_KEY_DISABLED("API_KEY_DISABLED", "비활성화된 API Key입니다", 401),
    API_KEY_PATH_NOT_ALLOWED("API_KEY_PATH_NOT_ALLOWED", "해당 경로에 대한 접근 권한이 없습니다", 403),
    API_KEY_IP_NOT_ALLOWED("API_KEY_IP_NOT_ALLOWED", "해당 IP에서의 접근이 허용되지 않습니다", 403),

    // JWT
    JWT_MISSING("JWT_MISSING", "인증 토큰이 필요합니다", 401),
    JWT_INVALID("JWT_INVALID", "유효하지 않은 토큰입니다", 401),
    JWT_EXPIRED("JWT_EXPIRED", "만료된 토큰입니다", 401),
    JWT_MALFORMED("JWT_MALFORMED", "잘못된 형식의 토큰입니다", 401),
    JWT_SIGNATURE_INVALID("JWT_SIGNATURE_INVALID", "토큰 서명이 유효하지 않습니다", 401),

    // Circuit Breaker
    CIRCUIT_BREAKER_OPEN("CIRCUIT_BREAKER_OPEN", "서비스가 일시적으로 사용 불가합니다", 503),

    // Bot Detection
    BOT_BLOCKED("BOT_BLOCKED", "봇 접근이 차단되었습니다", 403);

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
