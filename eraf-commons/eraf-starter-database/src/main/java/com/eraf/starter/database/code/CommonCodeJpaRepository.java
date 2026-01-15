package com.eraf.starter.database.code;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 공통코드 JPA Repository
 */
public interface CommonCodeJpaRepository extends JpaRepository<CommonCodeEntity, Long> {

    /**
     * 그룹의 모든 코드 조회 (정렬순)
     */
    List<CommonCodeEntity> findByCodeGroupOrderBySortOrderAsc(String codeGroup);

    /**
     * 그룹의 활성화된 코드 조회
     */
    List<CommonCodeEntity> findByCodeGroupAndEnabledTrueOrderBySortOrderAsc(String codeGroup);

    /**
     * 그룹과 코드로 조회
     */
    Optional<CommonCodeEntity> findByCodeGroupAndCode(String codeGroup, String code);

    /**
     * 존재 여부 확인
     */
    boolean existsByCodeGroupAndCode(String codeGroup, String code);

    /**
     * 활성화된 코드 존재 여부 확인
     */
    @Query("SELECT COUNT(c) > 0 FROM CommonCodeEntity c WHERE c.codeGroup = :group AND c.code = :code AND c.enabled = true")
    boolean existsByCodeGroupAndCodeAndEnabledTrue(@Param("group") String codeGroup, @Param("code") String code);
}
