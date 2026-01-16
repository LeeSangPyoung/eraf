package com.eraf.gateway.exception;

/**
 * 유효하지 않은 API Key 예외
 */
public class InvalidApiKeyException extends GatewayException {

    public InvalidApiKeyException() {
        super(GatewayErrorCode.API_KEY_INVALID);
    }

    public InvalidApiKeyException(GatewayErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidApiKeyException missing() {
        return new InvalidApiKeyException(GatewayErrorCode.API_KEY_MISSING);
    }

    public static InvalidApiKeyException invalid() {
        return new InvalidApiKeyException(GatewayErrorCode.API_KEY_INVALID);
    }
}
