package com.eraf.openapi.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Target 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetResponse {

    private Long id;
    private Long serviceId;
    private String serviceName;
    private Long upstreamId;
    private String upstreamName;
    private String host;
    private Integer port;
    private Integer weight;
    private String healthStatus;
    private Integer consecutiveFailures;
    private String description;
    private Boolean enabled;
    private LocalDateTime lastHealthCheckAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
