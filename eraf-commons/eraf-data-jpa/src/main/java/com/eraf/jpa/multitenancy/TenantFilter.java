package com.eraf.jpa.multitenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 테넌트 식별 필터
 */
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    private final String headerName;
    private final String defaultTenantId;
    private final boolean required;

    public TenantFilter(String headerName, String defaultTenantId, boolean required) {
        this.headerName = headerName;
        this.defaultTenantId = defaultTenantId;
        this.required = required;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String tenantId = resolveTenantId(request);

            if (tenantId != null) {
                TenantContext.setTenantId(tenantId);
                log.debug("Tenant context set: {}", tenantId);
            } else if (required) {
                log.warn("Tenant ID not found and is required");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant ID is required");
                return;
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenantId(HttpServletRequest request) {
        // 1. Header에서 추출
        String tenantId = request.getHeader(headerName);
        if (StringUtils.hasText(tenantId)) {
            return tenantId;
        }

        // 2. 쿼리 파라미터에서 추출
        tenantId = request.getParameter("tenantId");
        if (StringUtils.hasText(tenantId)) {
            return tenantId;
        }

        // 3. 서브도메인에서 추출 (예: tenant1.example.com)
        String serverName = request.getServerName();
        if (serverName.contains(".")) {
            String subdomain = serverName.split("\\.")[0];
            if (!"www".equals(subdomain) && !"api".equals(subdomain)) {
                return subdomain;
            }
        }

        // 4. 기본값 반환
        return defaultTenantId;
    }
}
