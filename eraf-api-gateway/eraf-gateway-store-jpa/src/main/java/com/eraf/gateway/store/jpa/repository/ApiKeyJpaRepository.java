package com.eraf.gateway.store.jpa.repository;

import com.eraf.gateway.store.jpa.entity.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * API Key Spring Data JPA Repository
 */
@Repository
public interface ApiKeyJpaRepository extends JpaRepository<ApiKeyEntity, String> {

    Optional<ApiKeyEntity> findByApiKey(String apiKey);

    List<ApiKeyEntity> findByEnabledTrue();

    boolean existsByApiKey(String apiKey);
}
