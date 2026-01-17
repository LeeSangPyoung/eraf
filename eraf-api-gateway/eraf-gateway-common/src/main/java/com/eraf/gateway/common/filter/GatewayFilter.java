package com.eraf.gateway.common.filter;

import org.springframework.util.AntPathMatcher;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Gateway 필터 베이스 클래스
 * 공통 필터 로직 제공
 */
@Slf4j
public abstract class GatewayFilter implements Filter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private List<String> excludePatterns = Collections.emptyList();

    /**
     * 제외 패턴 설정
     */
    public void setExcludePatterns(List<String> excludePatterns) {
        this.excludePatterns = excludePatterns != null ? excludePatterns : Collections.emptyList();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String requestPath = httpRequest.getRequestURI();

        // 제외 패턴 체크
        if (shouldExclude(requestPath)) {
            log.debug("Path {} excluded from {} filter", requestPath, getFilterName());
            chain.doFilter(request, response);
            return;
        }

        // 필터 활성화 여부 체크
        if (!isEnabled()) {
            log.debug("{} filter is disabled, skipping", getFilterName());
            chain.doFilter(request, response);
            return;
        }

        // 실제 필터 로직 실행
        doFilterInternal(httpRequest, response, chain);
    }

    /**
     * 경로가 제외 패턴에 매칭되는지 확인
     */
    protected boolean shouldExclude(String path) {
        for (String pattern : excludePatterns) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 필터 활성화 여부 (기본값: true)
     * 서브클래스에서 오버라이드하여 설정에 따라 필터를 활성화/비활성화 가능
     */
    protected boolean isEnabled() {
        return true;
    }

    /**
     * 필터 이름 반환
     */
    protected String getFilterName() {
        return getClass().getSimpleName();
    }

    /**
     * 실제 필터 로직
     * 서브클래스에서 구현
     */
    protected abstract void doFilterInternal(HttpServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException;
}
