package com.eraf.gateway.validation.validator;

import com.eraf.gateway.validation.domain.ValidationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON Schema Validator
 * JSON Schema 기반 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonSchemaValidator {

    private final ObjectMapper objectMapper;

    // Schema 캐시 (스키마 파싱 성능 최적화)
    private final ConcurrentHashMap<String, JsonSchema> schemaCache = new ConcurrentHashMap<>();

    /**
     * JSON Schema로 JSON 검증
     *
     * @param json       검증할 JSON 문자열
     * @param schemaJson JSON Schema 문자열
     * @return 검증 결과
     */
    public ValidationResult validate(String json, String schemaJson) {
        try {
            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(json);

            // Schema 파싱 (캐시 사용)
            JsonSchema schema = getOrCreateSchema(schemaJson);

            // 검증 수행
            Set<ValidationMessage> errors = schema.validate(jsonNode);

            if (errors.isEmpty()) {
                return ValidationResult.success();
            }

            // 오류 메시지 변환
            ValidationResult result = ValidationResult.builder().success(false).build();
            for (ValidationMessage error : errors) {
                String field = error.getPath();
                String message = error.getMessage();
                result.addFieldError(field, message);
            }

            return result;

        } catch (Exception e) {
            log.error("JSON Schema validation failed", e);
            return ValidationResult.failure("Invalid JSON or Schema: " + e.getMessage());
        }
    }

    /**
     * Schema 캐시에서 가져오거나 생성
     */
    private JsonSchema getOrCreateSchema(String schemaJson) {
        return schemaCache.computeIfAbsent(schemaJson, json -> {
            try {
                JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
                JsonNode schemaNode = objectMapper.readTree(json);
                return factory.getSchema(schemaNode);
            } catch (Exception e) {
                log.error("Failed to parse JSON Schema", e);
                throw new RuntimeException("Invalid JSON Schema", e);
            }
        });
    }

    /**
     * 스키마 캐시 초기화
     */
    public void clearCache() {
        schemaCache.clear();
    }

    /**
     * 캐시 크기 조회
     */
    public int getCacheSize() {
        return schemaCache.size();
    }
}
