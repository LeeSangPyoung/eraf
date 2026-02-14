package com.eraf.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErafJpaProperties 테스트
 */
class ErafJpaPropertiesTest {

    private ErafJpaProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ErafJpaProperties();
    }

    @Test
    @DisplayName("기본 Auditing 설정")
    void shouldHaveDefaultAuditingSettings() {
        assertTrue(properties.isAuditingEnabled());
    }

    @Test
    @DisplayName("Auditing 비활성화")
    void shouldDisableAuditing() {
        properties.setAuditingEnabled(false);

        assertFalse(properties.isAuditingEnabled());
    }

    @Test
    @DisplayName("MultiTenancy 기본 설정")
    void shouldHaveMultiTenancyDefaults() {
        ErafJpaProperties.MultiTenancy mt = properties.getMultiTenancy();

        assertNotNull(mt);
        assertFalse(mt.isEnabled());
        assertEquals("X-Tenant-ID", mt.getHeaderName());
    }

    @Test
    @DisplayName("MultiTenancy 활성화")
    void shouldEnableMultiTenancy() {
        ErafJpaProperties.MultiTenancy mt = new ErafJpaProperties.MultiTenancy();
        mt.setEnabled(true);
        mt.setHeaderName("X-Organization");
        mt.setDefaultTenantId("default-org");

        properties.setMultiTenancy(mt);

        assertTrue(properties.getMultiTenancy().isEnabled());
        assertEquals("X-Organization", properties.getMultiTenancy().getHeaderName());
        assertEquals("default-org", properties.getMultiTenancy().getDefaultTenantId());
    }

    @Test
    @DisplayName("MultiTenancy 설정 변경")
    void shouldAllowMultiTenancyConfigurationChanges() {
        ErafJpaProperties.MultiTenancy mt = properties.getMultiTenancy();

        mt.setEnabled(true);
        mt.setHeaderName("Tenant");
        mt.setDefaultTenantId("tenant-1");
        mt.setRequired(true);

        assertTrue(mt.isEnabled());
        assertEquals("Tenant", mt.getHeaderName());
        assertEquals("tenant-1", mt.getDefaultTenantId());
        assertTrue(mt.isRequired());
    }

    @Test
    @DisplayName("새 MultiTenancy 인스턴스 할당")
    void shouldAssignNewMultiTenancyInstance() {
        ErafJpaProperties.MultiTenancy newMt = new ErafJpaProperties.MultiTenancy();
        newMt.setEnabled(true);

        properties.setMultiTenancy(newMt);

        assertSame(newMt, properties.getMultiTenancy());
    }
}
