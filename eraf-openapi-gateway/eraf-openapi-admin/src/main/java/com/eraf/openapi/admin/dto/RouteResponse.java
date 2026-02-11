package com.eraf.openapi.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Route 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {

    private Long id;
    private String name;
    private List<String> paths;
    private List<String> methods;
    private List<String> hosts;
    private Map<String, String> headers;
    private Long serviceId;
    private String serviceName;
    private Integer priority;
    private Boolean stripPath;
    private String pathPrefix;
    private String apiVersion;
    private String description;
    private Map<String, Object> metadata;
    private Boolean enabled;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
