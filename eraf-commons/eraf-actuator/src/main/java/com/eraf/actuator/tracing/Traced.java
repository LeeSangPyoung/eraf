package com.eraf.actuator.tracing;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 트레이싱 (새 스팬 생성)
 *
 * 사용 예:
 * <pre>
 * @Traced("orderService.createOrder")
 * public Order createOrder(OrderRequest request) {
 *     return orderRepository.save(order);
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Traced {

    /**
     * 스팬 이름 (비어있으면 메서드명 사용)
     */
    String value() default "";

    /**
     * 추가 태그
     */
    String[] tags() default {};

    /**
     * 파라미터 로깅 여부
     */
    boolean logParameters() default false;

    /**
     * 결과 로깅 여부
     */
    boolean logResult() default false;
}
