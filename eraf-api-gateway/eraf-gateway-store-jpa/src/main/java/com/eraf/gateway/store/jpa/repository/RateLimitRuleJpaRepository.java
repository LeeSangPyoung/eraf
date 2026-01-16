package com.eraf.gateway.store.jpa.repository;

import com.eraf.gateway.domain.RateLimitRule;
import com.eraf.gateway.store.jpa.entity.RateLimitRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Rate Limit Rule Spring Data JPA Repository
 */
@Repository
public interface RateLimitRuleJpaRepository extends JpaRepository<RateLimitRuleEntity, String> {

    List<RateLimitRuleEntity> findByEnabledTrue();

    List<RateLimitRuleEntity> findByType(RateLimitRule.RateLimitType type);

    List<RateLimitRuleEntity> findByEnabledTrueAndType(RateLimitRule.RateLimitType type);

    List<RateLimitRuleEntity> findByEnabledTrueOrderByPriorityAsc();
}
