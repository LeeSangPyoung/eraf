package com.eraf.jpa.softdelete;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Soft Delete 활성화 마커
 *
 * 사용 예:
 * <pre>
 * @Entity
 * @SoftDelete
 * public class User extends SoftDeleteEntity {
 *     // ...
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SoftDelete {

    /**
     * 삭제 플래그 컬럼명
     */
    String column() default "deleted";

    /**
     * 삭제 시간 컬럼명 (비어있으면 기록 안함)
     */
    String deletedAtColumn() default "deleted_at";

    /**
     * 삭제자 컬럼명 (비어있으면 기록 안함)
     */
    String deletedByColumn() default "deleted_by";
}
