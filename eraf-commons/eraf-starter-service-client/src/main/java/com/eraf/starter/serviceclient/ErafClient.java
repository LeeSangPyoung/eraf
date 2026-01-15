package com.eraf.starter.serviceclient;

import java.lang.annotation.*;

/**
 * 선언적 API 클라이언트 어노테이션
 * JWT 자동 전파, TraceId/UserId 헤더 자동 추가
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
     * Circuit Breaker 활성화
     */
    boolean circuitBreaker() default true;

    /**
     * 재시도 횟수
     */
    int retry() default 3;

    /**
     * 타임아웃 (밀리초)
     */
    long timeout() default 30000;
}
