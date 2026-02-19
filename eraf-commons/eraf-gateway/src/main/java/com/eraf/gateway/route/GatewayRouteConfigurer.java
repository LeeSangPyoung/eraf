package com.eraf.gateway.route;

import com.eraf.gateway.ErafGatewayProperties;
import com.eraf.gateway.filter.AuthenticationGatewayFilter;
import com.eraf.gateway.filter.RateLimitGatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

import java.util.List;

/**
 * Gateway Route Configurer
 * 라우트 설정을 프로그래밍 방식으로 구성
 */
public class GatewayRouteConfigurer {

    private final ErafGatewayProperties properties;
    private final RouteLocatorBuilder builder;
    private final Object jwtTokenProvider;

    public GatewayRouteConfigurer(ErafGatewayProperties properties, RouteLocatorBuilder builder) {
        this(properties, builder, null);
    }

    public GatewayRouteConfigurer(ErafGatewayProperties properties, RouteLocatorBuilder builder, Object jwtTokenProvider) {
        this.properties = properties;
        this.builder = builder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 라우트 로케이터 빌드
     */
    public RouteLocator buildRoutes() {
        RouteLocatorBuilder.Builder routes = builder.routes();

        // Properties에서 정의한 라우트 추가
        for (ErafGatewayProperties.RouteConfig routeConfig : properties.getRoutes()) {
            routes.route(routeConfig.getId(), r -> {
                // Predicates 추가
                var predicateSpec = r.path(routeConfig.getPredicates().toArray(new String[0]));

                // Filters 추가
                var filterSpec = predicateSpec.filters(f -> {
                    // Rate Limit Filter
                    if (properties.getRateLimit().isEnabled()) {
                        f.filter(new RateLimitGatewayFilter(properties.getRateLimit()));
                    }

                    // Authentication Filter (기본 제외 패턴)
                    f.filter(new AuthenticationGatewayFilter(List.of(
                            "/actuator/.*",
                            "/health",
                            "/public/.*"
                    ), jwtTokenProvider));

                    // Custom filters from config
                    for (ErafGatewayProperties.FilterConfig filterConfig : routeConfig.getFilters()) {
                        applyFilter(f, filterConfig);
                    }

                    return f;
                });

                // URI 설정
                return filterSpec.uri(routeConfig.getUri());
            });
        }

        return routes.build();
    }

    private void applyFilter(GatewayFilterSpec f,
                              ErafGatewayProperties.FilterConfig filterConfig) {
        switch (filterConfig.getName().toLowerCase()) {
            case "stripprefix":
                f.stripPrefix(Integer.parseInt(filterConfig.getArgs()));
                break;
            case "addrequest header":
                String[] headerParts = filterConfig.getArgs().split(":");
                if (headerParts.length == 2) {
                    f.addRequestHeader(headerParts[0].trim(), headerParts[1].trim());
                }
                break;
            case "rewritepath":
                String[] pathParts = filterConfig.getArgs().split(",");
                if (pathParts.length == 2) {
                    f.rewritePath(pathParts[0].trim(), pathParts[1].trim());
                }
                break;
            default:
                // 기본적으로 무시
                break;
        }
    }
}
