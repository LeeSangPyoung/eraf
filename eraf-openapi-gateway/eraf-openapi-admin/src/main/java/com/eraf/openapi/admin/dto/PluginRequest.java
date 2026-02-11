package com.eraf.openapi.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Plugin 생성/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginRequest {

    @NotBlank(message = "Plugin name is required")
    @Pattern(regexp = "^(rate-limit|jwt-auth|api-key|circuit-breaker|cache|analytics|cors|request-transformer|response-transformer|ip-restriction|bot-detection|logging|retry|timeout)$",
             message = "Invalid plugin name")
    private String name;

    @NotNull(message = "Scope is required")
    @Pattern(regexp = "^(GLOBAL|SERVICE|ROUTE|CONSUMER_GROUP)$", message = "Scope must be GLOBAL, SERVICE, ROUTE, or CONSUMER_GROUP")
    private String scope;

    private Long routeId;

    private Long serviceId;

    private Long consumerGroupId;

    @NotNull(message = "Plugin configuration is required")
    private Map<String, Object> config;

    @Min(value = 0, message = "Priority must be non-negative")
    @Builder.Default
    private Integer priority = 0;

    private String description;

    @Builder.Default
    private Boolean enabled = true;
}
