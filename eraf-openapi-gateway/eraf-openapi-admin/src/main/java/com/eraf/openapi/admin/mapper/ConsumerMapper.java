package com.eraf.openapi.admin.mapper;

import com.eraf.openapi.admin.dto.ConsumerRequest;
import com.eraf.openapi.admin.dto.ConsumerResponse;
import com.eraf.openapi.core.domain.GatewayConsumer;
import org.springframework.stereotype.Component;

/**
 * Consumer Entity ↔ DTO 변환 Mapper
 */
@Component
public class ConsumerMapper {

    public GatewayConsumer toEntity(ConsumerRequest request) {
        return GatewayConsumer.builder()
                .username(request.getUsername())
                .description(request.getDescription())
                .rateLimit(request.getRateLimit())
                .rateLimitWindowSeconds(request.getRateLimitWindowSeconds())
                .enabled(request.getEnabled())
                .tags(request.getTags())
                .customId(request.getCustomId())
                .metadata(request.getMetadata())
                .build();
    }

    public void updateEntity(GatewayConsumer consumer, ConsumerRequest request) {
        consumer.setUsername(request.getUsername());
        consumer.setDescription(request.getDescription());
        consumer.setRateLimit(request.getRateLimit());
        consumer.setRateLimitWindowSeconds(request.getRateLimitWindowSeconds());
        consumer.setEnabled(request.getEnabled());
        if (request.getTags() != null) {
            consumer.setTags(request.getTags());
        }
        consumer.setCustomId(request.getCustomId());
        if (request.getMetadata() != null) {
            consumer.setMetadata(request.getMetadata());
        }
    }

    public ConsumerResponse toResponse(GatewayConsumer consumer) {
        return ConsumerResponse.builder()
                .id(consumer.getId())
                .username(consumer.getUsername())
                .apiKey(consumer.getApiKey())
                .description(consumer.getDescription())
                .rateLimit(consumer.getRateLimit())
                .rateLimitWindowSeconds(consumer.getRateLimitWindowSeconds())
                .enabled(consumer.getEnabled())
                .tags(consumer.getTags())
                .customId(consumer.getCustomId())
                .metadata(consumer.getMetadata())
                .createdAt(consumer.getCreatedAt())
                .updatedAt(consumer.getUpdatedAt())
                .createdBy(consumer.getCreatedBy())
                .build();
    }
}
