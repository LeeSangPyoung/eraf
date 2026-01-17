package com.eraf.gateway.apikey.filter;

import com.eraf.core.http.HttpUtils;
import com.eraf.gateway.apikey.domain.ApiKey;
import com.eraf.gateway.apikey.exception.InvalidApiKeyException;
import com.eraf.gateway.apikey.service.ApiKeyService;
import com.eraf.gateway.common.exception.GatewayErrorCode;
import com.eraf.gateway.common.filter.GatewayFilter;
import com.eraf.gateway.common.util.GatewayResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * API Key 인증 필터
 */
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends GatewayFilter {

    private final ApiKeyService apiKeyService;
    private final String headerName;
    private final boolean enabled;

    /**
     * 인증된 API Key를 request attribute에 저장할 때 사용하는 키
     */
    public static final String API_KEY_ATTRIBUTE = "ERAF_API_KEY";

    @Override
    protected void doFilterInternal(HttpServletRequest request, ServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = request.getRequestURI();

        String apiKeyValue = extractApiKey(request);
        String clientIp = HttpUtils.getClientIp(request);

        try {
            ApiKey apiKey = apiKeyService.authenticate(apiKeyValue, path, clientIp);

            // 인증된 API Key를 request에 저장
            request.setAttribute(API_KEY_ATTRIBUTE, apiKey);

            filterChain.doFilter(request, response);

        } catch (InvalidApiKeyException e) {
            log.warn("API key authentication failed for path: {} - {}", path, e.getMessage());

            httpResponse.setHeader("WWW-Authenticate", "ApiKey realm=\"API\"");

            // 에러 코드에 따라 적절한 GatewayErrorCode 선택
            GatewayErrorCode errorCode = mapToErrorCode(e.getErrorCode().getCode());
            GatewayResponseUtils.sendError(httpResponse, errorCode, e.getMessage());
        }
    }

    @Override
    protected boolean isEnabled() {
        return enabled;
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
}
