package com.eraf.core.exception;

/**
 * 공통 에러 코드
 */
public enum CommonErrorCode implements ErrorCode {

    // 400 Bad Request
    BAD_REQUEST("BAD_REQUEST", "잘못된 요청입니다.", 400),
    INVALID_INPUT("INVALID_INPUT", "입력값이 올바르지 않습니다.", 400),
    VALIDATION_ERROR("VALIDATION_ERROR", "유효성 검증에 실패했습니다.", 400),

    // 401 Unauthorized
    UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", 401),
    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰입니다.", 401),
    EXPIRED_TOKEN("EXPIRED_TOKEN", "만료된 토큰입니다.", 401),

    // 403 Forbidden
    FORBIDDEN("FORBIDDEN", "접근 권한이 없습니다.", 403),
    ACCESS_DENIED("ACCESS_DENIED", "접근이 거부되었습니다.", 403),
    FEATURE_DISABLED("FEATURE_DISABLED", "기능이 비활성화되어 있습니다: %s", 403),

    // 404 Not Found
    NOT_FOUND("NOT_FOUND", "리소스를 찾을 수 없습니다.", 404),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "요청한 리소스가 존재하지 않습니다.", 404),

    // 409 Conflict
    CONFLICT("CONFLICT", "리소스 충돌이 발생했습니다.", 409),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "이미 존재하는 리소스입니다.", 409),

    // 500 Internal Server Error
    INTERNAL_ERROR("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.", 500),
    SYSTEM_ERROR("SYSTEM_ERROR", "시스템 오류가 발생했습니다.", 500);

    private final String code;
    private final String message;
    private final int status;

    CommonErrorCode(String code, String message, int status) {
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
