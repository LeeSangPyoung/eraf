package com.eraf.core.idempotent;

/**
 * 멱등성 예외
 * 중복 요청 시 발생
 */
public class IdempotentException extends RuntimeException {

    private final String idempotencyKey;

    public IdempotentException(String message) {
        super(message);
        this.idempotencyKey = null;
    }

    public IdempotentException(String message, String idempotencyKey) {
        super(message);
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public static IdempotentException duplicateRequest(String key) {
        return new IdempotentException("중복된 요청입니다", key);
    }

    public static IdempotentException duplicateRequest(String key, String message) {
        return new IdempotentException(message, key);
    }
}
