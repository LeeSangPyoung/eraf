package com.eraf.jpa.softdelete;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Soft Delete 지원 Repository
 *
 * 사용 예:
 * <pre>
 * public interface UserRepository extends SoftDeleteRepository<User, Long> {
 * }
 * </pre>
 */
@NoRepositoryBean
public interface SoftDeleteRepository<T extends SoftDeleteEntity, ID> extends JpaRepository<T, ID> {

    /**
     * 삭제되지 않은 엔티티 조회
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    List<T> findAllActive();

    /**
     * 삭제되지 않은 엔티티 페이징 조회
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    Page<T> findAllActive(Pageable pageable);

    /**
     * ID로 삭제되지 않은 엔티티 조회
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = ?1 AND e.deleted = false")
    Optional<T> findByIdActive(ID id);

    /**
     * 삭제된 엔티티만 조회
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    List<T> findAllDeleted();

    /**
     * 삭제된 엔티티 페이징 조회
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    Page<T> findAllDeleted(Pageable pageable);

    /**
     * 소프트 삭제 실행
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = true, e.deletedAt = CURRENT_TIMESTAMP WHERE e.id = ?1")
    int softDeleteById(ID id);

    /**
     * 소프트 삭제 복원
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.deletedAt = null, e.deletedBy = null WHERE e.id = ?1")
    int restoreById(ID id);

    /**
     * 삭제되지 않은 엔티티 수
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = false")
    long countActive();

    /**
     * 삭제된 엔티티 수
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = true")
    long countDeleted();

    /**
     * ID로 삭제되지 않은 엔티티 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = ?1 AND e.deleted = false")
    boolean existsByIdActive(ID id);
}
