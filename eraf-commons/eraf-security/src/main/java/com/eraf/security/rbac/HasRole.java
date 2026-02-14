package com.eraf.security.rbac;

import java.lang.annotation.*;

/**
 * RBAC 역할 기반 접근 제어 어노테이션
 * 메서드 또는 클래스 레벨에서 특정 역할을 가진 사용자만 접근 가능하도록 제한
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code @HasRole("ADMIN")}
 * public void deleteUser(Long userId) {
 *     // ADMIN 역할을 가진 사용자만 실행 가능
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasRole {

    /**
     * 필요한 역할 이름
     * Spring Security의 ROLE_ prefix는 자동으로 추가됨
     *
     * @return 역할 이름
     */
    String value();

    /**
     * 에러 메시지 (옵션)
     * 접근 거부 시 반환할 사용자 정의 메시지
     *
     * @return 에러 메시지
     */
    String message() default "접근 권한이 없습니다.";
}
