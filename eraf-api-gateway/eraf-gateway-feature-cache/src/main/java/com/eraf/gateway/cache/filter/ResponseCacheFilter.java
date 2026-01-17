package com.eraf.gateway.cache.filter;

import com.eraf.gateway.cache.domain.CachedResponse;
import com.eraf.gateway.cache.domain.CacheRule;
import com.eraf.gateway.cache.repository.ResponseCacheRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * 응답 캐싱 필터
 */
@Slf4j
@RequiredArgsConstructor
public class ResponseCacheFilter extends OncePerRequestFilter {

    private final ResponseCacheRepository cacheRepository;
    private final List<CacheRule> rules;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 캐시 가능한 규칙 찾기
        Optional<CacheRule> matchingRule = rules.stream()
                .filter(r -> r.isEnabled() && r.matchesPath(path) && r.matchesMethod(method))
                .findFirst();

        if (matchingRule.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        CacheRule rule = matchingRule.get();
        String cacheKey = buildCacheKey(request, rule);

        // 캐시 조회
        Optional<CachedResponse> cached = cacheRepository.get(cacheKey);
        if (cached.isPresent() && !cached.get().isExpired()) {
            log.debug("Cache HIT for key: {}", cacheKey);
            serveCachedResponse(response, cached.get());
            return;
        }

        log.debug("Cache MISS for key: {}", cacheKey);

        // 응답 캡처
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, responseWrapper);

        // 성공 응답만 캐시
        int status = responseWrapper.getStatus();
        if (status >= 200 && status < 300) {
            CachedResponse cachedResponse = CachedResponse.builder()
                    .statusCode(status)
                    .headers(extractHeaders(responseWrapper))
                    .body(responseWrapper.getContentAsByteArray())
                    .contentType(responseWrapper.getContentType())
                    .cachedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(rule.getTtlSeconds()))
                    .build();

            cacheRepository.put(cacheKey, cachedResponse);
            log.debug("Cached response for key: {}, TTL: {}s", cacheKey, rule.getTtlSeconds());
        }

        responseWrapper.copyBodyToResponse();
    }

    private String buildCacheKey(HttpServletRequest request, CacheRule rule) {
        StringBuilder key = new StringBuilder();
        key.append(request.getMethod()).append(":").append(request.getRequestURI());

        if (rule.isVaryByQueryParams() && request.getQueryString() != null) {
            key.append("?").append(request.getQueryString());
        }

        if (rule.isVaryByHeaders() && rule.getVaryHeaders() != null) {
            for (String header : rule.getVaryHeaders()) {
                String value = request.getHeader(header);
                if (value != null) {
                    key.append("|").append(header).append("=").append(value);
                }
            }
        }

        return key.toString();
    }

    private void serveCachedResponse(HttpServletResponse response, CachedResponse cached) throws IOException {
        response.setStatus(cached.getStatusCode());

        if (cached.getContentType() != null) {
            response.setContentType(cached.getContentType());
        }

        cached.getHeaders().forEach(response::setHeader);

        response.setHeader("X-Cache", "HIT");
        response.setHeader("X-Cache-TTL", String.valueOf(cached.getRemainingTtlSeconds()));

        if (cached.getBody() != null) {
            response.getOutputStream().write(cached.getBody());
        }
    }

    private Map<String, String> extractHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        response.getHeaderNames().forEach(name -> {
            if (!name.equalsIgnoreCase("Set-Cookie")) { // 쿠키는 캐시하지 않음
                headers.put(name, response.getHeader(name));
            }
        });
        return headers;
    }
}
