package com.eraf.openapi.features.config;

import com.eraf.openapi.features.factory.PluginFilterFactory;
import com.eraf.openapi.features.route.DynamicRouteLocator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Configuration
 * Spring Cloud Gateway 설정
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayConfiguration {

    private final DynamicRouteLocator dynamicRouteLocator;
    private final PluginFilterFactory pluginFilterFactory;

    /**
     * 초기화 - Plugin Filter Factory 초기화
     */
    @PostConstruct
    public void init() {
        log.info("Initializing ERAF OpenAPI Gateway...");
        pluginFilterFactory.initialize();
        log.info("ERAF OpenAPI Gateway initialized successfully");
    }

    /**
     * Dynamic Route Locator Bean 등록
     */
    @Bean
    public RouteLocator customRouteLocator() {
        log.info("Creating dynamic route locator from database");
        return dynamicRouteLocator.createRouteLocator();
    }
}
