package com.eraf.openapi.features.route;

import com.eraf.openapi.core.domain.GatewayRoute;
import com.eraf.openapi.core.domain.GatewayService;
import com.eraf.openapi.core.domain.GatewayTarget;
import com.eraf.openapi.core.repository.GatewayRouteRepository;
import com.eraf.openapi.core.repository.GatewayTargetRepository;
import com.eraf.openapi.features.factory.PluginFilterFactory;
import com.eraf.openapi.features.loadbalancer.LoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Dynamic Route Locator
 * DB 기반 동적 라우팅 설정 + 전략 패턴 기반 로드밸런싱
 */
@Slf4j
@Component
public class DynamicRouteLocator {

    private final GatewayRouteRepository routeRepository;
    private final GatewayTargetRepository targetRepository;
    private final PluginFilterFactory pluginFilterFactory;
    private final RouteLocatorBuilder routeLocatorBuilder;

    /**
     * 로드밸런싱 알고리즘 맵 (Strategy Pattern)
     * Key: 알고리즘 이름 (ROUND_ROBIN, WEIGHTED_ROUND_ROBIN, LEAST_CONNECTIONS, IP_HASH)
     */
    private final Map<String, LoadBalancer> loadBalancerMap;

    public DynamicRouteLocator(GatewayRouteRepository routeRepository,
                                GatewayTargetRepository targetRepository,
                                PluginFilterFactory pluginFilterFactory,
                                RouteLocatorBuilder routeLocatorBuilder,
                                List<LoadBalancer> loadBalancers) {
        this.routeRepository = routeRepository;
        this.targetRepository = targetRepository;
        this.pluginFilterFactory = pluginFilterFactory;
        this.routeLocatorBuilder = routeLocatorBuilder;

        // LoadBalancer 빈들을 알고리즘 이름으로 매핑
        this.loadBalancerMap = loadBalancers.stream()
                .collect(Collectors.toMap(LoadBalancer::getAlgorithm, Function.identity()));

        log.info("Registered load balancing algorithms: {}", loadBalancerMap.keySet());
    }

    /**
     * DB에서 모든 활성화된 Route 로드하여 RouteLocator 생성
     */
    public RouteLocator createRouteLocator() {
        RouteLocatorBuilder.Builder routes = routeLocatorBuilder.routes();

        List<GatewayRoute> gatewayRoutes = routeRepository.findEnabledWithServiceOrderByPriority();

        log.info("Loading {} active routes from database", gatewayRoutes.size());

        for (GatewayRoute gatewayRoute : gatewayRoutes) {
            try {
                routes.route(String.valueOf(gatewayRoute.getId()), predicateSpec -> {
                    // 1. Path 설정
                    var spec = predicateSpec.path(gatewayRoute.getPaths().toArray(new String[0]));

                    // 2. Method 설정
                    if (gatewayRoute.getMethods() != null && !gatewayRoute.getMethods().isEmpty()) {
                        spec = spec.and()
                                .method(gatewayRoute.getMethods().toArray(new String[0]));
                    }

                    // 3. Host 설정
                    if (gatewayRoute.getHosts() != null && !gatewayRoute.getHosts().isEmpty()) {
                        spec = spec.and()
                                .host(gatewayRoute.getHosts().toArray(new String[0]));
                    }

                    // 4. Target URI 설정 (로드 밸런싱)
                    URI targetUri = selectTarget(gatewayRoute.getService());

                    // 5. 필터 적용 (플러그인 + Path 변환) + URI 설정
                    List<GatewayFilter> pluginFilters = pluginFilterFactory.createAllFiltersForRoute(
                            gatewayRoute.getId(),
                            gatewayRoute.getService().getId()
                    );

                    log.debug("Route configured: {} -> {} (priority={}, lb={})",
                            gatewayRoute.getPaths(), targetUri, gatewayRoute.getPriority(),
                            gatewayRoute.getService().getLoadBalancingAlgorithm());

                    return spec.filters(f -> {
                        // 플러그인 필터 추가
                        for (GatewayFilter filter : pluginFilters) {
                            f.filter(filter);
                        }

                        // Path 변환 필터 추가
                        if (gatewayRoute.getStripPath()) {
                            f.stripPrefix(1);
                        }

                        if (gatewayRoute.getPathPrefix() != null) {
                            f.prefixPath(gatewayRoute.getPathPrefix());
                        }

                        return f;
                    }).uri(targetUri);
                });

            } catch (Exception e) {
                log.error("Failed to configure route: {}", gatewayRoute.getName(), e);
            }
        }

        return routes.build();
    }

    /**
     * Service의 Target 중 하나를 선택 (전략 패턴 기반 로드 밸런싱)
     */
    private URI selectTarget(GatewayService service) {
        List<GatewayTarget> targets = targetRepository
                .findHealthyTargetsByServiceId(service.getId());

        if (targets.isEmpty()) {
            log.warn("No healthy targets found for service: {}, using service config",
                    service.getName());
            return URI.create(String.format("%s://%s:%d%s",
                    service.getProtocol(),
                    service.getHost(),
                    service.getPort(),
                    service.getBasePath() != null ? service.getBasePath() : ""));
        }

        // 전략 패턴: 알고리즘 이름으로 LoadBalancer 조회
        String algorithm = service.getLoadBalancingAlgorithm();
        LoadBalancer loadBalancer = loadBalancerMap.getOrDefault(algorithm,
                loadBalancerMap.get("ROUND_ROBIN"));

        if (loadBalancer == null) {
            log.error("No load balancer found for algorithm: {}, falling back to first target", algorithm);
            GatewayTarget fallback = targets.get(0);
            return buildTargetUri(service, fallback);
        }

        // 로드 밸런서로 타겟 선택 (clientIp는 라우트 빌드 시점에서는 null, 런타임에서 처리)
        GatewayTarget selectedTarget = loadBalancer.select(targets, null);

        return buildTargetUri(service, selectedTarget);
    }

    /**
     * 타겟 URI 생성
     */
    private URI buildTargetUri(GatewayService service, GatewayTarget target) {
        return URI.create(String.format("%s://%s:%d%s",
                service.getProtocol(),
                target.getHost(),
                target.getPort(),
                service.getBasePath() != null ? service.getBasePath() : ""));
    }
}
