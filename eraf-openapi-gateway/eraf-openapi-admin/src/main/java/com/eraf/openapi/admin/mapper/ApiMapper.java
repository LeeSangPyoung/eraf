package com.eraf.openapi.admin.mapper;

import com.eraf.openapi.admin.dto.ApiRequest;
import com.eraf.openapi.admin.dto.ApiResponse;
import com.eraf.openapi.core.domain.GatewayApi;
import com.eraf.openapi.core.domain.GatewayRoute;
import com.eraf.openapi.core.domain.GatewayService;
import org.springframework.stereotype.Component;

/**
 * API Entity ↔ DTO 변환 Mapper
 */
@Component
public class ApiMapper {

    /**
     * Request DTO를 Entity로 변환
     *
     * @param request API 생성/수정 요청
     * @param service 연결된 서비스 (필수)
     * @param route 연결된 라우트 (선택)
     * @return GatewayApi Entity
     */
    public GatewayApi toEntity(ApiRequest request, GatewayService service, GatewayRoute route) {
        return GatewayApi.builder()
                // Basic
                .name(request.getName())
                .path(request.getPath())
                .method(request.getMethod())
                .service(service)
                .route(route)
                .description(request.getDescription())
                .enabled(request.getEnabled())
                .priority(request.getPriority())
                // Authentication
                .authRequired(request.getAuthRequired())
                .authType(request.getAuthType())
                .requiredRoles(request.getRequiredRoles())
                // Validation
                .validationEnabled(request.getValidationEnabled())
                .validateHeaders(request.getValidateHeaders())
                .validateQueryParams(request.getValidateQueryParams())
                .validateBody(request.getValidateBody())
                .validateResponse(request.getValidateResponse())
                .responseSchema(request.getResponseSchema())
                .openApiOperationId(request.getOpenApiOperationId())
                .requiredHeaders(request.getRequiredHeaders())
                .requiredQueryParams(request.getRequiredQueryParams())
                .jsonSchema(request.getJsonSchema())
                .allowedContentTypes(request.getAllowedContentTypes())
                .requiredFields(request.getRequiredFields())
                .maxBodySize(request.getMaxBodySize())
                // Rate Limiting
                .rateLimitEnabled(request.getRateLimitEnabled())
                .rateLimitRequests(request.getRateLimitRequests())
                .rateLimitWindowSeconds(request.getRateLimitWindowSeconds())
                .rateLimitKey(request.getRateLimitKey())
                // IP Restriction
                .ipRestrictionEnabled(request.getIpRestrictionEnabled())
                .allowedIps(request.getAllowedIps())
                .blockedIps(request.getBlockedIps())
                // Cache
                .cacheEnabled(request.getCacheEnabled())
                .cacheTtlSeconds(request.getCacheTtlSeconds())
                .cacheKey(request.getCacheKey())
                // Timeout
                .timeoutMs(request.getTimeoutMs())
                // Metadata
                .tags(request.getTags())
                .metadata(request.getMetadata())
                .build();
    }

