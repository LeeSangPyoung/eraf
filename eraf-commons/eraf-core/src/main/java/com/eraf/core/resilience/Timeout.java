package com.eraf.core.resilience;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 메서드 실행 타임아웃 적용
 *
 * 사용 예:
 * <pre>
 * @Timeout(value = 5, unit = TimeUnit.SECONDS)
 * public Data fetchData() {
 *     return externalService.call();
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Timeout {

    /**
     * 타임아웃 값
     */
    long value();

    /**
     * 시간 단위 (기본값: SECONDS)
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 폴백 메서드 이름
     */
    String fallbackMethod() default "";
}
