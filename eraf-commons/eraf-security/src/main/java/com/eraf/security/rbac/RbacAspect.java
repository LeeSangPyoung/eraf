package com.eraf.security.rbac;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RBAC 어노테이션 처리 AOP Aspect
 * @HasRole, @HasAnyRole, @HasPermission, @HasAnyPermission 어노테이션 처리
 */
@Aspect
@Component
public class RbacAspect {

    private static final Logger log = LoggerFactory.getLogger(RbacAspect.class);

    private final ErafPermissionEvaluator permissionEvaluator;

    public RbacAspect(ErafPermissionEvaluator permissionEvaluator) {
        this.permissionEvaluator = permissionEvaluator;
    }

    /**
     * @HasRole 어노테이션 체크
     */
    @Before("@annotation(hasRole)")
    public void checkHasRole(JoinPoint joinPoint, HasRole hasRole) {
        Authentication auth = getAuthentication();
        String requiredRole = hasRole.value();
        String methodName = joinPoint.getSignature().toShortString();
        String principal = auth.getName();

        log.debug("[RBAC] Checking @HasRole: user={}, role={}, method={}", principal, requiredRole, methodName);

        if (!hasRole(auth, requiredRole)) {
            log.warn("[RBAC] Access denied - @HasRole failed: user={}, requiredRole={}, method={}",
                    principal, requiredRole, methodName);
            throw new AccessDeniedException(hasRole.message());
        }

        log.info("[RBAC] Access granted - @HasRole passed: user={}, role={}, method={}",
                principal, requiredRole, methodName);
    }

    /**
     * @HasAnyRole 어노테이션 체크
     */
    @Before("@annotation(hasAnyRole)")
    public void checkHasAnyRole(JoinPoint joinPoint, HasAnyRole hasAnyRole) {
        Authentication auth = getAuthentication();
        String[] requiredRoles = hasAnyRole.value();
        String methodName = joinPoint.getSignature().toShortString();
        String principal = auth.getName();

        log.debug("[RBAC] Checking @HasAnyRole: user={}, roles={}, method={}",
                principal, String.join(", ", requiredRoles), methodName);

        boolean hasAnyRequiredRole = false;
        String matchedRole = null;
        for (String role : requiredRoles) {
            if (hasRole(auth, role)) {
                hasAnyRequiredRole = true;
                matchedRole = role;
                break;
            }
        }

        if (!hasAnyRequiredRole) {
            log.warn("[RBAC] Access denied - @HasAnyRole failed: user={}, requiredRoles={}, method={}",
                    principal, String.join(", ", requiredRoles), methodName);
            throw new AccessDeniedException(hasAnyRole.message());
        }

        log.info("[RBAC] Access granted - @HasAnyRole passed: user={}, matchedRole={}, method={}",
                principal, matchedRole, methodName);
    }

    /**
     * @HasPermission 어노테이션 체크
     */
    @Before("@annotation(hasPermission)")
    public void checkHasPermission(JoinPoint joinPoint, HasPermission hasPermission) {
        Authentication auth = getAuthentication();
        String requiredPermission = hasPermission.value();
        String methodName = joinPoint.getSignature().toShortString();
        String principal = auth.getName();

        log.debug("[RBAC] Checking @HasPermission: user={}, permission={}, method={}",
                principal, requiredPermission, methodName);

        // 직접 권한 체크
        if (!hasPermission(auth, requiredPermission)) {
            log.debug("[RBAC] Direct permission check failed, trying dynamic evaluation: user={}, permission={}",
                    principal, requiredPermission);

            // PermissionEvaluator로 동적 체크
            Object targetDomainObject = joinPoint.getThis();
            if (!permissionEvaluator.hasPermission(auth, targetDomainObject, requiredPermission)) {
                log.warn("[RBAC] Access denied - @HasPermission failed: user={}, requiredPermission={}, method={}",
                        principal, requiredPermission, methodName);
                throw new AccessDeniedException(hasPermission.message());
            }
        }

        log.info("[RBAC] Access granted - @HasPermission passed: user={}, permission={}, method={}",
                principal, requiredPermission, methodName);
    }

