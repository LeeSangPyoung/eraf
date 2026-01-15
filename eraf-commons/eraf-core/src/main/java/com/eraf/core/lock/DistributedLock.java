package com.eraf.core.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 분산 락 어노테이션
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

    /**
     * 락 키 (SpEL 표현식 지원)
     */
    String key();

    /**
     * 락 대기 시간
     */
    long waitTime() default 5;

    /**
     * 락 유지 시간
     */
    long leaseTime() default 10;

    /**
     * 시간 단위
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 락 획득 실패 시 예외 발생 여부
     */
    boolean failOnTimeout() default true;

    /**
     * 락 획득 실패 시 메시지
     */
    String message() default "락 획득에 실패했습니다";
}
