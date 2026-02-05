package com.eraf.jpa.multitenancy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TenantContext 테스트
 */
class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("테넌트 ID 설정 및 조회")
    void shouldSetAndGetTenantId() {
        TenantContext.setTenantId("tenant-123");

        assertEquals("tenant-123", TenantContext.getTenantId());
    }

    @Test
    @DisplayName("테넌트 ID 없을 때 null 반환")
    void shouldReturnNullWhenNoTenant() {
        assertNull(TenantContext.getTenantId());
    }

    @Test
    @DisplayName("clear 후 null 반환")
    void shouldReturnNullAfterClear() {
        TenantContext.setTenantId("tenant-123");

        TenantContext.clear();

        assertNull(TenantContext.getTenantId());
    }

    @Test
    @DisplayName("테넌트 ID 덮어쓰기")
    void shouldOverwriteTenantId() {
        TenantContext.setTenantId("tenant-1");
        TenantContext.setTenantId("tenant-2");

        assertEquals("tenant-2", TenantContext.getTenantId());
    }

    @Test
    @DisplayName("다른 스레드는 독립적인 테넌트")
    void shouldHaveIndependentTenantPerThread() throws InterruptedException {
        TenantContext.setTenantId("main-tenant");

        final String[] otherThreadTenant = new String[1];

        Thread otherThread = new Thread(() -> {
            TenantContext.setTenantId("other-tenant");
            otherThreadTenant[0] = TenantContext.getTenantId();
        });

        otherThread.start();
        otherThread.join();

        // 메인 스레드는 원래 테넌트 유지
        assertEquals("main-tenant", TenantContext.getTenantId());

        // 다른 스레드는 자신의 테넌트
        assertEquals("other-tenant", otherThreadTenant[0]);
    }

    @Test
    @DisplayName("한 스레드 clear가 다른 스레드에 영향 없음")
    void shouldNotAffectOtherThreadsOnClear() throws InterruptedException {
        TenantContext.setTenantId("main-tenant");

        Thread otherThread = new Thread(() -> {
            TenantContext.setTenantId("other-tenant");
            TenantContext.clear();
        });

        otherThread.start();
        otherThread.join();

        // 메인 스레드는 영향 없음
        assertEquals("main-tenant", TenantContext.getTenantId());
    }
}
