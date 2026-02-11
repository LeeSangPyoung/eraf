package com.eraf.openapi.admin.mapper;

import com.eraf.openapi.admin.dto.ConsumerGroupRequest;
import com.eraf.openapi.admin.dto.ConsumerGroupResponse;
import com.eraf.openapi.core.domain.GatewayConsumerGroup;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Consumer Group Entity ↔ DTO 변환 Mapper
 */
@Component
public class ConsumerGroupMapper {

    public GatewayConsumerGroup toEntity(ConsumerGroupRequest request) {
        return GatewayConsumerGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .rateLimit(request.getRateLimit())
                .rateLimitWindowSeconds(request.getRateLimitWindowSeconds())
                .enabled(request.getEnabled())
                .tags(request.getTags())
                .metadata(request.getMetadata())
                .build();
    }

    public void updateEntity(GatewayConsumerGroup group, ConsumerGroupRequest request) {
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setRateLimit(request.getRateLimit());
        group.setRateLimitWindowSeconds(request.getRateLimitWindowSeconds());
        group.setEnabled(request.getEnabled());
        if (request.getTags() != null) {
            group.setTags(request.getTags());
        }
        if (request.getMetadata() != null) {
            group.setMetadata(request.getMetadata());
        }
    }

    public ConsumerGroupResponse toResponse(GatewayConsumerGroup group) {
        return ConsumerGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .rateLimit(group.getRateLimit())
                .rateLimitWindowSeconds(group.getRateLimitWindowSeconds())
                .enabled(group.getEnabled())
                .memberCount(group.getConsumers() != null ? group.getConsumers().size() : 0)
                .members(group.getConsumers() != null ?
                        group.getConsumers().stream()
                                .map(c -> ConsumerGroupResponse.ConsumerSummary.builder()
                                        .id(c.getId())
                                        .username(c.getUsername())
                                        .enabled(c.getEnabled())
                                        .build())
                                .collect(Collectors.toList()) :
                        null)
                .tags(group.getTags())
                .metadata(group.getMetadata())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .createdBy(group.getCreatedBy())
                .build();
    }
}
