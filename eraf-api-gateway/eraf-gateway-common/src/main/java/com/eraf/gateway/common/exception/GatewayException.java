package com.eraf.gateway.common.exception;

import com.eraf.core.exception.BusinessException;
import com.eraf.core.exception.ErrorCode;

/**
 * API Gateway 기본 예외
 * eraf-core의 BusinessException을 확장
 */
public class GatewayException extends BusinessException {

    public GatewayException(ErrorCode errorCode) {
        super(errorCode);
    }

    public GatewayException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }

    public GatewayException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public GatewayException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode, cause, args);
    }

    /**
     * ErrorCode로부터 GatewayException 생성
     */
    public static GatewayException of(ErrorCode errorCode) {
        return new GatewayException(errorCode);
    }

    /**
     * ErrorCode로부터 GatewayException 생성 (인자 포함)
     */
    public static GatewayException of(ErrorCode errorCode, Object... args) {
        return new GatewayException(errorCode, args);
    }
}
