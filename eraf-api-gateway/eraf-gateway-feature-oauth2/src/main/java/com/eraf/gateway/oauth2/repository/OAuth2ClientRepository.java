package com.eraf.gateway.oauth2.repository;

import com.eraf.gateway.oauth2.domain.OAuth2Client;

import java.util.List;
import java.util.Optional;

/**
 * OAuth2 Client 저장소 인터페이스
 */
public interface OAuth2ClientRepository {

    /**
     * Client ID로 조회
     */
    Optional<OAuth2Client> findByClientId(String clientId);

    /**
     * Client ID와 Secret으로 조회 (인증용)
     */
    Optional<OAuth2Client> findByClientIdAndSecret(String clientId, String clientSecret);

    /**
     * 모든 Client 조회
     */
    List<OAuth2Client> findAll();

    /**
     * 활성화된 Client 조회
     */
    List<OAuth2Client> findAllEnabled();

    /**
     * Client 저장
     */
    OAuth2Client save(OAuth2Client client);

    /**
     * Client 삭제
     */
    void deleteByClientId(String clientId);

    /**
     * Client 존재 여부 확인
     */
    boolean existsByClientId(String clientId);
}
