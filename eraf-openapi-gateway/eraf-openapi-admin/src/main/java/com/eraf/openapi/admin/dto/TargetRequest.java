package com.eraf.openapi.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Target 생성/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    private Long upstreamId;

    @NotBlank(message = "Host is required")
    private String host;

    @NotNull(message = "Port is required")
    @Min(value = 1, message = "Port must be greater than 0")
    @Max(value = 65535, message = "Port must be less than 65536")
    private Integer port;

    @Min(value = 1, message = "Weight must be at least 1")
    @Max(value = 1000, message = "Weight must be at most 1000")
    @Builder.Default
    private Integer weight = 100;

    private String description;

    @Builder.Default
    private Boolean enabled = true;
}
