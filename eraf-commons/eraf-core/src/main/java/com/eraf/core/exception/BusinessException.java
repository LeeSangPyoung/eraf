package com.eraf.core.exception;

/**
 * 비즈니스 예외
 * ErrorCode 기반의 비즈니스 로직 예외 처리
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = null;
    }

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(formatMessage(errorCode.getMessage(), args));
        this.errorCode = errorCode;
        this.args = args;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(formatMessage(errorCode.getMessage(), args), cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public int getStatus() {
        return errorCode.getStatus();
    }

    public Object[] getArgs() {
        return args;
    }

    private static String formatMessage(String message, Object[] args) {
        if (args == null || args.length == 0) {
            return message;
        }
        try {
            return String.format(message.replace("{}", "%s"), args);
        } catch (Exception e) {
            return message;
        }
    }
}
