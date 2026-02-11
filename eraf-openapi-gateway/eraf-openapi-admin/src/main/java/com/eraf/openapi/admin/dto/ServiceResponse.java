package com.eraf.openapi.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

    private Long id;
    private String name;
    private String protocol;
    private String host;
    private Integer port;
    private String basePath;
    private Integer connectTimeout;
    private Integer readTimeout;
    private Integer writeTimeout;
    private Integer retries;
    private String loadBalancingAlgorithm;
    private Boolean healthCheckEnabled;
    private String healthCheckPath;
    private Integer healthCheckInterval;
    private Integer healthCheckTimeout;
    private Integer healthyThreshold;
    private Integer unhealthyThreshold;
    private String description;
    private Map<String, Object> metadata;
    private Boolean enabled;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 연관 데이터
    private List<TargetResponse> targets;
    private Integer activeTargetCount;
}
