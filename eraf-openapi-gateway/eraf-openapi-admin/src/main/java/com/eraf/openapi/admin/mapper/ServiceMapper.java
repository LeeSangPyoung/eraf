package com.eraf.openapi.admin.mapper;

import com.eraf.openapi.admin.dto.ServiceRequest;
import com.eraf.openapi.admin.dto.ServiceResponse;
import com.eraf.openapi.core.domain.GatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Service Entity ↔ DTO 변환 Mapper
 */
@Component
@RequiredArgsConstructor
public class ServiceMapper {

    private final TargetMapper targetMapper;

    public GatewayService toEntity(ServiceRequest request) {
        return GatewayService.builder()
                .name(request.getName())
                .protocol(request.getProtocol())
                .host(request.getHost())
                .port(request.getPort())
                .basePath(request.getBasePath())
                .connectTimeout(request.getConnectTimeout())
                .readTimeout(request.getReadTimeout())
                .writeTimeout(request.getWriteTimeout())
                .retries(request.getRetries())
                .loadBalancingAlgorithm(request.getLoadBalancingAlgorithm())
                .healthCheckEnabled(request.getHealthCheckEnabled())
                .healthCheckPath(request.getHealthCheckPath())
                .healthCheckInterval(request.getHealthCheckInterval())
                .healthCheckTimeout(request.getHealthCheckTimeout())
                .healthyThreshold(request.getHealthyThreshold())
                .unhealthyThreshold(request.getUnhealthyThreshold())
                .description(request.getDescription())
                .metadata(request.getMetadata())
                .enabled(request.getEnabled())
                .build();
    }

    public void updateEntity(GatewayService service, ServiceRequest request) {
        service.setName(request.getName());
        service.setProtocol(request.getProtocol());
        service.setHost(request.getHost());
        service.setPort(request.getPort());
        service.setBasePath(request.getBasePath());
        service.setConnectTimeout(request.getConnectTimeout());
        service.setReadTimeout(request.getReadTimeout());
        service.setWriteTimeout(request.getWriteTimeout());
        service.setRetries(request.getRetries());
        service.setLoadBalancingAlgorithm(request.getLoadBalancingAlgorithm());
        service.setHealthCheckEnabled(request.getHealthCheckEnabled());
        service.setHealthCheckPath(request.getHealthCheckPath());
        service.setHealthCheckInterval(request.getHealthCheckInterval());
        service.setHealthCheckTimeout(request.getHealthCheckTimeout());
        service.setHealthyThreshold(request.getHealthyThreshold());
        service.setUnhealthyThreshold(request.getUnhealthyThreshold());
        service.setDescription(request.getDescription());
        service.setMetadata(request.getMetadata());
        service.setEnabled(request.getEnabled());
    }

    public ServiceResponse toResponse(GatewayService service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .protocol(service.getProtocol())
                .host(service.getHost())
                .port(service.getPort())
                .basePath(service.getBasePath())
                .connectTimeout(service.getConnectTimeout())
                .readTimeout(service.getReadTimeout())
                .writeTimeout(service.getWriteTimeout())
                .retries(service.getRetries())
                .loadBalancingAlgorithm(service.getLoadBalancingAlgorithm())
                .healthCheckEnabled(service.getHealthCheckEnabled())
                .healthCheckPath(service.getHealthCheckPath())
                .healthCheckInterval(service.getHealthCheckInterval())
                .healthCheckTimeout(service.getHealthCheckTimeout())
                .healthyThreshold(service.getHealthyThreshold())
                .unhealthyThreshold(service.getUnhealthyThreshold())
                .description(service.getDescription())
                .metadata(service.getMetadata())
                .enabled(service.getEnabled())
                .createdBy(service.getCreatedBy())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .targets(service.getTargets() != null ?
                        service.getTargets().stream()
                            .map(targetMapper::toResponse)
                            .collect(Collectors.toList()) : null)
                .activeTargetCount(service.getTargets() != null ?
                        (int) service.getTargets().stream()
                            .filter(t -> t.getEnabled() && "HEALTHY".equals(t.getHealthStatus()))
                            .count() : 0)
                .build();
    }
}
