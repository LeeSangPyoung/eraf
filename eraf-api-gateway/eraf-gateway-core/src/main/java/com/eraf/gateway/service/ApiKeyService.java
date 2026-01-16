package com.eraf.gateway.service;

import com.eraf.gateway.domain.ApiKey;
import com.eraf.gateway.exception.GatewayErrorCode;
import com.eraf.gateway.exception.InvalidApiKeyException;
import com.eraf.gateway.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * API Key 인증 서비스
 */
@Slf4j
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository repository;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int API_KEY_LENGTH = 32;

    /**
     * API Key 인증
     * @param apiKeyValue API Key 값
     * @param path 요청 경로
     * @param clientIp 클라이언트 IP
     * @return 인증된 ApiKey
     * @throws InvalidApiKeyException 인증 실패 시
     */
    public ApiKey authenticate(String apiKeyValue, String path, String clientIp) {
        if (apiKeyValue == null || apiKeyValue.isBlank()) {
            log.warn("API key is missing");
            throw InvalidApiKeyException.missing();
        }

        Optional<ApiKey> optionalApiKey = repository.findByApiKey(apiKeyValue);

        if (optionalApiKey.isEmpty()) {
            log.warn("Invalid API key: {}", maskApiKey(apiKeyValue));
            throw InvalidApiKeyException.invalid();
        }

        ApiKey apiKey = optionalApiKey.get();

        // 유효성 검사
        if (!apiKey.isValid()) {
            log.warn("API key is disabled or expired: {}", apiKey.getName());
            throw new InvalidApiKeyException(GatewayErrorCode.API_KEY_DISABLED);
        }

        // 경로 권한 검사
        if (!apiKey.isPathAllowed(path)) {
            log.warn("API key {} not allowed for path: {}", apiKey.getName(), path);
            throw new InvalidApiKeyException(GatewayErrorCode.API_KEY_PATH_NOT_ALLOWED);
        }

        // IP 권한 검사
        if (!apiKey.isIpAllowed(clientIp)) {
            log.warn("API key {} not allowed from IP: {}", apiKey.getName(), clientIp);
            throw new InvalidApiKeyException(GatewayErrorCode.API_KEY_IP_NOT_ALLOWED);
        }

        log.debug("API key authenticated: {}", apiKey.getName());
        return apiKey;
    }

    /**
     * API Key 생성
     */
    public ApiKey createApiKey(String name, String description, Set<String> allowedPaths,
                                Set<String> allowedIps, Integer rateLimitPerSecond, LocalDateTime expiresAt) {
        String apiKeyValue = generateApiKey();

        ApiKey apiKey = ApiKey.builder()
                .id(UUID.randomUUID().toString())
                .apiKey(apiKeyValue)
                .name(name)
                .description(description)
                .allowedPaths(allowedPaths)
                .allowedIps(allowedIps)
                .rateLimitPerSecond(rateLimitPerSecond)
                .enabled(true)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return repository.save(apiKey);
    }

    /**
     * API Key 비활성화
     */
    public void disableApiKey(String id) {
        repository.findById(id).ifPresent(apiKey -> {
            ApiKey disabled = ApiKey.builder()
                    .id(apiKey.getId())
                    .apiKey(apiKey.getApiKey())
                    .name(apiKey.getName())
                    .description(apiKey.getDescription())
                    .allowedPaths(apiKey.getAllowedPaths())
                    .allowedIps(apiKey.getAllowedIps())
                    .rateLimitPerSecond(apiKey.getRateLimitPerSecond())
                    .enabled(false)
                    .expiresAt(apiKey.getExpiresAt())
                    .createdAt(apiKey.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
            repository.save(disabled);
        });
    }

    /**
     * API Key 활성화
     */
    public void enableApiKey(String id) {
        repository.findById(id).ifPresent(apiKey -> {
            ApiKey enabled = ApiKey.builder()
                    .id(apiKey.getId())
                    .apiKey(apiKey.getApiKey())
                    .name(apiKey.getName())
                    .description(apiKey.getDescription())
                    .allowedPaths(apiKey.getAllowedPaths())
                    .allowedIps(apiKey.getAllowedIps())
                    .rateLimitPerSecond(apiKey.getRateLimitPerSecond())
                    .enabled(true)
                    .expiresAt(apiKey.getExpiresAt())
                    .createdAt(apiKey.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
            repository.save(enabled);
        });
    }

    /**
     * API Key 삭제
     */
    public void deleteApiKey(String id) {
        repository.deleteById(id);
    }

    /**
     * 모든 API Key 조회
     */
    public List<ApiKey> getAllApiKeys() {
        return repository.findAll();
    }

    /**
     * API Key ID로 조회
     */
    public Optional<ApiKey> getApiKey(String id) {
        return repository.findById(id);
    }

    /**
     * API Key 재발급
     */
    public ApiKey regenerateApiKey(String id) {
        return repository.findById(id).map(apiKey -> {
            String newApiKeyValue = generateApiKey();
            ApiKey regenerated = ApiKey.builder()
                    .id(apiKey.getId())
                    .apiKey(newApiKeyValue)
                    .name(apiKey.getName())
                    .description(apiKey.getDescription())
                    .allowedPaths(apiKey.getAllowedPaths())
                    .allowedIps(apiKey.getAllowedIps())
                    .rateLimitPerSecond(apiKey.getRateLimitPerSecond())
                    .enabled(apiKey.isEnabled())
                    .expiresAt(apiKey.getExpiresAt())
                    .createdAt(apiKey.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return repository.save(regenerated);
        }).orElseThrow(() -> InvalidApiKeyException.invalid());
    }

    /**
     * 안전한 API Key 생성
     */
    private String generateApiKey() {
        byte[] bytes = new byte[API_KEY_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * API Key 마스킹 (로깅용)
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
    }
}
