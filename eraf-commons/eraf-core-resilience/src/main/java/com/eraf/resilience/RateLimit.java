package com.eraf.resilience;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드에 Rate Limiting 적용
 *
 * 사용 예:
 * <pre>
 * @RateLimit(name = "userApi", permitsPerSecond = 100)
 * public User getUser(Long id) {
 *     return userRepository.findById(id);
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * Rate Limiter 이름 (같은 이름은 공유됨)
     */
    String name();

    /**
     * 초당 허가 수
     */
    double permitsPerSecond() default 100.0;

    /**
     * 최대 버스트 크기 (0이면 permitsPerSecond와 동일)
     */
    long maxBurstSize() default 0;

    /**
     * 키 생성 표현식 (SpEL)
     * 비어있으면 name만 사용, 있으면 name + key 조합
     * 예: "#userId" -> "userApi:123"
     */
    String key() default "";

    /**
     * 제한 초과 시 대기 여부 (false면 예외 발생)
     */
    boolean blocking() default false;

    /**
     * 폴백 메서드 이름
     */
    String fallbackMethod() default "";
}
