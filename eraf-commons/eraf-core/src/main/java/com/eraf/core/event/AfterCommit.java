package com.eraf.core.event;

import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.annotation.*;

/**
 * 트랜잭션 커밋 후 이벤트 처리 어노테이션
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public @interface AfterCommit {

    /**
     * 처리할 이벤트 클래스
     */
    Class<?>[] value() default {};

    /**
     * 조건 (SpEL 표현식)
     */
    String condition() default "";

    /**
     * 트랜잭션이 없을 때도 실행할지 여부
     */
    boolean fallbackExecution() default false;
}
