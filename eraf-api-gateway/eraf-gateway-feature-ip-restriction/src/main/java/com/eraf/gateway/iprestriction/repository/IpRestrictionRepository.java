package com.eraf.gateway.iprestriction.repository;

import com.eraf.gateway.iprestriction.domain.IpRestriction;

import java.util.List;
import java.util.Optional;

/**
 * IP Restriction Repository 인터페이스
 * 구현체: InMemory, JPA, MyBatis, Redis 등
 */
public interface IpRestrictionRepository {

    /**
     * ID로 조회
     */
    Optional<IpRestriction> findById(String id);

    /**
     * 모든 IP 제한 규칙 조회
     */
    List<IpRestriction> findAll();

    /**
     * 활성화된 IP 제한 규칙만 조회
     */
    List<IpRestriction> findAllEnabled();

    /**
     * 타입별 IP 제한 규칙 조회
     */
    List<IpRestriction> findByType(IpRestriction.RestrictionType type);

    /**
     * 활성화된 타입별 IP 제한 규칙 조회
     */
    List<IpRestriction> findEnabledByType(IpRestriction.RestrictionType type);

    /**
     * IP 주소로 조회
     */
    Optional<IpRestriction> findByIpAddress(String ipAddress);

    /**
     * IP 제한 규칙 저장/수정
     */
    IpRestriction save(IpRestriction ipRestriction);

    /**
     * IP 제한 규칙 삭제
     */
    void deleteById(String id);

    /**
     * IP 주소 존재 여부 확인
     */
    boolean existsByIpAddress(String ipAddress);
}
