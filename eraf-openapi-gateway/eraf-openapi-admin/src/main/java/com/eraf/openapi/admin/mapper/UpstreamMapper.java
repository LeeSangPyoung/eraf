package com.eraf.openapi.admin.mapper;

import com.eraf.openapi.admin.dto.UpstreamRequest;
import com.eraf.openapi.admin.dto.UpstreamResponse;
import com.eraf.openapi.core.domain.GatewayUpstream;
import org.springframework.stereotype.Component;

/**
 * Upstream Entity ↔ DTO 변환 Mapper
 */
@Component
public class UpstreamMapper {

    public GatewayUpstream toEntity(UpstreamRequest request) {
        return GatewayUpstream.builder()
                .name(request.getName())
                .description(request.getDescription())
                .algorithm(request.getAlgorithm())
                .hashOn(request.getHashOn())
                .hashOnHeader(request.getHashOnHeader())
                .hashFallback(request.getHashFallback())
                .slots(request.getSlots())
                .healthcheckActiveEnabled(request.getHealthcheckActiveEnabled())
                .healthcheckActivePath(request.getHealthcheckActivePath())
                .healthcheckActiveInterval(request.getHealthcheckActiveInterval())
                .healthcheckActiveTimeout(request.getHealthcheckActiveTimeout())
                .healthcheckActiveHealthySuccesses(request.getHealthcheckActiveHealthySuccesses())
                .healthcheckActiveUnhealthyFailures(request.getHealthcheckActiveUnhealthyFailures())
                .healthcheckActiveHealthyStatuses(request.getHealthcheckActiveHealthyStatuses())
                .healthcheckActiveUnhealthyStatuses(request.getHealthcheckActiveUnhealthyStatuses())
                .healthcheckPassiveEnabled(request.getHealthcheckPassiveEnabled())
                .healthcheckPassiveUnhealthyFailures(request.getHealthcheckPassiveUnhealthyFailures())
                .healthcheckPassiveUnhealthyTimeouts(request.getHealthcheckPassiveUnhealthyTimeouts())
                .enabled(request.getEnabled())
                .tags(request.getTags())
                .metadata(request.getMetadata())
                .build();
    }

    public void updateEntity(GatewayUpstream upstream, UpstreamRequest request) {
        upstream.setName(request.getName());
        upstream.setDescription(request.getDescription());
        upstream.setAlgorithm(request.getAlgorithm());
        upstream.setHashOn(request.getHashOn());
        upstream.setHashOnHeader(request.getHashOnHeader());
        upstream.setHashFallback(request.getHashFallback());
        upstream.setSlots(request.getSlots());
        upstream.setHealthcheckActiveEnabled(request.getHealthcheckActiveEnabled());
        upstream.setHealthcheckActivePath(request.getHealthcheckActivePath());
        upstream.setHealthcheckActiveInterval(request.getHealthcheckActiveInterval());
        upstream.setHealthcheckActiveTimeout(request.getHealthcheckActiveTimeout());
        upstream.setHealthcheckActiveHealthySuccesses(request.getHealthcheckActiveHealthySuccesses());
        upstream.setHealthcheckActiveUnhealthyFailures(request.getHealthcheckActiveUnhealthyFailures());
        if (request.getHealthcheckActiveHealthyStatuses() != null) {
            upstream.setHealthcheckActiveHealthyStatuses(request.getHealthcheckActiveHealthyStatuses());
        }
        if (request.getHealthcheckActiveUnhealthyStatuses() != null) {
            upstream.setHealthcheckActiveUnhealthyStatuses(request.getHealthcheckActiveUnhealthyStatuses());
        }
        upstream.setHealthcheckPassiveEnabled(request.getHealthcheckPassiveEnabled());
        upstream.setHealthcheckPassiveUnhealthyFailures(request.getHealthcheckPassiveUnhealthyFailures());
        upstream.setHealthcheckPassiveUnhealthyTimeouts(request.getHealthcheckPassiveUnhealthyTimeouts());
        upstream.setEnabled(request.getEnabled());
        if (request.getTags() != null) {
            upstream.setTags(request.getTags());
        }
        if (request.getMetadata() != null) {
            upstream.setMetadata(request.getMetadata());
        }
    }

    public UpstreamResponse toResponse(GatewayUpstream upstream) {
        return UpstreamResponse.builder()
                .id(upstream.getId())
                .name(upstream.getName())
                .description(upstream.getDescription())
                .algorithm(upstream.getAlgorithm())
                .hashOn(upstream.getHashOn())
                .hashOnHeader(upstream.getHashOnHeader())
                .hashFallback(upstream.getHashFallback())
                .slots(upstream.getSlots())
                .healthcheckActiveEnabled(upstream.getHealthcheckActiveEnabled())
                .healthcheckActivePath(upstream.getHealthcheckActivePath())
                .healthcheckActiveInterval(upstream.getHealthcheckActiveInterval())
                .healthcheckActiveTimeout(upstream.getHealthcheckActiveTimeout())
                .healthcheckActiveHealthySuccesses(upstream.getHealthcheckActiveHealthySuccesses())
                .healthcheckActiveUnhealthyFailures(upstream.getHealthcheckActiveUnhealthyFailures())
                .healthcheckActiveHealthyStatuses(upstream.getHealthcheckActiveHealthyStatuses())
                .healthcheckActiveUnhealthyStatuses(upstream.getHealthcheckActiveUnhealthyStatuses())
                .healthcheckPassiveEnabled(upstream.getHealthcheckPassiveEnabled())
                .healthcheckPassiveUnhealthyFailures(upstream.getHealthcheckPassiveUnhealthyFailures())
                .healthcheckPassiveUnhealthyTimeouts(upstream.getHealthcheckPassiveUnhealthyTimeouts())
                .enabled(upstream.getEnabled())
                .targetCount(upstream.getTargets() != null ? upstream.getTargets().size() : 0)
                .tags(upstream.getTags())
                .metadata(upstream.getMetadata())
                .createdAt(upstream.getCreatedAt())
                .updatedAt(upstream.getUpdatedAt())
                .createdBy(upstream.getCreatedBy())
                .build();
    }
}
