package com.eraf.openapi.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Consumer Group 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerGroupResponse {

    private Long id;
    private String name;
    private String description;
    private Integer rateLimit;
    private Integer rateLimitWindowSeconds;
    private Boolean enabled;
    private Integer memberCount;

    @Builder.Default
    private List<ConsumerSummary> members = new ArrayList<>();

    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;

    /**
     * Consumer 요약 정보 (그룹 멤버 목록용)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumerSummary {
        private Long id;
        private String username;
        private Boolean enabled;
    }
}
