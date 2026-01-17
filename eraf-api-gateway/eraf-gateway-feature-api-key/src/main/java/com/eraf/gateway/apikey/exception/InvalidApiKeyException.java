package com.eraf.gateway.apikey.exception;

import com.eraf.gateway.common.exception.GatewayErrorCode;
import com.eraf.gateway.common.exception.GatewayException;

/**
 * API Key 인증 실패 예외
 */
public class InvalidApiKeyException extends GatewayException {

    public InvalidApiKeyException(GatewayErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidApiKeyException(GatewayErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public InvalidApiKeyException(GatewayErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public InvalidApiKeyException(GatewayErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }

    /**
     * API Key가 누락된 경우
     */
    public static InvalidApiKeyException missing() {
        return new InvalidApiKeyException(GatewayErrorCode.API_KEY_MISSING);
    }

    /**
     * API Key가 유효하지 않은 경우
     */
    public static InvalidApiKeyException invalid() {
        return new InvalidApiKeyException(GatewayErrorCode.API_KEY_INVALID);
    }

    /**
     * API Key가 만료된 경우
     */
    public static InvalidApiKeyException expired() {
        return new InvalidApiKeyException(GatewayErrorCode.API_KEY_EXPIRED);
    }

    /**
     * API Key가 비활성화된 경우
     */
    public static InvalidApiKeyException disabled() {
        return new InvalidApiKeyException(GatewayErrorCode.API_KEY_DISABLED);
    }

    /**
     * 경로 접근 권한이 없는 경우
     */
    public static InvalidApiKeyException pathNotAllowed(String path) {
        return new InvalidApiKeyException(GatewayErrorCode.API_KEY_PATH_NOT_ALLOWED, path);
    }

    /**
     * IP 접근 권한이 없는 경우
     */
    public static InvalidApiKeyException ipNotAllowed(String ip) {
        return new InvalidApiKeyException(GatewayErrorCode.API_KEY_IP_NOT_ALLOWED, ip);
    }
}
