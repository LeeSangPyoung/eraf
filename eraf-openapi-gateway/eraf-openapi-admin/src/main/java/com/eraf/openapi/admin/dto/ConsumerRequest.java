package com.eraf.openapi.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumer 생성/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumerRequest {

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,50}$",
            message = "Username must be 3-50 characters, alphanumeric with underscore/hyphen")
    private String username;

    private String description;

    @Min(value = 1, message = "Rate limit must be at least 1")
    private Integer rateLimit;

    @Min(value = 1, message = "Rate limit window must be at least 1 second")
    @Builder.Default
    private Integer rateLimitWindowSeconds = 60;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    private String customId;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
