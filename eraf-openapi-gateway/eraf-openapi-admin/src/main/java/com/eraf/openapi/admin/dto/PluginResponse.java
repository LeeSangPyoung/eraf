package com.eraf.openapi.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Plugin 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginResponse {

    private Long id;
    private String name;
    private String scope;
    private Long routeId;
    private String routeName;
    private Long serviceId;
    private String serviceName;
    private Long consumerGroupId;
    private String consumerGroupName;
    private Map<String, Object> config;
    private Integer priority;
    private String description;
    private Boolean enabled;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
