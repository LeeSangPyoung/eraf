package com.eraf.sample.config;

import com.eraf.security.audit.SecurityAuditLogger;
import com.eraf.security.bot.BotDetector;
import com.eraf.security.bot.UserAgentBotDetector;
import com.eraf.security.jwt.JwtProperties;
import com.eraf.security.jwt.JwtTokenProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Security Configuration for Sample Application
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenProvider jwtTokenProvider(JwtProperties properties) {
        return new JwtTokenProvider(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityAuditLogger securityAuditLogger() {
        return new SecurityAuditLogger();
    }

    @Bean
    @ConditionalOnMissingBean
    public BotDetector botDetector() {
        // 허용할 봇 목록 (Postman, cURL 등 개발 도구 허용)
        Set<String> allowedBots = Set.of("Postman", "cURL", "Insomnia", "HTTPie");
        // 알 수 없는 봇 차단 여부
        boolean blockUnknownBots = false;
        return new UserAgentBotDetector(allowedBots, blockUnknownBots);
    }
}
