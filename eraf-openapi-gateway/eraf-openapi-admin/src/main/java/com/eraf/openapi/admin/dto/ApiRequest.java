package com.eraf.openapi.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * API 생성/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequest {

    // ========== Basic Information ==========

    @NotBlank(message = "API name is required")
    private String name;

    @NotBlank(message = "API path is required")
    private String path;

    @NotBlank(message = "HTTP method is required")
    @Pattern(regexp = "GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS",
             message = "Method must be one of: GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS")
    private String method;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    private Long routeId;  // Optional

    private String description;

    @Builder.Default
    private Boolean enabled = true;

    @Min(value = 0, message = "Priority must be non-negative")
    @Builder.Default
    private Integer priority = 0;

    // ========== Authentication ==========

    @Builder.Default
    private Boolean authRequired = false;

    private String authType;  // "JWT", "API_KEY", "BASIC", etc.

    private List<String> requiredRoles;

    // ========== Validation ==========

    @Builder.Default
    private Boolean validationEnabled = false;

    @Builder.Default
    private Boolean validateHeaders = true;

    @Builder.Default
    private Boolean validateQueryParams = true;

    @Builder.Default
    private Boolean validateBody = true;

    @Builder.Default
    private Boolean validateResponse = false;

    private String responseSchema;  // JSON Schema for response validation

    private String openApiOperationId;  // OpenAPI Operation ID

    private Map<String, String> requiredHeaders;  // {"X-API-Key": "required", "Content-Type": "application/json"}

    private List<String> requiredQueryParams;  // ["userId", "page"]

    private String jsonSchema;  // JSON Schema for body validation

    private List<String> allowedContentTypes;  // ["application/json", "application/xml"]

    private List<String> requiredFields;  // For simple required fields check

    @Min(value = 0, message = "Max body size must be non-negative")
    private Long maxBodySize;  // bytes

    // ========== Rate Limiting ==========

    @Builder.Default
    private Boolean rateLimitEnabled = false;

    @Min(value = 1, message = "Rate limit requests must be positive")
    private Integer rateLimitRequests;  // e.g., 100

    @Min(value = 1, message = "Rate limit window must be positive")
    private Integer rateLimitWindowSeconds;  // e.g., 60

    private String rateLimitKey;  // "ip", "user", "api_key", or custom

    // ========== IP Restriction ==========

    @Builder.Default
    private Boolean ipRestrictionEnabled = false;

    private List<String> allowedIps;

    private List<String> blockedIps;

    // ========== Cache ==========

    @Builder.Default
    private Boolean cacheEnabled = false;

    @Min(value = 0, message = "Cache TTL must be non-negative")
    private Integer cacheTtlSeconds;

    private String cacheKey;  // Pattern for cache key generation

    // ========== Timeout ==========

    @Min(value = 0, message = "Timeout must be non-negative")
    private Long timeoutMs;  // Request timeout in milliseconds

    // ========== Metadata ==========

    private List<String> tags;  // ["public", "v1", "beta"]

    private Map<String, Object> metadata;  // Flexible key-value storage
}
