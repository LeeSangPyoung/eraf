package com.eraf.security.rbac;

import java.lang.annotation.*;

/**
 * RBAC 다중 역할 기반 접근 제어 어노테이션
 * 여러 역할 중 하나라도 가지고 있으면 접근 허용
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code @HasAnyRole({"ADMIN", "MANAGER"})}
 * public void viewReport() {
 *     // ADMIN 또는 MANAGER 역할을 가진 사용자 접근 가능
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasAnyRole {

    /**
     * 필요한 역할 목록 (하나라도 일치하면 허용)
     * Spring Security의 ROLE_ prefix는 자동으로 추가됨
     *
     * @return 역할 이름 배열
     */
    String[] value();

    /**
     * 에러 메시지 (옵션)
     *
     * @return 에러 메시지
     */
    String message() default "접근 권한이 없습니다.";
}
