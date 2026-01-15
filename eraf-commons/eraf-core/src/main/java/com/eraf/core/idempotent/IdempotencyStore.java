package com.eraf.core.idempotent;

import java.time.Duration;
import java.util.Optional;

/**
 * 멱등성 저장소 인터페이스
 * 기본 구현은 인메모리, Redis 사용 시 Redis 구현으로 대체
 */
public interface IdempotencyStore {

    /**
     * 키 존재 여부 확인 및 저장 (원자적)
     *
     * @param key     멱등성 키
     * @param timeout 만료 시간
     * @return true: 새로운 키 (저장됨), false: 이미 존재하는 키
     */
    boolean setIfAbsent(String key, Duration timeout);

    /**
     * 키 존재 여부 확인
     */
    boolean exists(String key);

    /**
     * 결과 저장
     */
    void saveResult(String key, Object result, Duration timeout);

    /**
     * 결과 조회
     */
    Optional<Object> getResult(String key);

    /**
     * 키 삭제
     */
    void delete(String key);
}
