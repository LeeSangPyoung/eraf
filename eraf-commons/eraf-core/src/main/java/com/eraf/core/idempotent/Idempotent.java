package com.eraf.core.idempotent;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 멱등성 보장 어노테이션
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 멱등성 키 표현식 (SpEL)
     * 기본값: 메서드 파라미터를 조합하여 생성
     */
    String key() default "";

    /**
     * 키 유지 시간
     */
    long timeout() default 24;

    /**
     * 시간 단위
     */
    TimeUnit timeUnit() default TimeUnit.HOURS;

    /**
     * 중복 요청 시 예외 발생 여부 (false면 이전 결과 반환)
     */
    boolean throwOnDuplicate() default false;

    /**
     * 중복 요청 시 메시지
     */
    String message() default "중복된 요청입니다";
}
