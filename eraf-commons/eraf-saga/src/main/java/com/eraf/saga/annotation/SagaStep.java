package com.eraf.saga.annotation;

import java.lang.annotation.*;

/**
 * Saga Step 정의 어노테이션
 * 메서드에 적용하여 Saga의 단계를 정의합니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SagaStep {

    /**
     * 단계 순서 (1부터 시작)
     */
    int order();

    /**
     * 단계 이름
     */
    String name() default "";

    /**
     * 보상 트랜잭션 메서드 이름
     */
    String compensate() default "";

    /**
     * 타임아웃 (밀리초, 0이면 Saga 기본값 사용)
     */
    long timeout() default 0;

    /**
     * 재시도 횟수 (0이면 Saga 기본값 사용)
     */
    int retries() default 0;

    /**
     * 재시도 간격 (밀리초)
     */
    long retryDelay() default 1000;
}
