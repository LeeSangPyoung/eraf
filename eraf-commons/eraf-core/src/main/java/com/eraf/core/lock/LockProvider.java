package com.eraf.core.lock;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 락 제공자 인터페이스
 * 기본 구현은 인메모리, Redis 사용 시 Redisson 기반으로 대체
 */
public interface LockProvider {

    /**
     * 락 획득 시도
     *
     * @param key       락 키
     * @param waitTime  대기 시간
     * @param leaseTime 유지 시간
     * @param unit      시간 단위
     * @return 획득 성공 여부
     */
    boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 락 획득 시도 (즉시)
     */
    boolean tryLock(String key);

    /**
     * 락 해제
     */
    void unlock(String key);

    /**
     * 락 보유 여부 확인
     */
    boolean isLocked(String key);

    /**
     * 현재 스레드가 락을 보유 중인지 확인
     */
    boolean isHeldByCurrentThread(String key);
}
