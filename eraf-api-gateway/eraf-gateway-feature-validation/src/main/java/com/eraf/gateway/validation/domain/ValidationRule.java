package com.eraf.gateway.validation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * Validation Rule
 * 경로별 검증 규칙
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRule {

    /**
     * 경로 패턴 (예: /api/users/**, /api/orders/*)
     */
    private String pathPattern;

    /**
     * HTTP 메서드 (null이면 모든 메서드)
     */
    private String method;

    /**
     * JSON Schema (JSON 검증용)
     */
    private String jsonSchema;

    /**
     * OpenAPI Operation ID (OpenAPI 검증용)
     */
    private String openApiOperationId;

    /**
     * 최대 요청 바디 크기 (bytes, null이면 무제한)
     */
    private Long maxBodySize;

    /**
     * 허용되는 Content-Type 목록
     */
    @Builder.Default
    private List<String> allowedContentTypes = new ArrayList<>();

    /**
     * 필수 헤더 목록
     */
    @Builder.Default
    private Set<String> requiredHeaders = new HashSet<>();

    /**
     * 필수 쿼리 파라미터 목록
     */
    @Builder.Default
    private Set<String> requiredQueryParams = new HashSet<>();

    /**
     * 필수 필드 목록 (JSON body)
     */
    @Builder.Default
    private Set<String> requiredFields = new HashSet<>();

    /**
     * 검증 활성화 여부
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Strict 모드 (알 수 없는 필드에 대해 실패)
     */
    @Builder.Default
    private boolean strictMode = false;

    /**
     * 헤더 검증 활성화
     */
    @Builder.Default
    private boolean validateHeaders = true;

    /**
     * 쿼리 파라미터 검증 활성화
     */
    @Builder.Default
    private boolean validateQueryParams = true;

    /**
     * 바디 검증 활성화
     */
    @Builder.Default
    private boolean validateBody = true;

    /**
     * 추가 메타데이터
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 생성 시간
     */
    private Long createdAt;

    /**
     * 수정 시간
     */
    private Long updatedAt;

    /**
     * HTTP 메서드 매칭 확인
     */
    public boolean matchesMethod(String requestMethod) {
        return method == null || method.equalsIgnoreCase(requestMethod);
    }

    /**
     * Content-Type 허용 여부 확인
     */
    public boolean isContentTypeAllowed(String contentType) {
        if (allowedContentTypes.isEmpty()) {
            return true;
        }

        if (contentType == null) {
            return false;
        }

        // Content-Type에서 charset 제거 (application/json;charset=UTF-8 -> application/json)
        String normalizedContentType = contentType.split(";")[0].trim().toLowerCase();

        return allowedContentTypes.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(normalizedContentType));
    }

    /**
     * JSON Schema 검증이 필요한지 확인
     */
    public boolean needsJsonSchemaValidation() {
        return validateBody && jsonSchema != null && !jsonSchema.isEmpty();
    }

    /**
     * OpenAPI 검증이 필요한지 확인
     */
    public boolean needsOpenApiValidation() {
        return validateBody && openApiOperationId != null && !openApiOperationId.isEmpty();
    }

    /**
     * 바디 크기 검증이 필요한지 확인
     */
    public boolean needsBodySizeValidation() {
        return validateBody && maxBodySize != null && maxBodySize > 0;
    }

    /**
     * Content-Type 검증이 필요한지 확인
     */
    public boolean needsContentTypeValidation() {
        return validateBody && !allowedContentTypes.isEmpty();
    }
}
