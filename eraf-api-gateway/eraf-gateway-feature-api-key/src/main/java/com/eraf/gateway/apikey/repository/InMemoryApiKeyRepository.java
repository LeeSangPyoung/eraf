package com.eraf.gateway.apikey.repository;

import com.eraf.gateway.apikey.domain.ApiKey;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 인메모리 API Key Repository 구현체
 */
@Slf4j
public class InMemoryApiKeyRepository implements ApiKeyRepository {

    private final Map<String, ApiKey> storage = new ConcurrentHashMap<>();
    private final Map<String, String> apiKeyToIdMap = new ConcurrentHashMap<>();

    @Override
    public Optional<ApiKey> findByApiKey(String apiKey) {
        String id = apiKeyToIdMap.get(apiKey);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<ApiKey> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<ApiKey> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<ApiKey> findAllEnabled() {
        return storage.values().stream()
                .filter(ApiKey::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public ApiKey save(ApiKey apiKey) {
        if (apiKey.getId() == null) {
            apiKey = ApiKey.builder()
                    .id(UUID.randomUUID().toString())
                    .apiKey(apiKey.getApiKey())
                    .name(apiKey.getName())
                    .description(apiKey.getDescription())
                    .allowedPaths(apiKey.getAllowedPaths())
                    .allowedIps(apiKey.getAllowedIps())
                    .rateLimitPerSecond(apiKey.getRateLimitPerSecond())
                    .enabled(apiKey.isEnabled())
                    .expiresAt(apiKey.getExpiresAt())
                    .createdAt(apiKey.getCreatedAt())
                    .updatedAt(apiKey.getUpdatedAt())
                    .build();
        }

        storage.put(apiKey.getId(), apiKey);
        apiKeyToIdMap.put(apiKey.getApiKey(), apiKey.getId());

        log.debug("Saved API Key: {} (id: {})", apiKey.getName(), apiKey.getId());
        return apiKey;
    }

    @Override
    public void deleteById(String id) {
        ApiKey apiKey = storage.remove(id);
        if (apiKey != null) {
            apiKeyToIdMap.remove(apiKey.getApiKey());
            log.debug("Deleted API Key: {} (id: {})", apiKey.getName(), id);
        }
    }

    @Override
    public boolean existsByApiKey(String apiKey) {
        return apiKeyToIdMap.containsKey(apiKey);
    }
}
