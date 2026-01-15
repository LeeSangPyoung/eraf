package com.eraf.core.event;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.Async;

import java.lang.annotation.*;

/**
 * 비동기 이벤트 핸들러 어노테이션
 * 이벤트를 비동기로 처리
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EventListener
@Async
public @interface AsyncEventHandler {

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

    /**
     * 사용할 Executor 이름
     */
    @AliasFor(annotation = Async.class, attribute = "value")
    String executor() default "";
}
