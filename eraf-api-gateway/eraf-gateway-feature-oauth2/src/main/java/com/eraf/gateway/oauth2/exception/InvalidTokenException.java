package com.eraf.gateway.oauth2.exception;

/**
 * 유효하지 않은 토큰 예외
 */
public class InvalidTokenException extends OAuth2Exception {

    public InvalidTokenException() {
        super(OAuth2ErrorCode.OAUTH2_TOKEN_INVALID);
    }

    public InvalidTokenException(String message) {
        super(OAuth2ErrorCode.OAUTH2_TOKEN_INVALID, message);
    }

    public InvalidTokenException(Throwable cause) {
        super(OAuth2ErrorCode.OAUTH2_TOKEN_INVALID, cause);
    }
}
