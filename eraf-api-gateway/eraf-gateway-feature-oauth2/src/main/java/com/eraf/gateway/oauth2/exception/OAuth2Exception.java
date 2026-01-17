package com.eraf.gateway.oauth2.exception;

import com.eraf.gateway.common.exception.GatewayException;

/**
 * OAuth2 인증/인가 예외
 */
public class OAuth2Exception extends GatewayException {

    public OAuth2Exception(OAuth2ErrorCode errorCode) {
        super(errorCode);
    }

    public OAuth2Exception(OAuth2ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public OAuth2Exception(OAuth2ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public OAuth2Exception(OAuth2ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }

    /**
     * Token이 누락된 경우
     */
    public static OAuth2Exception tokenMissing() {
        return new OAuth2Exception(OAuth2ErrorCode.OAUTH2_TOKEN_MISSING);
    }

    /**
     * Token이 유효하지 않은 경우
     */
    public static OAuth2Exception tokenInvalid() {
        return new OAuth2Exception(OAuth2ErrorCode.OAUTH2_TOKEN_INVALID);
    }

    /**
     * Token이 만료된 경우
     */
    public static OAuth2Exception tokenExpired() {
        return new OAuth2Exception(OAuth2ErrorCode.OAUTH2_TOKEN_EXPIRED);
    }

    /**
     * Token이 취소된 경우
     */
    public static OAuth2Exception tokenRevoked() {
        return new OAuth2Exception(OAuth2ErrorCode.OAUTH2_TOKEN_REVOKED);
    }
}
