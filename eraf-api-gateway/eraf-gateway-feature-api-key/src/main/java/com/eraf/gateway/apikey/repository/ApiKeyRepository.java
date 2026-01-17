package com.eraf.gateway.apikey.repository;

import com.eraf.gateway.apikey.domain.ApiKey;

import java.util.List;
import java.util.Optional;

/**
 * API Key Repository 인터페이스
 * 구현체: InMemory, JPA, MyBatis, Redis 등
 */
public interface ApiKeyRepository {

    /**
     * API Key 값으로 조회
     */
    Optional<ApiKey> findByApiKey(String apiKey);

    /**
     * ID로 조회
     */
    Optional<ApiKey> findById(String id);

    /**
     * 모든 API Key 조회
     */
    List<ApiKey> findAll();

    /**
     * 활성화된 API Key만 조회
     */
    List<ApiKey> findAllEnabled();

    /**
     * API Key 저장/수정
     */
    ApiKey save(ApiKey apiKey);

    /**
     * API Key 삭제
     */
    void deleteById(String id);

    /**
     * API Key 존재 여부 확인
     */
    boolean existsByApiKey(String apiKey);
}
