package com.eraf.gateway.repository;

import com.eraf.gateway.domain.RateLimitRule;

import java.util.List;
import java.util.Optional;

/**
 * Rate Limit Rule Repository 인터페이스
 * 구현체: InMemory, JPA, MyBatis, Redis 등
 */
public interface RateLimitRuleRepository {

    /**
     * ID로 조회
     */
    Optional<RateLimitRule> findById(String id);

    /**
     * 모든 Rate Limit 규칙 조회
     */
    List<RateLimitRule> findAll();

    /**
     * 활성화된 Rate Limit 규칙만 조회
     */
    List<RateLimitRule> findAllEnabled();

    /**
     * 타입별 Rate Limit 규칙 조회
     */
    List<RateLimitRule> findByType(RateLimitRule.RateLimitType type);

    /**
     * 활성화된 타입별 Rate Limit 규칙 조회
     */
    List<RateLimitRule> findEnabledByType(RateLimitRule.RateLimitType type);

    /**
     * 우선순위 정렬된 활성화된 규칙 조회
     */
    List<RateLimitRule> findAllEnabledOrderByPriority();

    /**
     * Rate Limit 규칙 저장/수정
     */
    RateLimitRule save(RateLimitRule rateLimitRule);

    /**
     * Rate Limit 규칙 삭제
     */
    void deleteById(String id);
}
