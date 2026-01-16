package com.eraf.gateway.store.jpa;

import com.eraf.gateway.domain.ApiKey;
import com.eraf.gateway.repository.ApiKeyRepository;
import com.eraf.gateway.store.jpa.entity.ApiKeyEntity;
import com.eraf.gateway.store.jpa.repository.ApiKeyJpaRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA API Key Repository 어댑터
 */
@RequiredArgsConstructor
public class JpaApiKeyRepository implements ApiKeyRepository {

    private final ApiKeyJpaRepository jpaRepository;

    @Override
    public Optional<ApiKey> findByApiKey(String apiKey) {
        return jpaRepository.findByApiKey(apiKey)
                .map(ApiKeyEntity::toDomain);
    }

    @Override
    public Optional<ApiKey> findById(String id) {
        return jpaRepository.findById(id)
                .map(ApiKeyEntity::toDomain);
    }

    @Override
    public List<ApiKey> findAll() {
        return jpaRepository.findAll().stream()
                .map(ApiKeyEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApiKey> findAllEnabled() {
        return jpaRepository.findByEnabledTrue().stream()
                .map(ApiKeyEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public ApiKey save(ApiKey apiKey) {
        String id = apiKey.getId();
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
            apiKey = ApiKey.builder()
                    .id(id)
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

        ApiKeyEntity entity = ApiKeyEntity.fromDomain(apiKey);
        return jpaRepository.save(entity).toDomain();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByApiKey(String apiKey) {
        return jpaRepository.existsByApiKey(apiKey);
    }
}
