package com.eraf.openapi.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
 * Certificate 생성/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequest {

    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]{2,100}$",
            message = "Name must be 2-100 characters, alphanumeric with underscore/hyphen/dot")
    private String name;

    private String description;

    @NotBlank(message = "Certificate (PEM) is required")
    private String cert;

    @NotBlank(message = "Private key is required")
    private String privateKey;

    private String certChain;

    @Builder.Default
    private List<String> snis = new ArrayList<>();

    private String issuer;

    private String subject;

    private LocalDateTime validFrom;

    private LocalDateTime expiresAt;

    @Builder.Default
    private String certType = "STANDARD";

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Map<String, String> tags = new HashMap<>();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
