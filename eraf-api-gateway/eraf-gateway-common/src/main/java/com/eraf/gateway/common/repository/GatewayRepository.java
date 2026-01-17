package com.eraf.gateway.common.repository;

import java.util.List;
import java.util.Optional;

/**
 * Gateway Repository 공통 인터페이스
 *
 * @param <T> 엔티티 타입
 * @param <ID> ID 타입
 */
public interface GatewayRepository<T, ID> {

    /**
     * 엔티티 저장
     */
    T save(T entity);

    /**
     * ID로 엔티티 조회
     */
    Optional<T> findById(ID id);

    /**
     * 모든 엔티티 조회
     */
    List<T> findAll();

    /**
     * ID로 엔티티 존재 여부 확인
     */
    boolean existsById(ID id);

    /**
     * ID로 엔티티 삭제
     */
    void deleteById(ID id);

    /**
     * 엔티티 삭제
     */
    void delete(T entity);

    /**
     * 모든 엔티티 삭제
     */
    void deleteAll();

    /**
     * 엔티티 개수 조회
     */
    long count();
}
