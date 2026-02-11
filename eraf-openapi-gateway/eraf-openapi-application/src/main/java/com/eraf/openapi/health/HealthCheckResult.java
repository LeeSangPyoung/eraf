package com.eraf.openapi.health;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Health Check 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResult {

    private Long targetId;
    private String serviceName;
    private String host;
    private Integer port;
    private String healthStatus;
    private Integer consecutiveFailures;
    private LocalDateTime lastHealthCheckAt;
    private Long responseTimeMs;
    private String errorMessage;
    private Boolean enabled;
}
