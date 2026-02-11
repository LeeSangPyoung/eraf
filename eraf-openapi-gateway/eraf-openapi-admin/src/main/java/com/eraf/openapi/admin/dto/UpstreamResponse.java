package com.eraf.openapi.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Upstream 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpstreamResponse {

    private Long id;
    private String name;
    private String description;
    private String algorithm;
    private String hashOn;
    private String hashOnHeader;
    private String hashFallback;
    private Integer slots;

    // Active Health Check
    private Boolean healthcheckActiveEnabled;
    private String healthcheckActivePath;
    private Integer healthcheckActiveInterval;
    private Integer healthcheckActiveTimeout;
    private Integer healthcheckActiveHealthySuccesses;
    private Integer healthcheckActiveUnhealthyFailures;
    private List<Integer> healthcheckActiveHealthyStatuses;
    private List<Integer> healthcheckActiveUnhealthyStatuses;

    // Passive Health Check
    private Boolean healthcheckPassiveEnabled;
    private Integer healthcheckPassiveUnhealthyFailures;
    private Integer healthcheckPassiveUnhealthyTimeouts;

    private Boolean enabled;
    private Integer targetCount;

    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
}
