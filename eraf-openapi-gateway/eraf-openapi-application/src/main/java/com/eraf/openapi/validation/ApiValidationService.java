package com.eraf.openapi.validation;

import com.eraf.openapi.core.domain.GatewayApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * API Request Validation 서비스
 * <p>
 * 등록된 API의 검증 규칙에 따라 요청을 검증합니다.
 */
@Slf4j
@Service
public class ApiValidationService {

    /**
     * API 요청 검증
     *
     * @param exchange ServerWebExchange
     * @param api      검증할 API 정보
     * @return ValidationResult (성공/실패 및 상세 정보)
     */
    public Mono<ValidationResult> validateRequest(ServerWebExchange exchange, GatewayApi api) {
        ServerHttpRequest request = exchange.getRequest();

        try {
            // 1. Required Headers 검증
            if (api.getRequiredHeaders() != null && !api.getRequiredHeaders().isEmpty()) {
                ValidationResult headerResult = validateHeaders(request.getHeaders(), api.getRequiredHeaders());
                if (!headerResult.isValid()) {
                    return Mono.just(headerResult);
                }
            }

            // 2. Required Query Parameters 검증
            if (api.getRequiredQueryParams() != null && !api.getRequiredQueryParams().isEmpty()) {
                ValidationResult queryResult = validateQueryParams(
                        request.getQueryParams(),
                        api.getRequiredQueryParams()
                );
                if (!queryResult.isValid()) {
                    return Mono.just(queryResult);
                }
            }

            // 3. Content-Type 검증 (POST, PUT, PATCH 메서드인 경우)
            String method = request.getMethod().name();
            if (("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))
                    && api.getAllowedContentTypes() != null && !api.getAllowedContentTypes().isEmpty()) {
                ValidationResult contentTypeResult = validateContentType(
                        request.getHeaders(),
                        api.getAllowedContentTypes()
                );
                if (!contentTypeResult.isValid()) {
                    return Mono.just(contentTypeResult);
                }
            }

            // 4. Body 크기 검증 (향후 구현 가능)
            // Note: Spring Cloud Gateway에서 body 읽기는 복잡하므로 일단 생략
            // Body 검증이 필요한 경우 ModifyRequestBodyGatewayFilterFactory 활용 필요

            // 5. JSON Schema 검증 (향후 구현 가능)
            // Note: JSON Schema validation은 body를 읽어야 하므로 일단 생략
            // 필요시 json-schema-validator 라이브러리 활용

            return Mono.just(ValidationResult.success());

        } catch (Exception e) {
            log.error("Validation error for API: {} {}", api.getMethod(), api.getPath(), e);
            return Mono.just(ValidationResult.failure(
                    ValidationResult.ValidationType.INVALID_JSON,
                    "Validation processing error: " + e.getMessage()
            ));
        }
    }

    /**
     * Required Headers 검증
     */
    private ValidationResult validateHeaders(HttpHeaders headers, Map<String, String> requiredHeaders) {
        ValidationResult result = ValidationResult.builder()
                .valid(true)
                .failureType(ValidationResult.ValidationType.MISSING_HEADER)
                .build();

        for (Map.Entry<String, String> entry : requiredHeaders.entrySet()) {
            String headerName = entry.getKey();
            String expectedValue = entry.getValue();

            if (!headers.containsKey(headerName)) {
                result.setValid(false);
                result.addError("Missing required header: " + headerName);
            } else if (expectedValue != null && !expectedValue.isEmpty()
                    && !"*".equals(expectedValue)) {
                // 특정 값이 지정된 경우 값도 검증
                String actualValue = headers.getFirst(headerName);
                if (!expectedValue.equals(actualValue)) {
                    result.setValid(false);
                    result.addError("Invalid header value: " + headerName +
                            " (expected: " + expectedValue + ", actual: " + actualValue + ")");
                }
            }
        }

        if (!result.isValid()) {
            result.setMessage("Required headers validation failed");
        }

        return result;
    }

    /**
     * Required Query Parameters 검증
     */
    private ValidationResult validateQueryParams(
            MultiValueMap<String, String> queryParams,
            List<String> requiredParams) {

        ValidationResult result = ValidationResult.builder()
                .valid(true)
                .failureType(ValidationResult.ValidationType.MISSING_QUERY_PARAM)
                .build();

        for (String paramName : requiredParams) {
            if (!queryParams.containsKey(paramName)) {
                result.setValid(false);
                result.addError("Missing required query parameter: " + paramName);
            }
        }

        if (!result.isValid()) {
            result.setMessage("Required query parameters validation failed");
        }

        return result;
    }

    /**
     * Content-Type 검증
     */
    private ValidationResult validateContentType(HttpHeaders headers, List<String> allowedContentTypes) {
        String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);

        if (contentType == null) {
            return ValidationResult.failure(
                    ValidationResult.ValidationType.INVALID_CONTENT_TYPE,
                    "Content-Type header is required"
            );
        }

        // Content-Type에 charset 등이 포함될 수 있으므로 prefix 매칭
        boolean matches = allowedContentTypes.stream()
                .anyMatch(allowed -> contentType.toLowerCase().startsWith(allowed.toLowerCase()));

        if (!matches) {
            ValidationResult result = ValidationResult.failure(
                    ValidationResult.ValidationType.INVALID_CONTENT_TYPE,
                    "Content-Type not allowed: " + contentType
            );
            result.addError("Allowed Content-Types: " + String.join(", ", allowedContentTypes));
            return result;
        }

        return ValidationResult.success();
    }
}
