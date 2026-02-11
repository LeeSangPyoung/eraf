package com.eraf.openapi.admin.mapper;

import com.eraf.openapi.admin.dto.RouteRequest;
import com.eraf.openapi.admin.dto.RouteResponse;
import com.eraf.openapi.core.domain.GatewayRoute;
import com.eraf.openapi.core.domain.GatewayService;
import org.springframework.stereotype.Component;

/**
 * Route Entity ↔ DTO 변환 Mapper
 */
@Component
public class RouteMapper {

    public GatewayRoute toEntity(RouteRequest request, GatewayService service) {
        return GatewayRoute.builder()
                .name(request.getName())
                .paths(request.getPaths())
                .methods(request.getMethods())
                .hosts(request.getHosts())
                .headers(request.getHeaders())
                .service(service)
                .priority(request.getPriority())
                .stripPath(request.getStripPath())
                .pathPrefix(request.getPathPrefix())
                .apiVersion(request.getApiVersion())
                .description(request.getDescription())
                .metadata(request.getMetadata())
                .enabled(request.getEnabled())
                .build();
    }

    public void updateEntity(GatewayRoute route, RouteRequest request, GatewayService service) {
        route.setName(request.getName());
        route.setPaths(request.getPaths());
        route.setMethods(request.getMethods());
        route.setHosts(request.getHosts());
        route.setHeaders(request.getHeaders());
        route.setService(service);
        route.setPriority(request.getPriority());
        route.setStripPath(request.getStripPath());
        route.setPathPrefix(request.getPathPrefix());
        route.setApiVersion(request.getApiVersion());
        route.setDescription(request.getDescription());
        route.setMetadata(request.getMetadata());
        route.setEnabled(request.getEnabled());
    }

    public RouteResponse toResponse(GatewayRoute route) {
        return RouteResponse.builder()
                .id(route.getId())
                .name(route.getName())
                .paths(route.getPaths())
                .methods(route.getMethods())
                .hosts(route.getHosts())
                .headers(route.getHeaders())
                .serviceId(route.getService() != null ? route.getService().getId() : null)
                .serviceName(route.getService() != null ? route.getService().getName() : null)
                .priority(route.getPriority())
                .stripPath(route.getStripPath())
                .pathPrefix(route.getPathPrefix())
                .apiVersion(route.getApiVersion())
                .description(route.getDescription())
                .metadata(route.getMetadata())
                .enabled(route.getEnabled())
                .createdBy(route.getCreatedBy())
                .createdAt(route.getCreatedAt())
                .updatedAt(route.getUpdatedAt())
                .build();
    }
}
