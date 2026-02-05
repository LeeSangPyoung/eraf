package com.eraf.saga.annotation;

import java.lang.annotation.*;

/**
 * Saga 정의 어노테이션
 * 클래스에 적용하여 Saga를 정의합니다.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Saga {

    /**
     * Saga 이름 (고유 식별자)
     */
    String name();

    /**
     * Saga 설명
     */
    String description() default "";

    /**
     * 타임아웃 (밀리초)
     */
    long timeout() default 300000; // 5분

    /**
     * 최대 재시도 횟수
     */
    int maxRetries() default 3;
}
