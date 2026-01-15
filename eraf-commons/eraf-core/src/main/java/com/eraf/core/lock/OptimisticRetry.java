package com.eraf.core.lock;

import java.lang.annotation.*;

/**
 * 낙관적 락 재시도 어노테이션
 * JPA @Version과 함께 사용
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OptimisticRetry {

    /**
     * 최대 재시도 횟수
     */
    int maxRetries() default 3;

    /**
     * 재시도 간격 (밀리초)
     */
    long backoffMs() default 100;

    /**
     * 지수 백오프 사용 여부
     */
    boolean exponentialBackoff() default true;

    /**
     * 재시도할 예외 클래스
     */
    Class<? extends Exception>[] retryOn() default {
            org.springframework.dao.OptimisticLockingFailureException.class
    };
}
