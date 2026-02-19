package com.eraf.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ERAF Gateway Configuration Properties
 */
@ConfigurationProperties(prefix = "eraf.gateway")
public class ErafGatewayProperties {

    /**
     * Gateway 활성화 여부
     */
    private boolean enabled = true;

    /**
     * 라우트 설정
     */
    private List<RouteConfig> routes = new ArrayList<>();

    /**
     * Rate Limiting 설정
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /**
     * 서비스 디스커버리 활성화
     */
    private boolean discoveryEnabled = false;

    /**
     * 디스커버리 서버 URL (Eureka, Consul 등)
     */
    private String discoveryServerUrl;

    /**
     * 디스커버리 라우트에서 제외할 서비스 이름 목록 (소문자)
     * 예: eureka-server, config-server
     */
    private Set<String> excludedServices = new HashSet<>();

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<RouteConfig> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteConfig> routes) {
        this.routes = routes;
    }

    public RateLimitConfig getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimitConfig rateLimit) {
        this.rateLimit = rateLimit;
    }

    public boolean isDiscoveryEnabled() {
        return discoveryEnabled;
    }

    public void setDiscoveryEnabled(boolean discoveryEnabled) {
        this.discoveryEnabled = discoveryEnabled;
    }

    public String getDiscoveryServerUrl() {
        return discoveryServerUrl;
    }

    public void setDiscoveryServerUrl(String discoveryServerUrl) {
        this.discoveryServerUrl = discoveryServerUrl;
    }

    public Set<String> getExcludedServices() {
        return excludedServices;
    }

    public void setExcludedServices(Set<String> excludedServices) {
        this.excludedServices = excludedServices;
    }

    /**
     * 라우트 설정
     */
    public static class RouteConfig {
        private String id;
        private String uri;
        private List<String> predicates = new ArrayList<>();
        private List<FilterConfig> filters = new ArrayList<>();
        private int order = 0;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public List<String> getPredicates() {
            return predicates;
        }

        public void setPredicates(List<String> predicates) {
            this.predicates = predicates;
        }

        public List<FilterConfig> getFilters() {
            return filters;
        }

        public void setFilters(List<FilterConfig> filters) {
            this.filters = filters;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }
    }

    /**
     * 필터 설정
     */
    public static class FilterConfig {
        private String name;
        private String args;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getArgs() {
            return args;
        }

        public void setArgs(String args) {
            this.args = args;
        }
    }

    /**
     * Rate Limiting 설정
     */
    public static class RateLimitConfig {
        private boolean enabled = false;
        private int replenishRate = 10;
        private int burstCapacity = 20;
        private Duration requestedTokens = Duration.ofSeconds(1);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getReplenishRate() {
            return replenishRate;
        }

        public void setReplenishRate(int replenishRate) {
            this.replenishRate = replenishRate;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }

        public Duration getRequestedTokens() {
            return requestedTokens;
        }

        public void setRequestedTokens(Duration requestedTokens) {
            this.requestedTokens = requestedTokens;
        }
    }
}
