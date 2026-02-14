package com.eraf.security.rbac;

import java.lang.annotation.*;

/**
 * RBAC 권한 기반 접근 제어 어노테이션
 * 메서드 또는 클래스 레벨에서 특정 권한을 가진 사용자만 접근 가능하도록 제한
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code @HasPermission("USER:DELETE")}
 * public void deleteUser(Long userId) {
 *     // USER:DELETE 권한을 가진 사용자만 실행 가능
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasPermission {

    /**
     * 필요한 권한 코드
     * 일반적으로 "RESOURCE:ACTION" 형식 사용 (예: USER:READ, ORDER:CREATE)
     *
     * @return 권한 코드
     */
    String value();

    /**
     * 대상 리소스 타입 (옵션)
     * 동적 권한 체크 시 사용
     *
     * @return 리소스 타입
     */
    String resourceType() default "";

    /**
     * 에러 메시지 (옵션)
     *
     * @return 에러 메시지
     */
    String message() default "해당 권한이 없습니다.";
}
