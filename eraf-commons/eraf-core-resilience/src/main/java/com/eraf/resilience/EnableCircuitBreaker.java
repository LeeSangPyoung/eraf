package com.eraf.resilience;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드에 Circuit Breaker 적용
 *
 * 사용 예:
 * <pre>
 * @EnableCircuitBreaker(name = "externalApi", failureThreshold = 3)
 * public String callExternalApi() {
 *     return restTemplate.getForObject("http://external/api", String.class);
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableCircuitBreaker {

    /**
     * 서킷브레이커 이름 (같은 이름은 공유됨)
     */
    String name();

    /**
     * OPEN 전환 실패 임계값
     */
    int failureThreshold() default 5;

    /**
     * HALF_OPEN에서 CLOSED로 전환하기 위한 성공 임계값
     */
    int successThreshold() default 3;

    /**
     * OPEN 상태 유지 시간 (밀리초)
     */
    long openTimeoutMs() default 30000;

    /**
     * 폴백 메서드 이름 (같은 클래스 내에 동일 시그니처 필요)
     */
    String fallbackMethod() default "";

    /**
     * 서킷브레이커 OPEN 시 던질 예외 클래스
     */
    Class<? extends RuntimeException> openException() default CircuitBreakerOpenException.class;
}
