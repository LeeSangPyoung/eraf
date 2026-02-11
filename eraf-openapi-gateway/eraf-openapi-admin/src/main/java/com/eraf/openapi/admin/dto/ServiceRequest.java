package com.eraf.openapi.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Service 생성/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @NotBlank(message = "Service name is required")
    private String name;

    @NotBlank(message = "Protocol is required")
    @Pattern(regexp = "^(http|https)$", message = "Protocol must be http or https")
    private String protocol;

    @NotBlank(message = "Host is required")
    private String host;

    @Min(value = 1, message = "Port must be greater than 0")
    @Max(value = 65535, message = "Port must be less than 65536")
    private Integer port;

    private String basePath;

    @Min(value = 100, message = "Connect timeout must be at least 100ms")
    @Builder.Default
    private Integer connectTimeout = 5000;

    @Min(value = 100, message = "Read timeout must be at least 100ms")
    @Builder.Default
    private Integer readTimeout = 30000;

    @Min(value = 100, message = "Write timeout must be at least 100ms")
    @Builder.Default
    private Integer writeTimeout = 30000;

    @Min(value = 0, message = "Retries must be non-negative")
    @Builder.Default
    private Integer retries = 3;

    @Pattern(regexp = "^(ROUND_ROBIN|LEAST_CONNECTIONS|IP_HASH|WEIGHTED_ROUND_ROBIN)$",
             message = "Invalid load balancing algorithm")
    @Builder.Default
    private String loadBalancingAlgorithm = "ROUND_ROBIN";

    @Builder.Default
    private Boolean healthCheckEnabled = true;

    private String healthCheckPath;

    @Min(value = 1000, message = "Health check interval must be at least 1 second")
    @Builder.Default
    private Integer healthCheckInterval = 30000;

    @Min(value = 100, message = "Health check timeout must be at least 100ms")
    @Builder.Default
    private Integer healthCheckTimeout = 5000;

    @Min(value = 1, message = "Healthy threshold must be at least 1")
    @Builder.Default
    private Integer healthyThreshold = 2;

    @Min(value = 1, message = "Unhealthy threshold must be at least 1")
    @Builder.Default
    private Integer unhealthyThreshold = 3;

    private String description;

    private Map<String, Object> metadata;

    @Builder.Default
    private Boolean enabled = true;
}
