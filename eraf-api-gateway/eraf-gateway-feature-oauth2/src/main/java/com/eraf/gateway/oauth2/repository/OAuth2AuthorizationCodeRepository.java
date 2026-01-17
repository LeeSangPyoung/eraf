package com.eraf.gateway.oauth2.repository;

import com.eraf.gateway.oauth2.domain.OAuth2AuthorizationCode;

import java.util.Optional;

/**
 * OAuth2 Authorization Code 저장소 인터페이스
 */
public interface OAuth2AuthorizationCodeRepository {

    /**
     * Authorization Code로 조회
     */
    Optional<OAuth2AuthorizationCode> findByCode(String code);

    /**
     * Authorization Code 저장
     */
    OAuth2AuthorizationCode save(OAuth2AuthorizationCode authorizationCode);

    /**
     * Authorization Code 삭제
     */
    void deleteByCode(String code);

    /**
     * 만료된 Authorization Code 삭제
     */
    void deleteExpiredCodes();
}
