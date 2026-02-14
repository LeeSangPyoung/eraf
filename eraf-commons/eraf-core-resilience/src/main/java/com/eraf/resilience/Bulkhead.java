package com.eraf.resilience;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bulkhead 패턴 적용 (동시 실행 제한)
 *
 * 사용 예:
 * <pre>
 * @Bulkhead(name = "heavyOperation", maxConcurrent = 10)
 * public void processHeavyTask() {
 *     // 최대 10개까지만 동시 실행
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bulkhead {

    /**
     * Bulkhead 이름 (같은 이름은 공유됨)
     */
    String name();

    /**
     * 최대 동시 실행 수
     */
    int maxConcurrent() default 10;

    /**
     * 대기 큐 크기 (0이면 대기 없이 바로 거부)
     */
    int maxWait() default 0;

    /**
     * 대기 타임아웃 (밀리초, maxWait > 0일 때만 적용)
     */
    long waitTimeoutMs() default 0;

    /**
     * 폴백 메서드 이름
     */
    String fallbackMethod() default "";
}
