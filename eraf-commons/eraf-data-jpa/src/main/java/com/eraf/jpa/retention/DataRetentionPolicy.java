package com.eraf.jpa.retention;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 데이터 보존 정책 어노테이션
 *
 * Entity에 이 어노테이션을 추가하면 지정된 일수가 지난 데이터를 자동 삭제합니다.
 *
 * 예시:
 * <pre>
 * {@code
 * @Entity
 * @DataRetentionPolicy(days = 90, dateField = "createdAt")
 * public class AuditLog extends BaseEntity {
 *     // ...
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataRetentionPolicy {

    /**
     * 데이터 보존 일수 (기본값: 365일)
     */
    int days() default 365;

    /**
     * 날짜 비교에 사용할 필드명 (기본값: "createdAt")
     */
    String dateField() default "createdAt";
}
