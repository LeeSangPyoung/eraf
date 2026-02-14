package com.eraf.web.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * RequestContext 자동 설정 필터
 * HTTP 요청 정보를 RequestContext에 바인딩하고 MDC에 전파
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestContextFilter.class);

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // RequestContext 생성 및 설정
            RequestContext context = buildRequestContext(request);
            RequestContextHolder.setContext(context);

            // MDC 전파
            MdcContextPropagator.propagate(context);

            // 응답 헤더에 Trace ID, Request ID 추가
            response.setHeader(TRACE_ID_HEADER, context.getTraceId());
            response.setHeader(REQUEST_ID_HEADER, context.getRequestId());

            log.debug("RequestContext initialized: requestId={}, traceId={}, uri={}",
                    context.getRequestId(), context.getTraceId(), context.getUri());

            // 다음 필터 실행
            filterChain.doFilter(request, response);

        } finally {
            // RequestContext 정리
            RequestContextHolder.clear();
            MdcContextPropagator.clear();

            log.debug("RequestContext cleared");
        }
    }

    /**
     * HTTP 요청에서 RequestContext 생성
     */
    private RequestContext buildRequestContext(HttpServletRequest request) {
        // Trace ID (헤더 또는 생성)
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        // Request ID (헤더 또는 생성)
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        // Session ID
        String sessionId = null;
        HttpSession session = request.getSession(false);
        if (session != null) {
            sessionId = session.getId();
        }

        // User ID, Username (헤더 또는 Spring Security에서 추출)
        String userId = request.getHeader(USER_ID_HEADER);
        String username = extractUsernameFromSecurity();

        // userId가 null이고 username이 있으면 userId에 할당
        if (userId == null && username != null) {
            userId = username;
        }

        // Client IP
        String clientIp = extractClientIp(request);

        // User-Agent
        String userAgent = request.getHeader("User-Agent");

        // Locale
        Locale locale = request.getLocale();

        // Headers (선택적)
        Map<String, String> headers = extractHeaders(request);

        return RequestContext.builder()
                .requestId(requestId)
                .traceId(traceId)
                .sessionId(sessionId)
                .userId(userId)
                .username(username)
                .clientIp(clientIp)
                .userAgent(userAgent)
                .method(request.getMethod())
                .uri(request.getRequestURI())
                .queryString(request.getQueryString())
                .locale(locale)
                .headers(headers)
                .build();
    }

    /**
     * Client IP 추출 (프록시 고려)
     */
    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For에 여러 IP가 있을 경우 첫 번째 IP 추출
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 주요 헤더 추출
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();

                // 주요 헤더만 저장 (보안 및 성능)
                if (isImportantHeader(headerName)) {
                    String headerValue = request.getHeader(headerName);
                    headers.put(headerName, headerValue);
                }
            }
        }

        return headers;
    }

    /**
     * Spring Security에서 사용자 이름 추출 (optional - Reflection 사용)
     * Spring Security가 classpath에 없어도 정상 동작
     */
    private String extractUsernameFromSecurity() {
        try {
            // Spring Security가 classpath에 있는 경우에만 실행
            Class<?> securityContextHolderClass = Class.forName(
                    "org.springframework.security.core.context.SecurityContextHolder");

            Object securityContext = securityContextHolderClass
                    .getMethod("getContext")
                    .invoke(null);

            Object authentication = securityContext.getClass()
                    .getMethod("getAuthentication")
                    .invoke(securityContext);

            if (authentication != null) {
                Boolean isAuthenticated = (Boolean) authentication.getClass()
                        .getMethod("isAuthenticated")
                        .invoke(authentication);

                if (Boolean.TRUE.equals(isAuthenticated)) {
                    return (String) authentication.getClass()
                            .getMethod("getName")
                            .invoke(authentication);
                }
            }
        } catch (ClassNotFoundException e) {
            // Spring Security가 없음 - 정상적인 상황
            log.trace("Spring Security not found in classpath");
        } catch (Exception e) {
            // 기타 오류 - 무시
            log.trace("Failed to extract username from Spring Security: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 중요한 헤더인지 확인
     */
    private boolean isImportantHeader(String headerName) {
        if (headerName == null) {
            return false;
        }

        String lowerName = headerName.toLowerCase();

        // 저장할 헤더 목록
        return lowerName.equals("content-type")
                || lowerName.equals("accept")
                || lowerName.equals("accept-language")
                || lowerName.equals("user-agent")
                || lowerName.equals("referer")
                || lowerName.equals("origin")
                || lowerName.startsWith("x-");  // 커스텀 헤더 (X-)
    }
}
