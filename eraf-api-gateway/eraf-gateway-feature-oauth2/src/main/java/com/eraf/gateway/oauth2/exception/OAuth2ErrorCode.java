package com.eraf.gateway.oauth2.exception;

import com.eraf.core.exception.ErrorCode;

/**
 * OAuth2 에러 코드
 */
public enum OAuth2ErrorCode implements ErrorCode {

    // Token 관련 오류
    OAUTH2_TOKEN_MISSING("OAUTH2_TOKEN_MISSING", "OAuth2 토큰이 필요합니다", 401),
    OAUTH2_TOKEN_INVALID("OAUTH2_TOKEN_INVALID", "유효하지 않은 OAuth2 토큰입니다", 401),
    OAUTH2_TOKEN_EXPIRED("OAUTH2_TOKEN_EXPIRED", "만료된 OAuth2 토큰입니다", 401),
    OAUTH2_TOKEN_REVOKED("OAUTH2_TOKEN_REVOKED", "취소된 OAuth2 토큰입니다", 401),

    // Scope 관련 오류
    OAUTH2_INSUFFICIENT_SCOPE("OAUTH2_INSUFFICIENT_SCOPE", "권한 범위가 부족합니다", 403),
    OAUTH2_INVALID_SCOPE("OAUTH2_INVALID_SCOPE", "유효하지 않은 권한 범위입니다", 400),

    // Client 관련 오류
    OAUTH2_CLIENT_NOT_FOUND("OAUTH2_CLIENT_NOT_FOUND", "클라이언트를 찾을 수 없습니다", 401),
    OAUTH2_CLIENT_UNAUTHORIZED("OAUTH2_CLIENT_UNAUTHORIZED", "클라이언트 인증에 실패했습니다", 401),
    OAUTH2_CLIENT_DISABLED("OAUTH2_CLIENT_DISABLED", "비활성화된 클라이언트입니다", 401),

    // Authorization Code 관련 오류
    OAUTH2_INVALID_AUTHORIZATION_CODE("OAUTH2_INVALID_AUTHORIZATION_CODE", "유효하지 않은 인증 코드입니다", 400),
    OAUTH2_AUTHORIZATION_CODE_EXPIRED("OAUTH2_AUTHORIZATION_CODE_EXPIRED", "만료된 인증 코드입니다", 400),
    OAUTH2_AUTHORIZATION_CODE_USED("OAUTH2_AUTHORIZATION_CODE_USED", "이미 사용된 인증 코드입니다", 400),

    // Grant Type 관련 오류
    OAUTH2_UNSUPPORTED_GRANT_TYPE("OAUTH2_UNSUPPORTED_GRANT_TYPE", "지원하지 않는 Grant Type입니다", 400),
    OAUTH2_INVALID_GRANT("OAUTH2_INVALID_GRANT", "유효하지 않은 Grant입니다", 400),

    // Redirect URI 관련 오류
    OAUTH2_INVALID_REDIRECT_URI("OAUTH2_INVALID_REDIRECT_URI", "유효하지 않은 Redirect URI입니다", 400),

    // Introspection 관련 오류
    OAUTH2_INTROSPECTION_FAILED("OAUTH2_INTROSPECTION_FAILED", "토큰 검증에 실패했습니다", 500),

    // 일반 오류
    OAUTH2_SERVER_ERROR("OAUTH2_SERVER_ERROR", "OAuth2 서버 오류가 발생했습니다", 500);

    private final String code;
    private final String message;
    private final int status;

    OAuth2ErrorCode(String code, String message, int status) {
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
