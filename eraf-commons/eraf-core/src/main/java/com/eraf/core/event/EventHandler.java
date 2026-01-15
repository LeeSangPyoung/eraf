package com.eraf.core.event;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 이벤트 핸들러 어노테이션
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EventListener
public @interface EventHandler {

    /**
     * 처리할 이벤트 클래스
     */
    @AliasFor(annotation = EventListener.class)
    Class<?>[] value() default {};

    /**
     * 조건 (SpEL 표현식)
     */
    @AliasFor(annotation = EventListener.class)
    String condition() default "";
}
