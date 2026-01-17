package com.eraf.gateway.oauth2.repository;

import com.eraf.gateway.oauth2.domain.OAuth2Client;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 인메모리 OAuth2 Client Repository 구현체
 */
@Slf4j
public class InMemoryOAuth2ClientRepository implements OAuth2ClientRepository {

    private final Map<String, OAuth2Client> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<OAuth2Client> findByClientId(String clientId) {
        return Optional.ofNullable(storage.get(clientId));
    }

    @Override
    public Optional<OAuth2Client> findByClientIdAndSecret(String clientId, String clientSecret) {
        return findByClientId(clientId)
                .filter(c -> c.getClientSecret().equals(clientSecret));
    }

    @Override
    public List<OAuth2Client> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<OAuth2Client> findAllEnabled() {
        return storage.values().stream()
                .filter(OAuth2Client::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public OAuth2Client save(OAuth2Client client) {
        OAuth2Client savedClient = client;
        if (storage.containsKey(client.getClientId())) {
            // Update existing
            savedClient = OAuth2Client.builder()
                    .clientId(client.getClientId())
                    .clientSecret(client.getClientSecret())
                    .clientName(client.getClientName())
                    .description(client.getDescription())
                    .redirectUris(client.getRedirectUris())
                    .allowedGrantTypes(client.getAllowedGrantTypes())
                    .allowedScopes(client.getAllowedScopes())
                    .accessTokenValiditySeconds(client.getAccessTokenValiditySeconds())
                    .refreshTokenValiditySeconds(client.getRefreshTokenValiditySeconds())
                    .enabled(client.isEnabled())
                    .createdAt(storage.get(client.getClientId()).getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
        } else {
            // New client
            savedClient = OAuth2Client.builder()
                    .clientId(client.getClientId())
                    .clientSecret(client.getClientSecret())
                    .clientName(client.getClientName())
                    .description(client.getDescription())
                    .redirectUris(client.getRedirectUris())
                    .allowedGrantTypes(client.getAllowedGrantTypes())
                    .allowedScopes(client.getAllowedScopes())
                    .accessTokenValiditySeconds(client.getAccessTokenValiditySeconds())
                    .refreshTokenValiditySeconds(client.getRefreshTokenValiditySeconds())
                    .enabled(client.isEnabled())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        storage.put(savedClient.getClientId(), savedClient);
        log.debug("Saved OAuth2 client: {}", savedClient.getClientId());
        return savedClient;
    }

    @Override
    public void deleteByClientId(String clientId) {
        OAuth2Client removed = storage.remove(clientId);
        if (removed != null) {
            log.debug("Deleted OAuth2 client: {}", clientId);
        }
    }

    @Override
    public boolean existsByClientId(String clientId) {
        return storage.containsKey(clientId);
    }
}
