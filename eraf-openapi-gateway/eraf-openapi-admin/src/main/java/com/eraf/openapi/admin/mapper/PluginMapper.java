package com.eraf.openapi.admin.mapper;

import com.eraf.openapi.admin.dto.PluginRequest;
import com.eraf.openapi.admin.dto.PluginResponse;
import com.eraf.openapi.core.domain.GatewayConsumerGroup;
import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.core.domain.GatewayRoute;
import com.eraf.openapi.core.domain.GatewayService;
import org.springframework.stereotype.Component;

/**
 * Plugin Entity ↔ DTO 변환 Mapper
 */
@Component
public class PluginMapper {

    public GatewayPlugin toEntity(PluginRequest request, GatewayRoute route, GatewayService service, GatewayConsumerGroup consumerGroup) {
        return GatewayPlugin.builder()
                .name(request.getName())
                .scope(request.getScope())
                .route(route)
                .service(service)
                .consumerGroup(consumerGroup)
                .config(request.getConfig())
                .priority(request.getPriority())
                .description(request.getDescription())
                .enabled(request.getEnabled())
                .build();
    }

    public void updateEntity(GatewayPlugin plugin, PluginRequest request,
                            GatewayRoute route, GatewayService service, GatewayConsumerGroup consumerGroup) {
        plugin.setName(request.getName());
        plugin.setScope(request.getScope());
        plugin.setRoute(route);
        plugin.setService(service);
        plugin.setConsumerGroup(consumerGroup);
        plugin.setConfig(request.getConfig());
        plugin.setPriority(request.getPriority());
        plugin.setDescription(request.getDescription());
        plugin.setEnabled(request.getEnabled());
    }

    public PluginResponse toResponse(GatewayPlugin plugin) {
        return PluginResponse.builder()
                .id(plugin.getId())
                .name(plugin.getName())
                .scope(plugin.getScope())
                .routeId(plugin.getRoute() != null ? plugin.getRoute().getId() : null)
                .routeName(plugin.getRoute() != null ? plugin.getRoute().getName() : null)
                .serviceId(plugin.getService() != null ? plugin.getService().getId() : null)
                .serviceName(plugin.getService() != null ? plugin.getService().getName() : null)
                .consumerGroupId(plugin.getConsumerGroup() != null ? plugin.getConsumerGroup().getId() : null)
                .consumerGroupName(plugin.getConsumerGroup() != null ? plugin.getConsumerGroup().getName() : null)
                .config(plugin.getConfig())
                .priority(plugin.getPriority())
                .description(plugin.getDescription())
                .enabled(plugin.getEnabled())
                .createdBy(plugin.getCreatedBy())
                .createdAt(plugin.getCreatedAt())
                .updatedAt(plugin.getUpdatedAt())
                .build();
    }
}
