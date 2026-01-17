package com.eraf.gateway.oauth2.repository;

import com.eraf.gateway.oauth2.domain.OAuth2Token;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 인메모리 OAuth2 Token Repository 구현체
 */
@Slf4j
public class InMemoryOAuth2TokenRepository implements OAuth2TokenRepository {

    private final Map<String, OAuth2Token> byAccessToken = new ConcurrentHashMap<>();
    private final Map<String, OAuth2Token> byRefreshToken = new ConcurrentHashMap<>();

    @Override
    public Optional<OAuth2Token> findByAccessToken(String accessToken) {
        return Optional.ofNullable(byAccessToken.get(accessToken));
    }

    @Override
    public Optional<OAuth2Token> findByRefreshToken(String refreshToken) {
        return Optional.ofNullable(byRefreshToken.get(refreshToken));
    }

    @Override
    public List<OAuth2Token> findByUserId(String userId) {
        return byAccessToken.values().stream()
                .filter(t -> userId.equals(t.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<OAuth2Token> findByClientId(String clientId) {
        return byAccessToken.values().stream()
                .filter(t -> clientId.equals(t.getClientId()))
                .collect(Collectors.toList());
    }

    @Override
    public OAuth2Token save(OAuth2Token token) {
        OAuth2Token savedToken = token;
        if (token.getId() == null) {
            savedToken = OAuth2Token.builder()
                    .id(UUID.randomUUID().toString())
                    .accessToken(token.getAccessToken())
                    .refreshToken(token.getRefreshToken())
                    .tokenType(token.getTokenType())
                    .clientId(token.getClientId())
                    .userId(token.getUserId())
                    .scopes(token.getScopes())
                    .expiresAt(token.getExpiresAt())
                    .refreshExpiresAt(token.getRefreshExpiresAt())
                    .issuedAt(token.getIssuedAt() != null ? token.getIssuedAt() : LocalDateTime.now())
                    .build();
        }

        byAccessToken.put(savedToken.getAccessToken(), savedToken);
        if (savedToken.getRefreshToken() != null) {
            byRefreshToken.put(savedToken.getRefreshToken(), savedToken);
        }

        log.debug("Saved OAuth2 token for user: {}", savedToken.getUserId());
        return savedToken;
    }

    @Override
    public void deleteByAccessToken(String accessToken) {
        OAuth2Token removed = byAccessToken.remove(accessToken);
        if (removed != null && removed.getRefreshToken() != null) {
            byRefreshToken.remove(removed.getRefreshToken());
        }
    }

    @Override
    public void deleteByRefreshToken(String refreshToken) {
        OAuth2Token removed = byRefreshToken.remove(refreshToken);
        if (removed != null) {
            byAccessToken.remove(removed.getAccessToken());
        }
    }

    @Override
    public void deleteByUserId(String userId) {
        List<String> accessTokensToRemove = byAccessToken.values().stream()
                .filter(t -> userId.equals(t.getUserId()))
                .map(OAuth2Token::getAccessToken)
                .collect(Collectors.toList());

        for (String accessToken : accessTokensToRemove) {
            deleteByAccessToken(accessToken);
        }
    }

    @Override
    public void deleteExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int removed = 0;

        Iterator<Map.Entry<String, OAuth2Token>> iterator = byAccessToken.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, OAuth2Token> entry = iterator.next();
            OAuth2Token token = entry.getValue();
            if (token.getExpiresAt().isBefore(now)) {
                iterator.remove();
                if (token.getRefreshToken() != null) {
                    byRefreshToken.remove(token.getRefreshToken());
                }
                removed++;
            }
        }

        if (removed > 0) {
            log.info("Deleted {} expired OAuth2 tokens", removed);
        }
    }
}
