package com.eraf.jpa.multitenancy;

import com.eraf.jpa.ErafJpaProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TenantFilter 테스트
 */
@ExtendWith(MockitoExtension.class)
class TenantFilterTest {

    private TenantFilter filter;
    private ErafJpaProperties properties;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        properties = new ErafJpaProperties();
        ErafJpaProperties.MultiTenancy multiTenancy = new ErafJpaProperties.MultiTenancy();
        multiTenancy.setEnabled(true);
        multiTenancy.setHeaderName("X-Tenant-ID");
        properties.setMultiTenancy(multiTenancy);

        filter = new TenantFilter("X-Tenant-ID", null, false);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("헤더에서 테넌트 ID 추출")
    void shouldExtractTenantIdFromHeader() throws ServletException, IOException {
        // Given
        request.addHeader("X-Tenant-ID", "tenant-123");

        doAnswer(inv -> {
            assertEquals("tenant-123", TenantContext.getTenantId());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("테넌트 ID 없이 요청 시 기본값")
    void shouldUseDefaultWhenNoTenantId() throws ServletException, IOException {
        // Given - 헤더 없음
        filter = new TenantFilter("X-Tenant-ID", "default-tenant", false);

        doAnswer(inv -> {
            assertEquals("default-tenant", TenantContext.getTenantId());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("필터 완료 후 컨텍스트 정리")
    void shouldClearContextAfterFilter() throws ServletException, IOException {
        // Given
        request.addHeader("X-Tenant-ID", "tenant-123");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(TenantContext.getTenantId());
    }

    @Test
    @DisplayName("예외 발생해도 컨텍스트 정리")
    void shouldClearContextOnException() throws ServletException, IOException {
        // Given
        request.addHeader("X-Tenant-ID", "tenant-123");
        doThrow(new RuntimeException("error")).when(filterChain).doFilter(request, response);

        // When & Then
        assertThrows(RuntimeException.class, () ->
                filter.doFilterInternal(request, response, filterChain));

        assertNull(TenantContext.getTenantId());
    }

    @Test
    @DisplayName("커스텀 헤더명 지원")
    void shouldSupportCustomHeaderName() throws ServletException, IOException {
        // Given
        filter = new TenantFilter("X-Organization-ID", null, false);

        request.addHeader("X-Organization-ID", "org-456");

        doAnswer(inv -> {
            assertEquals("org-456", TenantContext.getTenantId());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("빈 테넌트 ID는 무시")
    void shouldIgnoreEmptyTenantId() throws ServletException, IOException {
        // Given
        filter = new TenantFilter("X-Tenant-ID", "default", false);
        request.addHeader("X-Tenant-ID", "");

        doAnswer(inv -> {
            assertEquals("default", TenantContext.getTenantId());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("공백만 있는 테넌트 ID는 무시")
    void shouldIgnoreWhitespaceTenantId() throws ServletException, IOException {
        // Given
        filter = new TenantFilter("X-Tenant-ID", "default", false);
        request.addHeader("X-Tenant-ID", "   ");

        doAnswer(inv -> {
            assertEquals("default", TenantContext.getTenantId());
            return null;
        }).when(filterChain).doFilter(request, response);

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }
}