    /**
     * @HasAnyPermission 어노테이션 체크
     */
    @Before("@annotation(hasAnyPermission)")
    public void checkHasAnyPermission(JoinPoint joinPoint, HasAnyPermission hasAnyPermission) {
        Authentication auth = getAuthentication();
        String[] requiredPermissions = hasAnyPermission.value();
        String methodName = joinPoint.getSignature().toShortString();
        String principal = auth.getName();

        log.debug("[RBAC] Checking @HasAnyPermission: user={}, permissions={}, method={}",
                principal, String.join(", ", requiredPermissions), methodName);

        boolean hasAnyRequiredPermission = false;
        String matchedPermission = null;
        for (String permission : requiredPermissions) {
            if (hasPermission(auth, permission)) {
                hasAnyRequiredPermission = true;
                matchedPermission = permission;
                break;
            }
        }

        if (!hasAnyRequiredPermission) {
            log.warn("[RBAC] Access denied - @HasAnyPermission failed: user={}, requiredPermissions={}, method={}",
                    principal, String.join(", ", requiredPermissions), methodName);
            throw new AccessDeniedException(hasAnyPermission.message());
        }

        log.info("[RBAC] Access granted - @HasAnyPermission passed: user={}, matchedPermission={}, method={}",
                principal, matchedPermission, methodName);
    }

    /**
     * 클래스 레벨 @HasRole 체크
     */
    @Before("@within(hasRole) && execution(public * *(..))")
    public void checkClassHasRole(JoinPoint joinPoint, HasRole hasRole) {
        // 메서드 레벨에 어노테이션이 있으면 스킵 (메서드 레벨이 우선)
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.isAnnotationPresent(HasRole.class)) {
            return;
        }

        checkHasRole(joinPoint, hasRole);
    }

    /**
     * 클래스 레벨 @HasAnyRole 체크
     */
    @Before("@within(hasAnyRole) && execution(public * *(..))")
    public void checkClassHasAnyRole(JoinPoint joinPoint, HasAnyRole hasAnyRole) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.isAnnotationPresent(HasAnyRole.class)) {
            return;
        }

        checkHasAnyRole(joinPoint, hasAnyRole);
    }

    /**
     * 클래스 레벨 @HasPermission 체크
     */
    @Before("@within(hasPermission) && execution(public * *(..))")
    public void checkClassHasPermission(JoinPoint joinPoint, HasPermission hasPermission) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.isAnnotationPresent(HasPermission.class)) {
            return;
        }

        checkHasPermission(joinPoint, hasPermission);
    }

    /**
     * 클래스 레벨 @HasAnyPermission 체크
     */
    @Before("@within(hasAnyPermission) && execution(public * *(..))")
    public void checkClassHasAnyPermission(JoinPoint joinPoint, HasAnyPermission hasAnyPermission) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (method.isAnnotationPresent(HasAnyPermission.class)) {
            return;
        }

        checkHasAnyPermission(joinPoint, hasAnyPermission);
    }

    // ========== Helper Methods ==========

    private Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("[RBAC] Authentication required but not found or not authenticated");
            throw new AccessDeniedException("인증이 필요합니다.");
        }
        return auth;
    }

    private boolean hasRole(Authentication auth, String role) {
        Set<String> authorities = getAuthorities(auth);

        // ROLE_ prefix 추가 (없는 경우)
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return authorities.contains(role) || authorities.contains(roleWithPrefix);
    }

    private boolean hasPermission(Authentication auth, String permission) {
        Set<String> authorities = getAuthorities(auth);
        return authorities.contains(permission);
    }

    private Set<String> getAuthorities(Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
