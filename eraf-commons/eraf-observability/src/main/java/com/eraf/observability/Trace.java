package com.eraf.observability;

import io.opentelemetry.api.trace.SpanKind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 선언적 OpenTelemetry Span 생성 어노테이션
 *
 * <p>메서드에 붙이면 AOP를 통해 자동으로 Span이 생성/종료됩니다.
 * 예외 발생 시 Span에 에러가 기록됩니다.
 *
 * <p>사용 예:
 * <pre>{@code
 * @Trace("user.service.createUser")
 * public User createUser(UserCreateRequest request) {
 *     // ...
 * }
 *
 * @Trace(value = "payment.process", kind = SpanKind.CLIENT, attributes = {"payment.type"})
 * public PaymentResult processPayment(String type, PaymentRequest request) {
 *     // ...
 * }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Trace {

    /**
     * Span 이름. 비어있으면 "클래스명.메서드명" 형식으로 자동 생성됩니다.
     */
    String value() default "";

    /**
     * Span 종류
     */
    SpanKind kind() default SpanKind.INTERNAL;

    /**
     * Span에 추가할 파라미터 이름 목록.
     * 지정된 이름의 파라미터 값이 Span attribute로 추가됩니다.
     * 예: attributes = {"userId", "orderId"}
     */
    String[] attributes() default {};
}
