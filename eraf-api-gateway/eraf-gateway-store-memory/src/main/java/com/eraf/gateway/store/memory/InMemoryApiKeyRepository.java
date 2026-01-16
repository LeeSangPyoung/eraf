package com.eraf.gateway.store.memory;

import com.eraf.gateway.domain.ApiKey;
import com.eraf.gateway.repository.ApiKeyRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-Memory API Key Repository 구현체
 */
public class InMemoryApiKeyRepository implements ApiKeyRepository {

    private final Map<String, ApiKey> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<ApiKey> findByApiKey(String apiKey) {
        return storage.values().stream()
                .filter(k -> k.getApiKey().equals(apiKey))
                .findFirst();
    }

    @Override
    public Optional<ApiKey> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<ApiKey> findAll() {
        return storage.values().stream().collect(Collectors.toList());
    }

    @Override
    public List<ApiKey> findAllEnabled() {
        return storage.values().stream()
                .filter(ApiKey::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public ApiKey save(ApiKey apiKey) {
        String id = apiKey.getId();
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }

        ApiKey toSave = ApiKey.builder()
                .id(id)
                .apiKey(apiKey.getApiKey())
                .name(apiKey.getName())
                .description(apiKey.getDescription())
                .allowedPaths(apiKey.getAllowedPaths())
                .allowedIps(apiKey.getAllowedIps())
                .rateLimitPerSecond(apiKey.getRateLimitPerSecond())
                .enabled(apiKey.isEnabled())
                .expiresAt(apiKey.getExpiresAt())
                .createdAt(apiKey.getCreatedAt() != null ? apiKey.getCreatedAt() : LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        storage.put(id, toSave);
        return toSave;
    }

    @Override
    public void deleteById(String id) {
        storage.remove(id);
    }

    @Override
    public boolean existsByApiKey(String apiKey) {
        return storage.values().stream()
                .anyMatch(k -> k.getApiKey().equals(apiKey));
    }
}
