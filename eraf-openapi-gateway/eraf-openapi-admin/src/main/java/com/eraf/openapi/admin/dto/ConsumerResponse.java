package com.eraf.openapi.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Consumer 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerResponse {

    private Long id;
    private String username;
    private String apiKey;
    private String description;
    private Integer rateLimit;
    private Integer rateLimitWindowSeconds;
    private Boolean enabled;

    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    private String customId;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
}
