package com.eraf.starter.session;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.session.Session;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * ERAF Session Auto Configuration
 * JWT + Redis 기반 세션 관리
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(Session.class)
@EnableConfigurationProperties(ErafSessionProperties.class)
public class ErafSessionAutoConfiguration {

    /**
     * JWT 토큰 프로바이더
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.session.jwt.enabled", havingValue = "true", matchIfMissing = true)
    public ErafJwtTokenProvider erafJwtTokenProvider(ErafSessionProperties properties) {
        return new ErafJwtTokenProvider(properties);
    }

    /**
     * Redis 세션 설정
     */
    @Configuration
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnBean(RedisConnectionFactory.class)
    @EnableRedisHttpSession
    public static class RedisSessionConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ErafSessionService erafSessionService(StringRedisTemplate redisTemplate,
                                                      ErafSessionProperties properties) {
            return new ErafSessionService(redisTemplate, properties);
        }
    }
}
