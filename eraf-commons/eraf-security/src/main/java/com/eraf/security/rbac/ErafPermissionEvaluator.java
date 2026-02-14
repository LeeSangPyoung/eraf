package com.eraf.security.rbac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ERAF PermissionEvaluator 구현
 * Spring Security의 @PreAuthorize/@PostAuthorize에서 hasPermission() 표현식 지원
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code @PreAuthorize("hasPermission(#userId, 'USER', 'READ')")}
 * public User getUser(Long userId) {
 *     return userRepository.findById(userId);
 * }
 * </pre>
 */
@Component
public class ErafPermissionEvaluator implements PermissionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(ErafPermissionEvaluator.class);

    private final RolePermissionRegistry rolePermissionRegistry;

    public ErafPermissionEvaluator(RolePermissionRegistry rolePermissionRegistry) {
        this.rolePermissionRegistry = rolePermissionRegistry;
    }

    /**
     * 특정 대상 객체에 대한 권한 체크
     *
     * @param authentication 인증 객체
     * @param targetDomainObject 대상 도메인 객체
     * @param permission 권한 (예: "READ", "WRITE", "DELETE")
     * @return 권한 여부
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("[PermissionEvaluator] Authentication null or not authenticated");
            return false;
        }

        if (permission == null) {
            log.warn("[PermissionEvaluator] Permission is null, denying access");
            return false;
        }

        String principal = authentication.getName();
        String permissionString = String.valueOf(permission);
        String targetType = targetDomainObject != null ? targetDomainObject.getClass().getSimpleName() : "null";

        log.debug("[PermissionEvaluator] Evaluating permission: user={}, permission={}, targetType={}",
                principal, permissionString, targetType);

        // 1. 직접 권한 체크 (authorities에 권한이 직접 포함된 경우)
        if (hasDirectPermission(authentication, permissionString)) {
            log.debug("[PermissionEvaluator] Direct permission granted: user={}, permission={}",
                    principal, permissionString);
            return true;
        }

        // 2. Role->Permission 매핑 체크
        Set<String> userRoles = getUserRoles(authentication);
        for (String role : userRoles) {
            if (rolePermissionRegistry.hasPermission(role, permissionString)) {
                log.debug("[PermissionEvaluator] Permission granted via role mapping: user={}, role={}, permission={}",
                        principal, role, permissionString);
                return true;
            }
        }

        // 3. 대상 객체 기반 동적 권한 체크 (확장 포인트)
        boolean result = checkObjectBasedPermission(authentication, targetDomainObject, permissionString);
        if (result) {
            log.debug("[PermissionEvaluator] Permission granted via object-based check: user={}, permission={}",
                    principal, permissionString);
        } else {
            log.debug("[PermissionEvaluator] Permission denied: user={}, permission={}, targetType={}",
                    principal, permissionString, targetType);
        }
        return result;
    }

    /**
     * 특정 리소스 타입과 ID에 대한 권한 체크
     *
     * @param authentication 인증 객체
     * @param targetId 대상 ID
     * @param targetType 대상 타입 (예: "USER", "ORDER", "PRODUCT")
     * @param permission 권한 (예: "READ", "WRITE", "DELETE")
     * @return 권한 여부
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("[PermissionEvaluator] Authentication null or not authenticated");
            return false;
        }

        if (permission == null) {
            log.warn("[PermissionEvaluator] Permission is null, denying access");
            return false;
        }

        if (targetType == null) {
            log.warn("[PermissionEvaluator] TargetType is null, denying access");
            return false;
        }

        String principal = authentication.getName();
        String permissionString = String.valueOf(permission);

        // 권한 포맷: "RESOURCE:ACTION" (예: "USER:READ", "ORDER:DELETE")
        String fullPermission = targetType + ":" + permissionString;

        log.debug("[PermissionEvaluator] Evaluating permission: user={}, targetType={}, targetId={}, permission={}",
                principal, targetType, targetId, permissionString);

        // 1. 직접 권한 체크
        if (hasDirectPermission(authentication, fullPermission)) {
            log.debug("[PermissionEvaluator] Direct permission granted: user={}, fullPermission={}",
                    principal, fullPermission);
            return true;
        }

        // 2. Role->Permission 매핑 체크
        Set<String> userRoles = getUserRoles(authentication);
        for (String role : userRoles) {
            if (rolePermissionRegistry.hasPermission(role, fullPermission)) {
                log.debug("[PermissionEvaluator] Permission granted via role mapping: user={}, role={}, fullPermission={}",
                        principal, role, fullPermission);
                return true;
            }
        }

        // 3. ID 기반 동적 권한 체크 (확장 포인트)
        boolean result = checkIdBasedPermission(authentication, targetId, targetType, permissionString);
        if (result) {
            log.debug("[PermissionEvaluator] Permission granted via ID-based check: user={}, targetId={}, fullPermission={}",
                    principal, targetId, fullPermission);
        } else {
            log.debug("[PermissionEvaluator] Permission denied: user={}, targetType={}, targetId={}, permission={}",
                    principal, targetType, targetId, permissionString);
        }
        return result;
    }

    // ========== Helper Methods ==========

    /**
     * 직접 권한 체크 (authorities에 권한이 직접 포함된 경우)
     */
    private boolean hasDirectPermission(Authentication authentication, String permission) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(permission));
    }

    /**
     * 사용자 역할 추출
     */
    private Set<String> getUserRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5)) // "ROLE_" 제거
                .collect(Collectors.toSet());
    }

    /**
     * 객체 기반 동적 권한 체크 (확장 포인트)
     * 서브클래스에서 오버라이드하여 커스텀 로직 구현 가능
     *
     * 예: 특정 문서의 소유자만 수정 가능하도록 체크
     */
    protected boolean checkObjectBasedPermission(Authentication authentication, Object targetDomainObject, String permission) {
        // 기본 구현: false 반환
        // 필요 시 서브클래스에서 오버라이드
        return false;
    }

    /**
     * ID 기반 동적 권한 체크 (확장 포인트)
     * 서브클래스에서 오버라이드하여 커스텀 로직 구현 가능
     *
     * 예: 특정 사용자 ID에 대한 소유권 체크
     */
    protected boolean checkIdBasedPermission(Authentication authentication, Serializable targetId, String targetType, String permission) {
        // 기본 구현: false 반환
        // 필요 시 서브클래스에서 오버라이드
        return false;
    }
}
