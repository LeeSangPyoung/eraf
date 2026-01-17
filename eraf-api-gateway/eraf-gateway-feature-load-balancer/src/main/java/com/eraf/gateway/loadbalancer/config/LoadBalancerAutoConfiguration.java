package com.eraf.gateway.loadbalancer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Auto-configuration for load balancer module.
 */
@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties(LoadBalancerProperties.class)
@ComponentScan(basePackages = "com.eraf.gateway.loadbalancer")
@ConditionalOnProperty(
        prefix = "eraf.gateway.load-balancer",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class LoadBalancerAutoConfiguration {

    public LoadBalancerAutoConfiguration() {
        log.info("Initializing ERAF Gateway Load Balancer Module");
    }

    /**
     * WebClient builder for HTTP proxy.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
