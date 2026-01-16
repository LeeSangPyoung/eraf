package com.eraf.gateway.filter;

import com.eraf.core.http.HttpUtils;
import com.eraf.core.utils.PathMatcher;
import com.eraf.gateway.domain.ApiKey;
import com.eraf.gateway.exception.GatewayErrorCode;
import com.eraf.gateway.exception.InvalidApiKeyException;
import com.eraf.gateway.service.ApiKeyService;
import com.eraf.gateway.util.GatewayResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * API Key 인증 필터
 */
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;
    private final String headerName;
    private final List<String> excludePatterns;

    /**
     * 인증된 API Key를 request attribute에 저장할 때 사용하는 키
     */
    public static final String API_KEY_ATTRIBUTE = "ERAF_API_KEY";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 제외 패턴 확인
        if (shouldExclude(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKeyValue = extractApiKey(request);
        String clientIp = HttpUtils.getClientIp(request);

        try {
            ApiKey apiKey = apiKeyService.authenticate(apiKeyValue, path, clientIp);

            // 인증된 API Key를 request에 저장
            request.setAttribute(API_KEY_ATTRIBUTE, apiKey);

            filterChain.doFilter(request, response);

        } catch (InvalidApiKeyException e) {
            log.warn("API key authentication failed for path: {} - {}", path, e.getMessage());

            response.setHeader("WWW-Authenticate", "ApiKey realm=\"API\"");

            // 에러 코드에 따라 적절한 GatewayErrorCode 선택
            GatewayErrorCode errorCode = mapToErrorCode(e.getCode());
            GatewayResponseUtils.sendError(response, errorCode, e.getMessage());
        }
    }

    private GatewayErrorCode mapToErrorCode(String code) {
        if (code == null) {
            return GatewayErrorCode.API_KEY_INVALID;
        }
        return switch (code) {
            case "API_KEY_MISSING" -> GatewayErrorCode.API_KEY_MISSING;
            case "API_KEY_EXPIRED" -> GatewayErrorCode.API_KEY_EXPIRED;
            case "API_KEY_DISABLED" -> GatewayErrorCode.API_KEY_DISABLED;
            case "API_KEY_PATH_NOT_ALLOWED" -> GatewayErrorCode.API_KEY_PATH_NOT_ALLOWED;
            case "API_KEY_IP_NOT_ALLOWED" -> GatewayErrorCode.API_KEY_IP_NOT_ALLOWED;
            default -> GatewayErrorCode.API_KEY_INVALID;
        };
    }

    private String extractApiKey(HttpServletRequest request) {
        // 1. 헤더에서 확인
        String apiKey = request.getHeader(headerName);
        if (apiKey != null && !apiKey.isEmpty()) {
            return apiKey;
        }

        // 2. Authorization 헤더에서 확인 (Bearer 형식)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("ApiKey ")) {
            return authHeader.substring(7);
        }

        // 3. 쿼리 파라미터에서 확인 (보안상 비권장)
        return request.getParameter("api_key");
    }

    private boolean shouldExclude(String path) {
        return PathMatcher.matchesAny(path, excludePatterns);
    }
}
