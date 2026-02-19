package com.eraf.idempotent;

/**
 * 멱등성 처리 중 예외
 *
 * <p>동일한 키로 요청이 아직 처리 중(in-progress)일 때 발생합니다.
 * {@link IdempotentException}과 달리 이전 결과가 아직 캐시되지 않은 상태를 나타냅니다.</p>
 */
public class IdempotentInProgressException extends IdempotentException {

    public IdempotentInProgressException(String idempotencyKey) {
        super("요청이 처리 중입니다. 잠시 후 다시 시도해 주세요.", idempotencyKey);
    }

    public IdempotentInProgressException(String message, String idempotencyKey) {
        super(message, idempotencyKey);
    }

    public static IdempotentInProgressException forKey(String key) {
        return new IdempotentInProgressException(key);
    }
}
