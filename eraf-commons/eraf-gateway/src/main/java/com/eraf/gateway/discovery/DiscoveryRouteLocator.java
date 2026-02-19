package com.eraf.gateway.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Discovery-based Route Locator
 * 서비스 디스커버리(Eureka, Consul 등)를 통해 동적으로 라우트 생성
 *
 * <p>DiscoveryClient에 등록된 서비스 목록을 주기적으로 조회하여 라우트를 생성합니다.</p>
 *
 * <p>생성되는 라우트 규칙:</p>
 * <ul>
 *   <li>Path: /{service-name}/**</li>
 *   <li>URI: lb://{SERVICE-NAME} (LoadBalancer)</li>
 *   <li>Filter: StripPrefix=1 (경로 앞 세그먼트 제거)</li>
 * </ul>
 *
 * <p>설정 예시:</p>
 * <pre>
 * eraf:
 *   gateway:
 *     discovery-enabled: true
 *     excluded-services:
 *       - eureka-server
 *       - config-server
 * </pre>
 */
public class DiscoveryRouteLocator implements RouteDefinitionLocator {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryRouteLocator.class);

    private final DiscoveryLocatorProperties properties;
    private final String serviceNamePrefix;
    private final DiscoveryClient discoveryClient;
    private final Set<String> excludedServices;

    public DiscoveryRouteLocator(
            DiscoveryLocatorProperties properties,
            String serviceNamePrefix,
            DiscoveryClient discoveryClient,
            Set<String> excludedServices) {
        this.properties = properties;
        this.serviceNamePrefix = serviceNamePrefix != null ? serviceNamePrefix : "lb://";
        this.discoveryClient = discoveryClient;
        this.excludedServices = excludedServices != null ? excludedServices : Set.of();
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        if (!properties.isEnabled()) {
            log.debug("DiscoveryRouteLocator is disabled");
            return Flux.empty();
        }

        return Flux.defer(() -> {
            List<String> services = discoveryClient.getServices();
            log.debug("Fetched {} services from discovery client: {}", services.size(), services);

            return Flux.fromIterable(services)
                    .filter(serviceName -> !excludedServices.contains(serviceName.toLowerCase()))
                    .map(this::createRouteDefinition)
                    .doOnNext(route -> log.debug("Discovery route created: id={}, uri={}", route.getId(), route.getUri()))
                    .doOnError(e -> log.error("Failed to load routes from discovery client", e));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 서비스 이름으로부터 RouteDefinition 생성
     */
    private RouteDefinition createRouteDefinition(String serviceName) {
        String normalizedName = properties.isLowerCaseServiceId()
                ? serviceName.toLowerCase()
                : serviceName;

        RouteDefinition definition = new RouteDefinition();
        definition.setId("discovery-" + normalizedName);
        definition.setUri(URI.create(serviceNamePrefix + serviceName));

        // Path 기반 라우팅: /{service-name}/**
        PredicateDefinition pathPredicate = new PredicateDefinition();
        pathPredicate.setName("Path");
        pathPredicate.addArg("pattern", "/" + normalizedName + "/**");
        definition.setPredicates(List.of(pathPredicate));

        // StripPrefix 필터: 경로 앞 1개 세그먼트 제거 (/service-name/api/v1 → /api/v1)
        FilterDefinition stripPrefixFilter = new FilterDefinition();
        stripPrefixFilter.setName("StripPrefix");
        stripPrefixFilter.addArg("parts", "1");
        definition.setFilters(List.of(stripPrefixFilter));

        return definition;
    }
}
