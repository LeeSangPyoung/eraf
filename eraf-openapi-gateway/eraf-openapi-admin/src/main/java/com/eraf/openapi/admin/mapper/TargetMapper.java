package com.eraf.openapi.admin.mapper;

import com.eraf.openapi.admin.dto.TargetRequest;
import com.eraf.openapi.admin.dto.TargetResponse;
import com.eraf.openapi.core.domain.GatewayService;
import com.eraf.openapi.core.domain.GatewayTarget;
import com.eraf.openapi.core.domain.GatewayUpstream;
import org.springframework.stereotype.Component;

/**
 * Target Entity ↔ DTO 변환 Mapper
 */
@Component
public class TargetMapper {

    public GatewayTarget toEntity(TargetRequest request, GatewayService service, GatewayUpstream upstream) {
        return GatewayTarget.builder()
                .service(service)
                .upstream(upstream)
                .host(request.getHost())
                .port(request.getPort())
                .weight(request.getWeight())
                .description(request.getDescription())
                .enabled(request.getEnabled())
                .healthStatus("UNKNOWN")
                .consecutiveFailures(0)
                .build();
    }

    public void updateEntity(GatewayTarget target, TargetRequest request, GatewayService service, GatewayUpstream upstream) {
        target.setService(service);
        target.setUpstream(upstream);
        target.setHost(request.getHost());
        target.setPort(request.getPort());
        target.setWeight(request.getWeight());
        target.setDescription(request.getDescription());
        target.setEnabled(request.getEnabled());
    }

    public TargetResponse toResponse(GatewayTarget target) {
        return TargetResponse.builder()
                .id(target.getId())
                .serviceId(target.getService() != null ? target.getService().getId() : null)
                .serviceName(target.getService() != null ? target.getService().getName() : null)
                .upstreamId(target.getUpstream() != null ? target.getUpstream().getId() : null)
                .upstreamName(target.getUpstream() != null ? target.getUpstream().getName() : null)
                .host(target.getHost())
                .port(target.getPort())
                .weight(target.getWeight())
                .healthStatus(target.getHealthStatus())
                .consecutiveFailures(target.getConsecutiveFailures())
                .description(target.getDescription())
                .enabled(target.getEnabled())
                .lastHealthCheckAt(target.getLastHealthCheckAt())
                .createdAt(target.getCreatedAt())
                .updatedAt(target.getUpdatedAt())
                .build();
    }
}
