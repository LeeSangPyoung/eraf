package com.eraf.security.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RolePermissionRegistry 단위 테스트
 */
class RolePermissionRegistryTest {

    private RolePermissionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new RolePermissionRegistry();
    }

    @Test
    void addPermission_ShouldAddPermissionToRole() {
        // given
        String role = "ADMIN";
        String permission = "USER:READ";

        // when
        registry.addPermission(role, permission);

        // then
        assertThat(registry.hasPermission(role, permission)).isTrue();
    }

    @Test
    void addRolePermissions_ShouldAddMultiplePermissions() {
        // given
        String role = "ADMIN";
        Set<String> permissions = Set.of("USER:READ", "USER:WRITE", "USER:DELETE");

        // when
        registry.addRolePermissions(role, permissions);

        // then
        assertThat(registry.hasPermission(role, "USER:READ")).isTrue();
        assertThat(registry.hasPermission(role, "USER:WRITE")).isTrue();
        assertThat(registry.hasPermission(role, "USER:DELETE")).isTrue();
    }

    @Test
    void hasPermission_ShouldReturnFalseForNonExistentPermission() {
        // given
        String role = "USER";
        String permission = "USER:DELETE";

        // when & then
        assertThat(registry.hasPermission(role, permission)).isFalse();
    }

    @Test
    void hasPermission_ShouldSupportWildcard() {
        // given
        String role = "ADMIN";
        registry.addPermission(role, "USER:*");

        // when & then
        assertThat(registry.hasPermission(role, "USER:READ")).isTrue();
        assertThat(registry.hasPermission(role, "USER:WRITE")).isTrue();
        assertThat(registry.hasPermission(role, "USER:DELETE")).isTrue();
    }

    @Test
    void hasPermission_ShouldSupportGlobalWildcard() {
        // given
        String role = "SUPERADMIN";
        registry.addPermission(role, "*:*");

        // when & then
        assertThat(registry.hasPermission(role, "USER:READ")).isTrue();
        assertThat(registry.hasPermission(role, "ORDER:DELETE")).isTrue();
        assertThat(registry.hasPermission(role, "PRODUCT:WRITE")).isTrue();
    }

    @Test
    void removePermission_ShouldRemovePermissionFromRole() {
        // given
        String role = "ADMIN";
        String permission = "USER:DELETE";
        registry.addPermission(role, permission);

        // when
        registry.removePermission(role, permission);

        // then
        assertThat(registry.hasPermission(role, permission)).isFalse();
    }

    @Test
    void removeRole_ShouldRemoveAllPermissionsFromRole() {
        // given
        String role = "ADMIN";
        registry.addRolePermissions(role, Set.of("USER:READ", "USER:WRITE"));

        // when
        registry.removeRole(role);

        // then
        assertThat(registry.hasPermission(role, "USER:READ")).isFalse();
        assertThat(registry.hasPermission(role, "USER:WRITE")).isFalse();
    }

    @Test
    void normalizeRole_ShouldHandleRolePrefix() {
        // given
        registry.addPermission("ROLE_ADMIN", "USER:READ");

        // when & then
        assertThat(registry.hasPermission("ADMIN", "USER:READ")).isTrue();
        assertThat(registry.hasPermission("ROLE_ADMIN", "USER:READ")).isTrue();
    }

    @Test
    void getPermissions_ShouldReturnAllPermissionsForRole() {
        // given
        String role = "ADMIN";
        Set<String> expected = Set.of("USER:READ", "USER:WRITE", "USER:DELETE");
        registry.addRolePermissions(role, expected);

        // when
        Set<String> actual = registry.getPermissions(role);

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void getAllRoles_ShouldReturnAllRegisteredRoles() {
        // given
        registry.addPermission("ADMIN", "USER:READ");
        registry.addPermission("USER", "PROFILE:READ");

        // when
        Set<String> roles = registry.getAllRoles();

        // then
        assertThat(roles).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    void clear_ShouldRemoveAllMappings() {
        // given
        registry.addPermission("ADMIN", "USER:READ");
        registry.addPermission("USER", "PROFILE:READ");

        // when
        registry.clear();

        // then
        assertThat(registry.getAllRoles()).isEmpty();
    }
}
