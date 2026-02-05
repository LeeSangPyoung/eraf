package com.eraf.actuator.metrics;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 실행 시간 측정
 *
 * 사용 예:
 * <pre>
 * @Timed(value = "user.find", description = "사용자 조회 시간")
 * public User findUser(Long id) {
 *     return userRepository.findById(id);
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Timed {

    /**
     * 메트릭 이름
     */
    String value() default "";

    /**
     * 설명
     */
    String description() default "";

    /**
     * 추가 태그 (key1=value1,key2=value2 형식)
     */
    String[] extraTags() default {};

    /**
     * 히스토그램 활성화
     */
    boolean histogram() default false;

    /**
     * 퍼센타일 계산
     */
    double[] percentiles() default {};

    /**
     * 예외 시에도 기록 여부
     */
    boolean recordOnException() default true;
}
