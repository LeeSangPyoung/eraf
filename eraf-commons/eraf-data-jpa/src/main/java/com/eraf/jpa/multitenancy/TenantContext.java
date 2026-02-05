package com.eraf.jpa.multitenancy;

/**
 * 테넌트 컨텍스트 홀더
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    /**
     * 현재 테넌트 ID 설정
     */
    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * 현재 테넌트 ID 조회
     */
    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * 테넌트 컨텍스트 클리어
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /**
     * 테넌트 설정 여부
     */
    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }
}
