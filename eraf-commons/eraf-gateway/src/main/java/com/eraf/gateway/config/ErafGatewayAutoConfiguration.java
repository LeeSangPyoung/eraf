package com.eraf.gateway.config;

import com.eraf.gateway.ErafGatewayProperties;
import com.eraf.gateway.discovery.DiscoveryRouteLocator;
import com.eraf.gateway.route.GatewayRouteConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * ERAF Gateway Auto-Configuration
 *
 * Spring Cloud Gateway 기반 API Gateway 자동 구성
 */
@AutoConfiguration
@ConditionalOnClass(RouteLocator.class)
@ConditionalOnProperty(prefix = "eraf.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ErafGatewayProperties.class)
public class ErafGatewayAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ErafGatewayAutoConfiguration.class);

    /**
     * Gateway Route Configurer 빈 등록
     *
     * <p>JwtTokenProvider가 classpath에 있으면 자동으로 주입하여 완전한 JWT 검증 수행</p>
     * <p>JwtTokenProvider가 없으면 기본 형식 검증만 수행</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public GatewayRouteConfigurer gatewayRouteConfigurer(
            ErafGatewayProperties properties,
            RouteLocatorBuilder builder,
            ApplicationContext applicationContext) {

        // JwtTokenProvider를 ApplicationContext에서 찾기 (eraf-security 모듈이 optional이므로)
        Object jwtTokenProvider = null;
        try {
            Class<?> jwtTokenProviderClass = Class.forName("com.eraf.security.jwt.JwtTokenProvider");
            jwtTokenProvider = applicationContext.getBean(jwtTokenProviderClass);
            log.info("JwtTokenProvider found - Full JWT validation enabled (signature + expiration verification)");
        } catch (ClassNotFoundException e) {
            log.info("JwtTokenProvider class not found on classpath - JWT authentication will use basic format validation only");
        } catch (Exception e) {
            log.warn("JwtTokenProvider class found but bean not available: {} - Falling back to basic validation", e.getMessage());
        }

        return new GatewayRouteConfigurer(properties, builder, jwtTokenProvider);
    }

    /**
     * Custom Route Locator 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean(name = "erafRouteLocator")
    public RouteLocator erafRouteLocator(GatewayRouteConfigurer configurer) {
        return configurer.buildRoutes();
    }

    /**
     * Discovery-based Route Locator 빈 등록 (선택적)
     * DiscoveryClient가 classpath에 있고 discovery-enabled=true일 때만 활성화
     */
    @Bean
    @ConditionalOnProperty(prefix = "eraf.gateway", name = "discovery-enabled", havingValue = "true")
    @ConditionalOnClass(name = "org.springframework.cloud.client.discovery.DiscoveryClient")
    @ConditionalOnMissingBean
    public RouteDefinitionLocator discoveryRouteLocator(
            DiscoveryLocatorProperties properties,
            DiscoveryClient discoveryClient,
            ErafGatewayProperties erafProperties) {
        return new DiscoveryRouteLocator(properties, "lb://", discoveryClient, erafProperties.getExcludedServices());
    }
}
