package com.eraf.actuator.metrics;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 호출 횟수 카운트
 *
 * 사용 예:
 * <pre>
 * @Counted(value = "user.created", description = "생성된 사용자 수")
 * public User createUser(UserDto dto) {
 *     return userService.create(dto);
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Counted {

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
     * 예외 시에도 카운트 여부
     */
    boolean recordOnException() default false;
}
