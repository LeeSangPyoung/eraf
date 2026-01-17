package com.eraf.gateway.validation.service;

import com.eraf.gateway.validation.domain.ValidationResult;
import com.eraf.gateway.validation.domain.ValidationRule;
import com.eraf.gateway.validation.validator.ContentTypeValidator;
import com.eraf.gateway.validation.validator.JsonSchemaValidator;
import com.eraf.gateway.validation.validator.OpenApiValidator;
import com.eraf.gateway.validation.validator.RequestSizeValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Validation Service
 * 요청 검증 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final JsonSchemaValidator jsonSchemaValidator;
    private final OpenApiValidator openApiValidator;
    private final RequestSizeValidator requestSizeValidator;
    private final ContentTypeValidator contentTypeValidator;

    /**
     * 요청 검증
     *
     * @param request HTTP 요청
     * @param rule    검증 규칙
     * @return 검증 결과
     */
    public ValidationResult validateRequest(HttpServletRequest request, ValidationRule rule) {
        if (!rule.isEnabled()) {
            return ValidationResult.success();
        }

        ValidationResult result = ValidationResult.success();

        // 1. Content-Type 검증
        if (rule.needsContentTypeValidation()) {
            ValidationResult contentTypeResult = contentTypeValidator.validate(
                    request, rule.getAllowedContentTypes());
            result.merge(contentTypeResult);

            if (contentTypeResult.hasErrors()) {
                return result; // Content-Type이 잘못되면 추가 검증 중단
            }
        }

        // 2. 바디 크기 검증
        if (rule.needsBodySizeValidation()) {
            ValidationResult sizeResult = requestSizeValidator.validateContentLength(
                    request, rule.getMaxBodySize());
            result.merge(sizeResult);

            if (sizeResult.hasErrors()) {
                return result; // 크기 초과시 추가 검증 중단
            }
        }

        // 3. 헤더 검증
        if (rule.isValidateHeaders() && !rule.getRequiredHeaders().isEmpty()) {
            ValidationResult headerResult = validateHeaders(
                    rule.getRequiredHeaders(), request);
            result.merge(headerResult);
        }

        // 4. 쿼리 파라미터 검증
        if (rule.isValidateQueryParams() && !rule.getRequiredQueryParams().isEmpty()) {
            ValidationResult queryResult = validateQueryParams(
                    rule.getRequiredQueryParams(), request);
            result.merge(queryResult);
        }

        // 5. 바디 검증 (JSON Schema 또는 OpenAPI)
        if (rule.isValidateBody()) {
            String body = extractRequestBody(request);

            if (body != null && !body.isEmpty()) {
                // JSON Schema 검증
                if (rule.needsJsonSchemaValidation()) {
                    ValidationResult schemaResult = validateJsonSchema(body, rule.getJsonSchema());
                    result.merge(schemaResult);
                }

                // OpenAPI 검증
                if (rule.needsOpenApiValidation()) {
                    ValidationResult openApiResult = validateOpenApiSpec(
                            request, rule.getOpenApiOperationId());
                    result.merge(openApiResult);
                }

                // 필수 필드 검증 (간단한 JSON 파싱)
                if (!rule.getRequiredFields().isEmpty()) {
                    ValidationResult fieldsResult = validateRequiredFields(body, rule.getRequiredFields());
                    result.merge(fieldsResult);
                }
            } else if (!rule.getRequiredFields().isEmpty()) {
                // 바디가 없는데 필수 필드가 있는 경우
                result.addError("Request body is required");
            }
        }

        return result;
    }

    /**
     * JSON Schema 검증
     *
     * @param json   JSON 문자열
     * @param schema JSON Schema 문자열
     * @return 검증 결과
     */
    public ValidationResult validateJsonSchema(String json, String schema) {
        if (json == null || json.isEmpty()) {
            return ValidationResult.failure("JSON body is required");
        }

        if (schema == null || schema.isEmpty()) {
            return ValidationResult.failure("JSON Schema is not configured");
        }

        return jsonSchemaValidator.validate(json, schema);
    }

    /**
     * OpenAPI 스펙 검증
     *
     * @param request     HTTP 요청
     * @param operationId OpenAPI Operation ID
     * @return 검증 결과
     */
    public ValidationResult validateOpenApiSpec(HttpServletRequest request, String operationId) {
        // OpenAPI 스펙 URL은 설정에서 가져와야 함 (여기서는 간단히 처리)
        String openApiSpec = (String) request.getAttribute("openapi.spec");
        if (openApiSpec == null) {
            return ValidationResult.failure("OpenAPI specification not configured");
        }

        return openApiValidator.validateRequest(request, operationId, openApiSpec);
    }

    /**
     * 헤더 검증
     *
     * @param requiredHeaders 필수 헤더 목록
     * @param request         HTTP 요청
     * @return 검증 결과
     */
    public ValidationResult validateHeaders(Set<String> requiredHeaders, HttpServletRequest request) {
        ValidationResult result = ValidationResult.success();

        for (String header : requiredHeaders) {
            String value = request.getHeader(header);
            if (value == null || value.isEmpty()) {
                result.addFieldError(header, "Required header '" + header + "' is missing");
            }
        }

        return result;
    }

    /**
     * 쿼리 파라미터 검증
     *
     * @param requiredParams 필수 쿼리 파라미터 목록
     * @param request        HTTP 요청
     * @return 검증 결과
     */
    public ValidationResult validateQueryParams(Set<String> requiredParams, HttpServletRequest request) {
        ValidationResult result = ValidationResult.success();

        for (String param : requiredParams) {
            String value = request.getParameter(param);
            if (value == null || value.isEmpty()) {
                result.addFieldError(param, "Required query parameter '" + param + "' is missing");
            }
        }

        return result;
    }

    /**
     * 필수 필드 검증 (간단한 JSON 파싱)
     */
    private ValidationResult validateRequiredFields(String json, Set<String> requiredFields) {
        ValidationResult result = ValidationResult.success();

        try {
            // 간단한 JSON 파싱 (실제로는 Jackson을 사용해야 함)
            for (String field : requiredFields) {
                String pattern = "\"" + field + "\"";
                if (!json.contains(pattern)) {
                    result.addFieldError(field, "Required field '" + field + "' is missing");
                }
            }
        } catch (Exception e) {
            result.addError("Failed to parse JSON body");
        }

        return result;
    }

    /**
     * 요청 바디 추출
     */
    private String extractRequestBody(HttpServletRequest request) {
        try {
            if (request instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
                byte[] content = wrapper.getContentAsByteArray();
                return new String(content, StandardCharsets.UTF_8);
            } else {
                // 일반 요청인 경우 InputStream 읽기 (한 번만 읽을 수 있음)
                return StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Failed to extract request body", e);
            return null;
        }
    }

    /**
     * 캐시 초기화
     */
    public void clearCache() {
        jsonSchemaValidator.clearCache();
        openApiValidator.clearCache();
        log.info("Validation cache cleared");
    }
}
