package com.eraf.openapi.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    // ========== Basic Information ==========

    private Long id;
    private String name;
    private String path;
    private String method;
    private Long serviceId;
    private String serviceName;  // For display
    private Long routeId;
    private String routeName;  // For display
    private String description;
    private Boolean enabled;
    private Integer priority;

    // ========== Authentication ==========

    private Boolean authRequired;
    private String authType;
    private List<String> requiredRoles;

    // ========== Validation ==========

    private Boolean validationEnabled;
    private Boolean validateHeaders;
    private Boolean validateQueryParams;
    private Boolean validateBody;
    private Boolean validateResponse;
    private String responseSchema;
    private String openApiOperationId;
    private Map<String, String> requiredHeaders;
    private List<String> requiredQueryParams;
    private String jsonSchema;
    private List<String> allowedContentTypes;
    private List<String> requiredFields;
    private Long maxBodySize;

    // ========== Rate Limiting ==========

    private Boolean rateLimitEnabled;
    private Integer rateLimitRequests;
    private Integer rateLimitWindowSeconds;
    private String rateLimitKey;

    // ========== IP Restriction ==========

    private Boolean ipRestrictionEnabled;
    private List<String> allowedIps;
    private List<String> blockedIps;

    // ========== Cache ==========

    private Boolean cacheEnabled;
    private Integer cacheTtlSeconds;
    private String cacheKey;

    // ========== Timeout ==========

    private Long timeoutMs;

    // ========== Metadata ==========

    private List<String> tags;
    private Map<String, Object> metadata;

    // ========== Timestamps ==========

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
