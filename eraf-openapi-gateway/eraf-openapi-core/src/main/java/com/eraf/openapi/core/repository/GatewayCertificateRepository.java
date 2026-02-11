package com.eraf.openapi.core.repository;

import com.eraf.openapi.core.domain.GatewayCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Gateway Certificate Repository
 */
@Repository
public interface GatewayCertificateRepository extends JpaRepository<GatewayCertificate, Long> {

    Optional<GatewayCertificate> findByName(String name);

    boolean existsByName(String name);

    List<GatewayCertificate> findByEnabledTrue();

    /**
     * 만료된 인증서 조회
     */
    @Query("SELECT c FROM GatewayCertificate c WHERE c.expiresAt < :now AND c.enabled = true")
    List<GatewayCertificate> findExpiredCertificates(@Param("now") LocalDateTime now);

    /**
     * 만료 임박 인증서 조회 (지정 날짜 이전에 만료되는)
     */
    @Query("SELECT c FROM GatewayCertificate c WHERE c.expiresAt BETWEEN :now AND :threshold AND c.enabled = true")
    List<GatewayCertificate> findExpiringSoonCertificates(@Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold);

    long countByEnabledTrue();

    @Query("SELECT COUNT(c) FROM GatewayCertificate c WHERE c.expiresAt < CURRENT_TIMESTAMP AND c.enabled = true")
    long countExpiredCertificates();
}
