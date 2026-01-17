package com.eraf.gateway.validation.validator;

import com.eraf.gateway.validation.domain.ValidationResult;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenAPI Validator
 * OpenAPI 3.0 스펙 기반 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenApiValidator {

    // OpenAPI 문서 캐시
    private final ConcurrentHashMap<String, OpenAPI> openApiCache = new ConcurrentHashMap<>();

    // Operation ID -> Operation 매핑 캐시
    private final ConcurrentHashMap<String, OperationInfo> operationCache = new ConcurrentHashMap<>();

    /**
     * OpenAPI 스펙으로 요청 검증
     *
     * @param request     HTTP 요청
     * @param operationId OpenAPI Operation ID
     * @param openApiSpec OpenAPI 스펙 URL 또는 YAML/JSON 문자열
     * @return 검증 결과
     */
    public ValidationResult validateRequest(HttpServletRequest request, String operationId, String openApiSpec) {
        try {
            // OpenAPI 파싱
            OpenAPI openApi = getOrParseOpenApi(openApiSpec);

            // Operation 조회
            OperationInfo operationInfo = findOperation(openApi, operationId);
            if (operationInfo == null) {
                return ValidationResult.failure("Operation not found: " + operationId);
            }

            Operation operation = operationInfo.operation;
            ValidationResult result = ValidationResult.success();

            // 파라미터 검증 (헤더, 쿼리, 경로)
            if (operation.getParameters() != null) {
                ValidationResult paramResult = validateParameters(request, operation.getParameters());
                result.merge(paramResult);
            }

            // Request Body 검증
            if (operation.getRequestBody() != null) {
                ValidationResult bodyResult = validateRequestBody(request, operation.getRequestBody());
                result.merge(bodyResult);
            }

            return result;

        } catch (Exception e) {
            log.error("OpenAPI validation failed", e);
            return ValidationResult.failure("OpenAPI validation error: " + e.getMessage());
        }
    }

    /**
     * OpenAPI 파싱 (캐시 사용)
     */
    private OpenAPI getOrParseOpenApi(String spec) {
        return openApiCache.computeIfAbsent(spec, s -> {
            SwaggerParseResult result = new OpenAPIV3Parser().readContents(s, null, null);
            if (result.getMessages() != null && !result.getMessages().isEmpty()) {
                log.warn("OpenAPI parsing warnings: {}", result.getMessages());
            }
            return result.getOpenAPI();
        });
    }

    /**
     * Operation ID로 Operation 찾기
     */
    private OperationInfo findOperation(OpenAPI openApi, String operationId) {
        String cacheKey = System.identityHashCode(openApi) + ":" + operationId;

        return operationCache.computeIfAbsent(cacheKey, key -> {
            if (openApi.getPaths() == null) {
                return null;
            }

            for (Map.Entry<String, PathItem> pathEntry : openApi.getPaths().entrySet()) {
                PathItem pathItem = pathEntry.getValue();

                for (PathItem.HttpMethod method : PathItem.HttpMethod.values()) {
                    Operation operation = pathItem.getOperation(method);
                    if (operation != null && operationId.equals(operation.getOperationId())) {
                        return new OperationInfo(pathEntry.getKey(), method.name(), operation);
                    }
                }
            }
            return null;
        });
    }

    /**
     * 파라미터 검증 (헤더, 쿼리, 경로)
     */
    private ValidationResult validateParameters(HttpServletRequest request, List<Parameter> parameters) {
        ValidationResult result = ValidationResult.success();

        for (Parameter param : parameters) {
            String paramName = param.getName();
            String paramIn = param.getIn();
            boolean required = param.getRequired() != null && param.getRequired();

            String value = null;

            switch (paramIn) {
                case "header":
                    value = request.getHeader(paramName);
                    break;
                case "query":
                    value = request.getParameter(paramName);
                    break;
                case "path":
                    // Path 파라미터는 URI에서 추출 필요 (간단 구현)
                    value = extractPathParam(request, paramName);
                    break;
            }

            if (required && (value == null || value.isEmpty())) {
                result.addFieldError(paramName, "Required " + paramIn + " parameter '" + paramName + "' is missing");
            }

            // 스키마 검증 (타입, 포맷 등)
            if (value != null && param.getSchema() != null) {
                ValidationResult schemaResult = validateParameterSchema(paramName, value, param.getSchema());
                result.merge(schemaResult);
            }
        }

        return result;
    }

    /**
     * Request Body 검증
     */
    private ValidationResult validateRequestBody(HttpServletRequest request, RequestBody requestBody) {
        ValidationResult result = ValidationResult.success();

        boolean required = requestBody.getRequired() != null && requestBody.getRequired();
        String contentType = request.getContentType();

        if (required && contentType == null) {
            result.addError("Request body is required");
            return result;
        }

        // Content 검증
        Content content = requestBody.getContent();
        if (content != null && contentType != null) {
            String normalizedContentType = contentType.split(";")[0].trim();
            MediaType mediaType = content.get(normalizedContentType);

            if (mediaType == null) {
                result.addError("Unsupported content type: " + contentType);
            }
        }

        return result;
    }

    /**
     * 파라미터 스키마 검증 (간단 구현)
     */
    private ValidationResult validateParameterSchema(String paramName, String value, Schema<?> schema) {
        ValidationResult result = ValidationResult.success();

        // 타입 검증
        String type = schema.getType();
        if ("integer".equals(type)) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                result.addFieldError(paramName, "Parameter '" + paramName + "' must be an integer");
            }
        } else if ("number".equals(type)) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                result.addFieldError(paramName, "Parameter '" + paramName + "' must be a number");
            }
        } else if ("boolean".equals(type)) {
            if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                result.addFieldError(paramName, "Parameter '" + paramName + "' must be a boolean");
            }
        }

        // Enum 검증
        List<?> enumValues = schema.getEnum();
        if (enumValues != null && !enumValues.isEmpty()) {
            if (!enumValues.contains(value)) {
                result.addFieldError(paramName, "Parameter '" + paramName + "' must be one of: " + enumValues);
            }
        }

        return result;
    }

    /**
     * 경로 파라미터 추출 (간단 구현)
     */
    private String extractPathParam(HttpServletRequest request, String paramName) {
        // 실제로는 경로 패턴 매칭 로직이 필요
        // 여기서는 간단히 null 반환
        return null;
    }

    /**
     * 캐시 초기화
     */
    public void clearCache() {
        openApiCache.clear();
        operationCache.clear();
    }

    /**
     * Operation 정보
     */
    private static class OperationInfo {
        final String path;
        final String method;
        final Operation operation;

        OperationInfo(String path, String method, Operation operation) {
            this.path = path;
            this.method = method;
            this.operation = operation;
        }
    }
}
