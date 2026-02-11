package com.eraf.openapi.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Route 생성/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

    @NotBlank(message = "Route name is required")
    private String name;

    @NotEmpty(message = "At least one path is required")
    private List<String> paths;

    private List<String> methods;

    private List<String> hosts;

    private Map<String, String> headers;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @Min(value = 0, message = "Priority must be non-negative")
    @Builder.Default
    private Integer priority = 0;

    @Builder.Default
    private Boolean stripPath = true;

    private String pathPrefix;

    private String apiVersion;

    private String description;

    private Map<String, Object> metadata;

    @Builder.Default
    private Boolean enabled = true;
}
