package com.eraf.session;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.session.Session;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

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
     * 세션 쿠키 설정 (프로퍼티에서 읽어 적용)
     */
    @Bean
    @ConditionalOnMissingBean(CookieSerializer.class)
    public CookieSerializer cookieSerializer(ErafSessionProperties properties) {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName(properties.getCookieName());
        serializer.setCookiePath(properties.getCookiePath());
        serializer.setUseSecureCookie(properties.isSecureCookie());
        serializer.setUseHttpOnlyCookie(properties.isHttpOnlyCookie());
        serializer.setCookieMaxAge(properties.getCookieMaxAge());
        serializer.setSameSite(properties.getCookieSameSite());

        if (properties.getCookieDomain() != null && !properties.getCookieDomain().isEmpty()) {
            serializer.setDomainName(properties.getCookieDomain());
        }

        return serializer;
    }

    /**
     * 세션 이벤트 퍼블리셔
     */
    @Bean
    @ConditionalOnMissingBean
    public SessionEventPublisher sessionEventPublisher(ApplicationEventPublisher eventPublisher) {
        return new SessionEventPublisher(eventPublisher);
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