    /**
     * 기존 Entity를 Request DTO로 업데이트
     *
     * @param api 업데이트할 API Entity
     * @param request API 수정 요청
     * @param service 연결된 서비스 (필수)
     * @param route 연결된 라우트 (선택)
     */
    public void updateEntity(GatewayApi api, ApiRequest request, GatewayService service, GatewayRoute route) {
        // Basic
        api.setName(request.getName());
        api.setPath(request.getPath());
        api.setMethod(request.getMethod());
        api.setService(service);
        api.setRoute(route);
        api.setDescription(request.getDescription());
        api.setEnabled(request.getEnabled());
        api.setPriority(request.getPriority());
        // Authentication
        api.setAuthRequired(request.getAuthRequired());
        api.setAuthType(request.getAuthType());
        api.setRequiredRoles(request.getRequiredRoles());
        // Validation
        api.setValidationEnabled(request.getValidationEnabled());
        api.setValidateHeaders(request.getValidateHeaders());
        api.setValidateQueryParams(request.getValidateQueryParams());
        api.setValidateBody(request.getValidateBody());
        api.setValidateResponse(request.getValidateResponse());
        api.setResponseSchema(request.getResponseSchema());
        api.setOpenApiOperationId(request.getOpenApiOperationId());
        api.setRequiredHeaders(request.getRequiredHeaders());
        api.setRequiredQueryParams(request.getRequiredQueryParams());
        api.setJsonSchema(request.getJsonSchema());
        api.setAllowedContentTypes(request.getAllowedContentTypes());
        api.setRequiredFields(request.getRequiredFields());
        api.setMaxBodySize(request.getMaxBodySize());
        // Rate Limiting
        api.setRateLimitEnabled(request.getRateLimitEnabled());
        api.setRateLimitRequests(request.getRateLimitRequests());
        api.setRateLimitWindowSeconds(request.getRateLimitWindowSeconds());
        api.setRateLimitKey(request.getRateLimitKey());
        // IP Restriction
        api.setIpRestrictionEnabled(request.getIpRestrictionEnabled());
        api.setAllowedIps(request.getAllowedIps());
        api.setBlockedIps(request.getBlockedIps());
        // Cache
        api.setCacheEnabled(request.getCacheEnabled());
        api.setCacheTtlSeconds(request.getCacheTtlSeconds());
        api.setCacheKey(request.getCacheKey());
        // Timeout
        api.setTimeoutMs(request.getTimeoutMs());
        // Metadata
        api.setTags(request.getTags());
        api.setMetadata(request.getMetadata());
    }

    /**
     * Entity를 Response DTO로 변환
     *
     * @param api GatewayApi Entity
     * @return ApiResponse DTO
     */
    public ApiResponse toResponse(GatewayApi api) {
        return ApiResponse.builder()
                // Basic
                .id(api.getId())
                .name(api.getName())
                .path(api.getPath())
                .method(api.getMethod())
                .serviceId(api.getService() != null ? api.getService().getId() : null)
                .serviceName(api.getService() != null ? api.getService().getName() : null)
                .routeId(api.getRoute() != null ? api.getRoute().getId() : null)
                .routeName(api.getRoute() != null ? api.getRoute().getName() : null)
                .description(api.getDescription())
                .enabled(api.getEnabled())
                .priority(api.getPriority())
                // Authentication
                .authRequired(api.getAuthRequired())
                .authType(api.getAuthType())
                .requiredRoles(api.getRequiredRoles())
                // Validation
                .validationEnabled(api.getValidationEnabled())
                .validateHeaders(api.getValidateHeaders())
                .validateQueryParams(api.getValidateQueryParams())
                .validateBody(api.getValidateBody())
                .validateResponse(api.getValidateResponse())
                .responseSchema(api.getResponseSchema())
                .openApiOperationId(api.getOpenApiOperationId())
                .requiredHeaders(api.getRequiredHeaders())
                .requiredQueryParams(api.getRequiredQueryParams())
                .jsonSchema(api.getJsonSchema())
                .allowedContentTypes(api.getAllowedContentTypes())
                .requiredFields(api.getRequiredFields())
                .maxBodySize(api.getMaxBodySize())
                // Rate Limiting
                .rateLimitEnabled(api.getRateLimitEnabled())
                .rateLimitRequests(api.getRateLimitRequests())
                .rateLimitWindowSeconds(api.getRateLimitWindowSeconds())
                .rateLimitKey(api.getRateLimitKey())
                // IP Restriction
                .ipRestrictionEnabled(api.getIpRestrictionEnabled())
                .allowedIps(api.getAllowedIps())
                .blockedIps(api.getBlockedIps())
                // Cache
                .cacheEnabled(api.getCacheEnabled())
                .cacheTtlSeconds(api.getCacheTtlSeconds())
                .cacheKey(api.getCacheKey())
                // Timeout
                .timeoutMs(api.getTimeoutMs())
                // Metadata
                .tags(api.getTags())
                .metadata(api.getMetadata())
                // Timestamps
                .createdBy(api.getCreatedBy())
                .createdAt(api.getCreatedAt())
                .updatedAt(api.getUpdatedAt())
                .build();
    }
}
