package com.eraf.openapi.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Upstream 생성/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpstreamRequest {

    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{2,100}$",
            message = "Name must be 2-100 characters, alphanumeric with underscore/hyphen")
    private String name;

    private String description;

    @Builder.Default
    private String algorithm = "ROUND_ROBIN";

    @Builder.Default
    private String hashOn = "NONE";

    private String hashOnHeader;

    @Builder.Default
    private String hashFallback = "NONE";

    @Min(value = 10, message = "Slots must be at least 10")
    @Max(value = 65536, message = "Slots must be at most 65536")
    @Builder.Default
    private Integer slots = 10000;

    // Active Health Check
    @Builder.Default
    private Boolean healthcheckActiveEnabled = true;

    @Builder.Default
    private String healthcheckActivePath = "/";

    @Min(value = 1, message = "Health check interval must be at least 1 second")
    @Builder.Default
    private Integer healthcheckActiveInterval = 30;

    @Min(value = 1, message = "Health check timeout must be at least 1 second")
    @Builder.Default
    private Integer healthcheckActiveTimeout = 5;

    @Min(value = 1)
    @Builder.Default
    private Integer healthcheckActiveHealthySuccesses = 2;

    @Min(value = 1)
    @Builder.Default
    private Integer healthcheckActiveUnhealthyFailures = 3;

    private List<Integer> healthcheckActiveHealthyStatuses;

    private List<Integer> healthcheckActiveUnhealthyStatuses;

    // Passive Health Check
    @Builder.Default
    private Boolean healthcheckPassiveEnabled = true;

    @Builder.Default
    private Integer healthcheckPassiveUnhealthyFailures = 5;

    @Builder.Default
    private Integer healthcheckPassiveUnhealthyTimeouts = 3;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
