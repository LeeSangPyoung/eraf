package com.eraf.http;

import java.lang.annotation.*;

/**
 * 선언적 API 클라이언트 어노테이션
 * JWT 자동 전파, TraceId/UserId 헤더 자동 추가
 *
 * <p>이 어노테이션의 속성들은 {@link ErafClientBeanPostProcessor}에 의해 처리되어
 * 각 클라이언트별 Feign 설정에 반영됩니다.
 * 전역 기본값은 {@link ErafHttpProperties}에서 설정할 수 있으며,
 * 어노테이션 속성이 지정된 경우 해당 클라이언트에 대해 전역 설정을 오버라이드합니다.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ErafClient {

    /**
     * 서비스 이름 (Service Discovery 사용 시)
     */
    String value() default "";

    /**
     * 서비스 URL (직접 지정 시)
     */
    String url() default "";

    /**
     * 컨텍스트 경로
     */
    String path() default "";

    /**
     * Circuit Breaker 활성화.
     * 기본값은 {@link ErafHttpProperties#isCircuitBreakerEnabled()}를 따릅니다.
     */
    boolean circuitBreaker() default true;

    /**
     * 재시도 횟수.
     * 기본값은 {@link ErafHttpProperties#getRetryCount()}를 따릅니다.
     */
    int retry() default 3;

    /**
     * 타임아웃 (밀리초).
     * 기본값은 {@link ErafHttpProperties#getTimeout()}를 따릅니다.
     */
    long timeout() default 30000;
}
