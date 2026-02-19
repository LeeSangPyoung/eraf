package com.eraf.featureflag.feature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Feature Flag persistence
 *
 * @author ERAF Team
 * @since Phase 3
 */
@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlagEntity, Long> {

    /**
     * Find feature flag by its unique key
     *
     * @param flagKey the feature flag key
     * @return Optional containing the feature flag if found
     */
    Optional<FeatureFlagEntity> findByFlagKey(String flagKey);

    /**
     * Check if a feature flag exists by key
     *
     * @param flagKey the feature flag key
     * @return true if exists, false otherwise
     */
    boolean existsByFlagKey(String flagKey);

    /**
     * Find all enabled feature flags
     *
     * @return list of enabled feature flags
     */
    List<FeatureFlagEntity> findByEnabledTrue();

    /**
     * Find all disabled feature flags
     *
     * @return list of disabled feature flags
     */
    List<FeatureFlagEntity> findByEnabledFalse();

    /**
     * Find feature flags by type
     *
     * @param flagType the flag type
     * @return list of feature flags matching the type
     */
    List<FeatureFlagEntity> findByFlagType(FeatureFlagEntity.FlagType flagType);

    /**
     * Find feature flags by enabled status and type
     *
     * @param enabled whether the flag is enabled
     * @param flagType the flag type
     * @return list of matching feature flags
     */
    List<FeatureFlagEntity> findByEnabledAndFlagType(Boolean enabled, FeatureFlagEntity.FlagType flagType);

    /**
     * Count all feature flags
     *
     * @return total count of feature flags
     */
    @Query("SELECT COUNT(f) FROM FeatureFlagEntity f")
    long countAll();

    /**
     * Count enabled feature flags
     *
     * @return count of enabled flags
     */
    long countByEnabledTrue();

    /**
     * Count disabled feature flags
     *
     * @return count of disabled flags
     */
    long countByEnabledFalse();

    /**
     * Count feature flags by type
     *
     * @param flagType the flag type
     * @return count of flags matching the type
     */
    long countByFlagType(FeatureFlagEntity.FlagType flagType);

    /**
     * Increment check count for a feature flag (for statistics)
     *
     * @param flagKey the feature flag key
     * @param now current timestamp
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE FeatureFlagEntity f SET f.checkCount = f.checkCount + 1, f.lastCheckedAt = :now WHERE f.flagKey = :flagKey")
    int incrementCheckCount(@Param("flagKey") String flagKey, @Param("now") Instant now);

    /**
     * Find top N most checked feature flags (for statistics)
     *
     * @param limit maximum number of results
     * @return list of feature flags ordered by check count descending
     */
    @Query("SELECT f FROM FeatureFlagEntity f ORDER BY f.checkCount DESC")
    List<FeatureFlagEntity> findTopByCheckCount(int limit);

    /**
     * Find feature flags created after a specific time
     *
     * @param since the starting timestamp
     * @return list of feature flags created after the given time
     */
    List<FeatureFlagEntity> findByCreatedAtAfter(Instant since);

    /**
     * Find feature flags updated after a specific time
     *
     * @param since the starting timestamp
     * @return list of feature flags updated after the given time
     */
    List<FeatureFlagEntity> findByUpdatedAtAfter(Instant since);

    /**
     * Delete feature flags by key
     *
     * @param flagKey the feature flag key
     */
    void deleteByFlagKey(String flagKey);
}
