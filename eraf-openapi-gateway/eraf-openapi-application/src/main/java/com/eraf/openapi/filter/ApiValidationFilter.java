package com.eraf.openapi.filter;

import com.eraf.openapi.core.domain.GatewayApi;
import com.eraf.openapi.core.repository.GatewayApiRepository;
import com.eraf.openapi.validation.ApiValidationService;
import com.eraf.openapi.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * API Registry + Validation Filter
 * <p>
 * 등록된 API만 통과시키고, 등록되지 않은 API는 차단합니다.
 * Validation이 활성화된 경우 추가 검증을 수행합니다.
 * <p>
 * Order: 100 (RequestLoggingFilter(0) 이후, Auth Filter(200) 이전)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiValidationFilter implements WebFilter, Ordered {

    private final GatewayApiRepository apiRepository;
    private final ApiValidationService validationService;

    public static final String MATCHED_API_ATTR = "MATCHED_API";
    private static final int ORDER = 100;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

        log.debug("ApiValidationFilter: {} {}", method, path);

        // CORS preflight OPTIONS 요청은 검증 제외 (브라우저가 인증 전에 먼저 보냄)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.debug("Skipping API validation for OPTIONS request: {}", path);
            return chain.filter(exchange);
        }

        // Admin API는 Gateway 로컬 엔드포인트이므로 라우팅 검증 제외 (인증은 JwtAuthFilter에서 처리)
        if (path.startsWith("/admin/")) {
            log.debug("Skipping routing validation for local Admin API: {} (auth will be handled by JwtAuthFilter)", path);
            return chain.filter(exchange);
        }

        // 매칭되는 API 찾기 (프록시 대상 API만)
        return findMatchingApi(path, method)
                .flatMap(api -> {
                    // 3. API가 비활성화된 경우 차단
                    if (!api.getEnabled()) {
                        log.warn("API is disabled: {} {} (id={})", method, path, api.getId());
                        return sendErrorResponse(
                                exchange,
                                HttpStatus.FORBIDDEN,
                                "API is disabled",
                                String.format("API %s %s is currently disabled", method, path)
                        );
                    }

                    // 4. Exchange Attributes에 매칭된 API 저장 (다운스트림 필터에서 사용 가능)
                    exchange.getAttributes().put(MATCHED_API_ATTR, api);

                    log.debug("API matched: {} {} -> API[id={}, name={}]",
                            method, path, api.getId(), api.getName());

                    // 5. Validation 활성화된 경우 검증 수행
                    if (Boolean.TRUE.equals(api.getValidationEnabled())) {
                        return validationService.validateRequest(exchange, api)
                                .flatMap(result -> {
                                    if (!result.isValid()) {
                                        log.warn("Validation failed for API: {} {} - {}",
                                                method, path, result.getMessage());
                                        return sendValidationErrorResponse(exchange, result);
                                    }
                                    return chain.filter(exchange);
                                });
                    }

                    // 6. Validation 비활성화면 바로 통과
                    return chain.filter(exchange);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // 7. 매칭되는 API가 없는 경우 차단
                    log.warn("No registered API found: {} {}", method, path);
                    return sendErrorResponse(
                            exchange,
                            HttpStatus.NOT_FOUND,
                            "API not found",
                            String.format("No registered API found for %s %s", method, path)
                    );
                }));
    }

    /**
     * 매칭되는 API 찾기
     * <p>
     * 1) 먼저 정확한 경로 매칭 시도 (DB Index 활용)
     * 2) 없으면 활성화된 API 중 패턴 매칭 (우선순위 순)
     */
    private Mono<GatewayApi> findMatchingApi(String path, String method) {
        // 1. 정확한 경로 매칭 (빠른 조회)
        return Mono.fromCallable(() -> apiRepository.findByPathAndMethod(path, method))
                .flatMap(exactMatch -> {
                    if (exactMatch.isPresent()) {
                        return Mono.just(exactMatch.get());
                    }
                    // 2. 패턴 매칭 (활성화된 API 중 메서드가 같은 것만)
                    return Mono.fromCallable(() -> apiRepository.findMatchingCandidates(method))
                            .flatMap(candidates -> findFirstPatternMatch(candidates, path));
                });
    }

    /**
     * 패턴 매칭으로 첫 번째 일치하는 API 찾기
     */
    private Mono<GatewayApi> findFirstPatternMatch(List<GatewayApi> candidates, String path) {
        Optional<GatewayApi> matched = candidates.stream()
                .filter(api -> api.matchesPath(path))
                .findFirst();  // Priority 순으로 정렬되어 있으므로 첫 번째가 최우선

        return matched.map(Mono::just).orElseGet(Mono::empty);
    }

    /**
     * 에러 응답 전송
     */
    private Mono<Void> sendErrorResponse(
            ServerWebExchange exchange,
            HttpStatus status,
            String error,
            String message) {

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                java.time.Instant.now().toString(),
                status.value(),
                error,
                message,
                exchange.getRequest().getPath().value()
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(json.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * Validation 에러 응답 전송
     */
    private Mono<Void> sendValidationErrorResponse(
            ServerWebExchange exchange,
            ValidationResult result) {

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorsJson = result.getErrors() != null
                ? "[\"" + String.join("\",\"", result.getErrors()) + "\"]"
                : "[]";

        String json = String.format(
                "{\"timestamp\":\"%s\",\"status\":400,\"error\":\"Validation Failed\"," +
                        "\"message\":\"%s\",\"validationType\":\"%s\",\"errors\":%s,\"path\":\"%s\"}",
                java.time.Instant.now().toString(),
                result.getMessage() != null ? result.getMessage() : "Request validation failed",
                result.getFailureType() != null ? result.getFailureType().name() : "UNKNOWN",
                errorsJson,
                exchange.getRequest().getPath().value()
        );

        DataBuffer buffer = response.bufferFactory()
                .wrap(json.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}
