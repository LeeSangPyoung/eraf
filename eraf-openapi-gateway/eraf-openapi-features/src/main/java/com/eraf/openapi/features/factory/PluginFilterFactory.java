package com.eraf.openapi.features.factory;

import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.core.repository.GatewayPluginRepository;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Plugin Filter Factory
 * DB 기반 동적 플러그인 필터 생성 및 관리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PluginFilterFactory {

    private final GatewayPluginRepository pluginRepository;
    private final List<PluginGatewayFilter> pluginFilters;

    // 플러그인 이름 → 필터 구현체 매핑
    private Map<String, PluginGatewayFilter> filterMap;

    /**
     * 초기화 - 플러그인 필터 맵 생성
     */
    public void initialize() {
        filterMap = pluginFilters.stream()
                .collect(Collectors.toMap(
                        PluginGatewayFilter::getPluginName,
                        filter -> filter
                ));

        log.info("Initialized {} plugin filters: {}",
                filterMap.size(), filterMap.keySet());
    }

    /**
     * Route ID에 대한 플러그인 필터 체인 생성
     */
    public List<GatewayFilter> createRouteFilters(Long routeId) {
        List<GatewayPlugin> plugins = pluginRepository
                .findByRouteIdAndEnabledTrueOrderByPriority(routeId);

        return createFiltersFromPlugins(plugins);
    }

    /**
     * Service ID에 대한 플러그인 필터 체인 생성
     */
    public List<GatewayFilter> createServiceFilters(Long serviceId) {
        List<GatewayPlugin> plugins = pluginRepository
                .findByServiceIdAndEnabledTrueOrderByPriority(serviceId);

        return createFiltersFromPlugins(plugins);
    }

    /**
     * Global 플러그인 필터 체인 생성
     */
    public List<GatewayFilter> createGlobalFilters() {
        List<GatewayPlugin> plugins = pluginRepository
                .findGlobalPluginsOrderByPriority();

        return createFiltersFromPlugins(plugins);
    }

    /**
     * 플러그인 설정으로부터 필터 생성
     */
    private List<GatewayFilter> createFiltersFromPlugins(List<GatewayPlugin> plugins) {
        if (filterMap == null) {
            initialize();
        }

        List<GatewayFilter> filters = new ArrayList<>();

        for (GatewayPlugin plugin : plugins) {
            PluginGatewayFilter filterImpl = filterMap.get(plugin.getName());

            if (filterImpl == null) {
                log.warn("Unknown plugin type: {}", plugin.getName());
                continue;
            }

            try {
                GatewayFilter filter = filterImpl.createFilter(plugin);
                filters.add(filter);
                log.debug("Created filter for plugin: {} (priority={})",
                        plugin.getName(), plugin.getPriority());
            } catch (Exception e) {
                log.error("Failed to create filter for plugin: {}", plugin.getName(), e);
            }
        }

        return filters;
    }

    /**
     * Route에 적용되는 모든 필터 생성 (Global + Service + Route)
     */
    public List<GatewayFilter> createAllFiltersForRoute(Long routeId, Long serviceId) {
        List<GatewayFilter> allFilters = new ArrayList<>();

        // 1. Global 필터
        allFilters.addAll(createGlobalFilters());

        // 2. Service 필터
        if (serviceId != null) {
            allFilters.addAll(createServiceFilters(serviceId));
        }

        // 3. Route 필터
        if (routeId != null) {
            allFilters.addAll(createRouteFilters(routeId));
        }

        log.debug("Created total {} filters for route {} (service={})",
                allFilters.size(), routeId, serviceId);

        return allFilters;
    }

    /**
     * 사용 가능한 모든 플러그인 타입 조회
     */
    public Set<String> getAvailablePluginTypes() {
        if (filterMap == null) {
            initialize();
        }
        return filterMap.keySet();
    }
}
