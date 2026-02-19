package com.eraf.featureflag.feature;

import com.eraf.featureflag.feature.dto.FeatureFlagRequest;
import com.eraf.featureflag.feature.dto.FeatureFlagResponse;
import org.springframework.stereotype.Component;

/**
 * Feature Flag Mapper (Entity ↔ DTO)
 *
 * @author ERAF Team
 * @since Phase 2
 */
@Component
public class FeatureFlagMapper {

    /**
     * Convert Request DTO to Entity
     *
     * @param request the request DTO
     * @return the entity
     */
    public FeatureFlagEntity toEntity(FeatureFlagRequest request) {
        FeatureFlagEntity entity = new FeatureFlagEntity();
        entity.setFlagKey(request.getFlagKey());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setEnabled(request.getEnabled());
        entity.setFlagType(request.getFlagType());
        entity.setTargetingRules(request.getTargetingRules());
        entity.setFallbackValue(request.getFallbackValue());
        entity.setTags(request.getTags());
        entity.setMetadata(request.getMetadata());
        return entity;
    }

    /**
     * Update existing entity from Request DTO
     *
     * @param entity the entity to update
     * @param request the request DTO
     */
    public void updateEntity(FeatureFlagEntity entity, FeatureFlagRequest request) {
        // Don't update flagKey (immutable identifier)
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setEnabled(request.getEnabled());
        entity.setFlagType(request.getFlagType());
        entity.setTargetingRules(request.getTargetingRules());
        entity.setFallbackValue(request.getFallbackValue());
        entity.setTags(request.getTags());
        entity.setMetadata(request.getMetadata());
    }

    /**
     * Convert Entity to Response DTO
     *
     * @param entity the entity
     * @return the response DTO
     */
    public FeatureFlagResponse toResponse(FeatureFlagEntity entity) {
        FeatureFlagResponse response = new FeatureFlagResponse();
        response.setId(entity.getId());
        response.setFlagKey(entity.getFlagKey());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setEnabled(entity.getEnabled());
        response.setFlagType(entity.getFlagType());
        response.setTargetingRules(entity.getTargetingRules());
        response.setFallbackValue(entity.getFallbackValue());
        response.setTags(entity.getTags());
        response.setMetadata(entity.getMetadata());
        response.setCheckCount(entity.getCheckCount());
        response.setLastCheckedAt(entity.getLastCheckedAt());
        response.setCreatedAt(entity.getCreatedAt());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        return response;
    }
}
