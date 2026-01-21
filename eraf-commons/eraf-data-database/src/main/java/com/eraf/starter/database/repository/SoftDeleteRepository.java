package com.eraf.starter.database.repository;

import com.eraf.starter.database.entity.SoftDeleteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Soft Delete 지원 Repository
 */
@NoRepositoryBean
public interface SoftDeleteRepository<T extends SoftDeleteEntity, ID> extends JpaRepository<T, ID> {

    /**
     * 삭제되지 않은 엔티티 조회
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
    Optional<T> findByIdAndNotDeleted(@Param("id") ID id);

    /**
     * 삭제되지 않은 모든 엔티티 조회
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    List<T> findAllNotDeleted();

    /**
     * 삭제된 엔티티 조회
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    List<T> findAllDeleted();

    /**
     * Soft Delete 수행
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = true, e.deletedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    int softDelete(@Param("id") ID id);

    /**
     * 복구
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.deletedAt = null, e.deletedBy = null WHERE e.id = :id")
    int restore(@Param("id") ID id);

    /**
     * 삭제되지 않은 엔티티 수
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = false")
    long countNotDeleted();
}
