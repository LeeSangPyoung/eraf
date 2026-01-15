package com.eraf.core.exception;

/**
 * 시스템 예외
 * 시스템 레벨의 예외 처리
 */
public class SystemException extends RuntimeException {

    private final String errorCode;

    public SystemException(String message) {
        super(message);
        this.errorCode = "SYSTEM_ERROR";
    }

    public SystemException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SYSTEM_ERROR";
    }

    public SystemException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
