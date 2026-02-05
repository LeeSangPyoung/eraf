package com.eraf.core.resilience;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 실행 재시도 적용
 *
 * 사용 예:
 * <pre>
 * @Retry(maxAttempts = 3, delay = 1000, multiplier = 2.0)
 * public Data fetchFromRemote() {
 *     return remoteService.call();
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retry {

    /**
     * 최대 시도 횟수 (첫 시도 포함)
     */
    int maxAttempts() default 3;

    /**
     * 재시도 간격 (밀리초)
     */
    long delay() default 1000;

    /**
     * 지수 백오프 배율 (1.0이면 고정 간격)
     */
    double multiplier() default 1.0;

    /**
     * 최대 지연 시간 (밀리초, 0이면 제한 없음)
     */
    long maxDelay() default 0;

    /**
     * 재시도할 예외 클래스들 (비어있으면 모든 예외)
     */
    Class<? extends Throwable>[] retryOn() default {};

    /**
     * 재시도하지 않을 예외 클래스들
     */
    Class<? extends Throwable>[] ignoreOn() default {};

    /**
     * 폴백 메서드 이름
     */
    String fallbackMethod() default "";
}
