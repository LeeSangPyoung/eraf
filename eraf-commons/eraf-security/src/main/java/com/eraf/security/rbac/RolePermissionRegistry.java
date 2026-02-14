package com.eraf.security.rbac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Role과 Permission 매핑 레지스트리
 * 역할별 권한 목록을 관리하고 조회
 *
 * <p>사용 예시:</p>
 * <pre>
 * // 초기화
 * rolePermissionRegistry.addRolePermissions("ADMIN", Set.of("USER:READ", "USER:WRITE", "USER:DELETE"));
 * rolePermissionRegistry.addRolePermissions("USER", Set.of("USER:READ"));
 *
 * // 체크
 * boolean canDelete = rolePermissionRegistry.hasPermission("ADMIN", "USER:DELETE"); // true
 * </pre>
 */
@Component
public class RolePermissionRegistry {

    private static final Logger log = LoggerFactory.getLogger(RolePermissionRegistry.class);

    /**
     * Role -> Permissions 매핑
     * ConcurrentHashMap으로 thread-safe 보장
     */
    private final Map<String, Set<String>> rolePermissions = new ConcurrentHashMap<>();

    /**
     * 역할에 권한 추가
     *
     * @param role 역할 이름
     * @param permission 권한 코드
     */
    public void addPermission(String role, String permission) {
        String normalizedRole = normalizeRole(role);
        rolePermissions.computeIfAbsent(normalizedRole, k -> ConcurrentHashMap.newKeySet())
                .add(permission);
        log.info("[RolePermissionRegistry] Added permission: role={}, permission={}", normalizedRole, permission);
    }

    /**
     * 역할에 여러 권한 일괄 추가
     *
     * @param role 역할 이름
     * @param permissions 권한 코드 목록
     */
    public void addRolePermissions(String role, Set<String> permissions) {
        String normalizedRole = normalizeRole(role);
        rolePermissions.computeIfAbsent(normalizedRole, k -> ConcurrentHashMap.newKeySet())
                .addAll(permissions);
        log.info("[RolePermissionRegistry] Added permissions: role={}, count={}, permissions={}",
                normalizedRole, permissions.size(), permissions);
    }

    /**
     * 역할의 모든 권한 설정 (기존 권한 덮어쓰기)
     *
     * @param role 역할 이름
     * @param permissions 권한 코드 목록
     */
    public void setRolePermissions(String role, Set<String> permissions) {
        String normalizedRole = normalizeRole(role);
        Set<String> permissionSet = ConcurrentHashMap.newKeySet();
        permissionSet.addAll(permissions);
        rolePermissions.put(normalizedRole, permissionSet);
        log.info("[RolePermissionRegistry] Set permissions (overwrite): role={}, count={}, permissions={}",
                normalizedRole, permissions.size(), permissions);
    }

    /**
     * 역할에서 권한 제거
     *
     * @param role 역할 이름
     * @param permission 권한 코드
     */
    public void removePermission(String role, String permission) {
        String normalizedRole = normalizeRole(role);
        Set<String> permissions = rolePermissions.get(normalizedRole);
        if (permissions != null) {
            boolean removed = permissions.remove(permission);
            if (removed) {
                log.info("[RolePermissionRegistry] Removed permission: role={}, permission={}", normalizedRole, permission);
            } else {
                log.debug("[RolePermissionRegistry] Permission not found for removal: role={}, permission={}",
                        normalizedRole, permission);
            }
        } else {
            log.debug("[RolePermissionRegistry] Role not found for permission removal: role={}", normalizedRole);
        }
    }

    /**
     * 역할의 모든 권한 제거
     *
     * @param role 역할 이름
     */
    public void removeRole(String role) {
        String normalizedRole = normalizeRole(role);
        Set<String> removed = rolePermissions.remove(normalizedRole);
        if (removed != null) {
            log.info("[RolePermissionRegistry] Removed role: role={}, permissionCount={}", normalizedRole, removed.size());
        } else {
            log.debug("[RolePermissionRegistry] Role not found for removal: role={}", normalizedRole);
        }
    }

    /**
     * 역할이 특정 권한을 가지고 있는지 체크
     *
     * @param role 역할 이름
     * @param permission 권한 코드
     * @return 권한 보유 여부
     */
    public boolean hasPermission(String role, String permission) {
        Set<String> permissions = rolePermissions.get(normalizeRole(role));
        if (permissions == null) {
            return false;
        }

        // 완전 일치 체크
        if (permissions.contains(permission)) {
            return true;
        }

        // 와일드카드 체크 (예: "USER:*"는 모든 USER 액션 허용)
        if (permission.contains(":")) {
            String resource = permission.substring(0, permission.indexOf(":"));
            String wildcardPermission = resource + ":*";
            if (permissions.contains(wildcardPermission)) {
                return true;
            }
        }

        // 전체 와일드카드 체크
        return permissions.contains("*:*") || permissions.contains("*");
    }

    /**
     * 역할의 모든 권한 조회
     *
     * @param role 역할 이름
     * @return 권한 목록 (읽기 전용)
     */
    public Set<String> getPermissions(String role) {
        Set<String> permissions = rolePermissions.get(normalizeRole(role));
        return permissions != null ? Collections.unmodifiableSet(permissions) : Collections.emptySet();
    }

    /**
     * 모든 역할 조회
     *
     * @return 역할 목록 (읽기 전용)
     */
    public Set<String> getAllRoles() {
        return Collections.unmodifiableSet(rolePermissions.keySet());
    }

    /**
     * 전체 매핑 조회
     *
     * @return Role -> Permissions 매핑 (읽기 전용)
     */
    public Map<String, Set<String>> getAllRolePermissions() {
        Map<String, Set<String>> result = new HashMap<>();
        rolePermissions.forEach((role, permissions) ->
                result.put(role, Collections.unmodifiableSet(permissions))
        );
        return Collections.unmodifiableMap(result);
    }

    /**
     * 특정 권한을 가진 역할 목록 조회
     *
     * @param permission 권한 코드
     * @return 역할 목록
     */
    public Set<String> getRolesWithPermission(String permission) {
        Set<String> roles = new HashSet<>();
        rolePermissions.forEach((role, permissions) -> {
            if (permissions.contains(permission)) {
                roles.add(role);
            }
        });
        return roles;
    }

    /**
     * 모든 매핑 초기화
     */
    public void clear() {
        int roleCount = rolePermissions.size();
        rolePermissions.clear();
        log.warn("[RolePermissionRegistry] Cleared all role-permission mappings: roleCount={}", roleCount);
    }

    /**
     * 역할 이름 정규화 (ROLE_ prefix 제거)
     */
    private String normalizeRole(String role) {
        return role.startsWith("ROLE_") ? role.substring(5) : role;
    }
}
