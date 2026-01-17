package com.eraf.gateway.builder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * ERAF API Gateway Application
 * 빌드 시 선택된 기능만 포함된 Gateway
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {
        "com.eraf.gateway.common",
        "com.eraf.gateway.ratelimit",
        "com.eraf.gateway.apikey",
        "com.eraf.gateway.iprestriction",
        "com.eraf.gateway.jwt",
        "com.eraf.gateway.circuitbreaker",
        "com.eraf.gateway.analytics",
        "com.eraf.gateway.cache",
        "com.eraf.gateway.bot",
        "com.eraf.gateway.oauth2",
        "com.eraf.gateway.ratelimit.advanced",
        "com.eraf.gateway.validation",
        "com.eraf.gateway.loadbalancer",
        "com.eraf.gateway.analytics.advanced",
        "com.eraf.gateway.builder"
})
public class GatewayApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(GatewayApplication.class, args);

        log.info("==================================================");
        log.info("ERAF API Gateway Started Successfully");
        log.info("==================================================");

        // Print loaded features
        printLoadedFeatures(context);
    }

    private static void printLoadedFeatures(ConfigurableApplicationContext context) {
        log.info("Loaded Features:");
        log.info("--- Phase 1: Core Features ---");
        checkFeature(context, "com.eraf.gateway.ratelimit.config.RateLimitAutoConfiguration", "Rate Limit");
        checkFeature(context, "com.eraf.gateway.apikey.config.ApiKeyAutoConfiguration", "API Key");
        checkFeature(context, "com.eraf.gateway.iprestriction.config.IpRestrictionAutoConfiguration", "IP Restriction");
        checkFeature(context, "com.eraf.gateway.jwt.config.JwtAutoConfiguration", "JWT");
        checkFeature(context, "com.eraf.gateway.circuitbreaker.config.CircuitBreakerAutoConfiguration", "Circuit Breaker");
        checkFeature(context, "com.eraf.gateway.analytics.config.AnalyticsAutoConfiguration", "Analytics");
        checkFeature(context, "com.eraf.gateway.cache.config.CacheAutoConfiguration", "Cache");
        checkFeature(context, "com.eraf.gateway.bot.config.BotDetectionAutoConfiguration", "Bot Detection");

        log.info("--- Phase 2: Advanced Features ---");
        checkFeature(context, "com.eraf.gateway.oauth2.config.OAuth2AutoConfiguration", "OAuth2");
        checkFeature(context, "com.eraf.gateway.ratelimit.advanced.config.AdvancedRateLimitAutoConfiguration", "Advanced Rate Limit");
        checkFeature(context, "com.eraf.gateway.validation.config.ValidationAutoConfiguration", "Request Validation");
        checkFeature(context, "com.eraf.gateway.loadbalancer.config.LoadBalancerAutoConfiguration", "Load Balancer");
        checkFeature(context, "com.eraf.gateway.analytics.advanced.config.AdvancedAnalyticsAutoConfiguration", "Advanced Analytics");

        log.info("==================================================");
    }

    private static void checkFeature(ConfigurableApplicationContext context, String className, String featureName) {
        try {
            Class.forName(className);
            log.info("  ✓ {}", featureName);
        } catch (ClassNotFoundException e) {
            log.info("  ✗ {} (not included)", featureName);
        }
    }
}
