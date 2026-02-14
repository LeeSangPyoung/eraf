package com.eraf.security.ip;

import com.eraf.security.audit.SecurityAuditLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * IP 접근 제어 필터
 *
 * <p>화이트리스트/블랙리스트 기반으로 IP 주소를 검증하여 접근을 제어합니다.
 */
public class IpAccessControlFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IpAccessControlFilter.class);

    private final IpAccessControlProperties properties;
    private final IpValidator ipValidator;
    private final SecurityAuditLogger auditLogger;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public IpAccessControlFilter(
            IpAccessControlProperties properties,
            SecurityAuditLogger auditLogger) {
        this.properties = properties;
        this.ipValidator = new IpValidator(
            properties.getWhitelist(),
            properties.getBlacklist()
        );
        this.auditLogger = auditLogger;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // Exclude patterns 확인
        if (shouldExclude(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Include patterns 확인 (설정되어 있으면)
        if (!shouldInclude(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 클라이언트 IP 추출
        String clientIp = extractClientIp(request);

        // IP 검증
        boolean allowed = ipValidator.isAllowed(clientIp, properties.getDefaultPolicy());

        if (!allowed) {
            handleDeniedAccess(request, response, clientIp);
            return;
        }

        // 허용된 IP - 다음 필터로 진행
        log.debug("IP access granted: {} from {}", requestUri, clientIp);
        filterChain.doFilter(request, response);
    }

    /**
     * 클라이언트 IP 주소 추출
     * 프록시 환경을 고려하여 X-Forwarded-For, X-Real-IP 헤더 확인
     */
    private String extractClientIp(HttpServletRequest request) {
        // X-Forwarded-For 헤더 (프록시 환경)
        if (properties.isUseForwardedFor()) {
            String forwardedFor = request.getHeader("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isEmpty()) {
                // 첫 번째 IP가 실제 클라이언트 IP
                String[] ips = forwardedFor.split(",");
                return ips[0].trim();
            }
        }

        // X-Real-IP 헤더
        if (properties.isUseRealIp()) {
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isEmpty()) {
                return realIp.trim();
            }
        }

        // 기본 remote address
        return request.getRemoteAddr();
    }

    /**
     * 접근 차단 처리
     */
    private void handleDeniedAccess(
            HttpServletRequest request,
            HttpServletResponse response,
            String clientIp) throws IOException {

        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        log.warn("IP access denied: {} {} from {}", method, requestUri, clientIp);

        // 감사 로그 기록
        if (properties.isAuditOnDenied()) {
            auditLogger.logIpAccessDenied(clientIp, requestUri, method);
        }

        // 403 Forbidden 응답
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String jsonResponse = String.format(
            "{\"error\":\"Forbidden\",\"message\":\"%s\",\"path\":\"%s\"}",
            properties.getDeniedMessage(),
            requestUri
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * URL이 제외 패턴에 매칭되는지 확인
     */
    private boolean shouldExclude(String requestUri) {
        List<String> excludePatterns = properties.getExcludePatterns();
        if (excludePatterns.isEmpty()) {
            return false;
        }

        for (String pattern : excludePatterns) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * URL이 포함 패턴에 매칭되는지 확인
     * 포함 패턴이 없으면 모든 요청에 적용
     */
    private boolean shouldInclude(String requestUri) {
        List<String> includePatterns = properties.getIncludePatterns();
        if (includePatterns.isEmpty()) {
            return true; // 모든 요청에 적용
        }

        for (String pattern : includePatterns) {
            if (pathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }
        return false;
    }
}
