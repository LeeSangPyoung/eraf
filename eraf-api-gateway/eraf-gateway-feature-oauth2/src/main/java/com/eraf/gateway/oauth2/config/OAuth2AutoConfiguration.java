package com.eraf.gateway.oauth2.config;

import com.eraf.gateway.common.filter.FilterOrder;
import com.eraf.gateway.oauth2.filter.OAuth2Filter;
import com.eraf.gateway.oauth2.repository.OAuth2ClientRepository;
import com.eraf.gateway.oauth2.repository.OAuth2TokenRepository;
import com.eraf.gateway.oauth2.repository.InMemoryOAuth2ClientRepository;
import com.eraf.gateway.oauth2.repository.InMemoryOAuth2TokenRepository;
import com.eraf.gateway.oauth2.service.OAuth2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth2 자동 설정
 */
@Slf4j
@Configuration
@ConditionalOnClass(OAuth2Filter.class)
@ConditionalOnProperty(prefix = "eraf.gateway.oauth2", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(OAuth2Properties.class)
public class OAuth2AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OAuth2TokenRepository oauth2TokenRepository() {
        log.info("Initializing InMemoryOAuth2TokenRepository");
        return new InMemoryOAuth2TokenRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2ClientRepository oauth2ClientRepository() {
        log.info("Initializing InMemoryOAuth2ClientRepository");
        return new InMemoryOAuth2ClientRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2Service oauth2Service(OAuth2TokenRepository tokenRepository,
                                       OAuth2ClientRepository clientRepository) {
        log.info("Initializing OAuth2Service");
        return new OAuth2Service(tokenRepository, clientRepository);
    }

    @Bean
    public FilterRegistrationBean<OAuth2Filter> oauth2FilterRegistration(
            OAuth2Service oauth2Service,
            OAuth2Properties properties) {

        log.info("Registering OAuth2Filter with order: {}", FilterOrder.OAUTH2);

        OAuth2Filter filter = new OAuth2Filter(oauth2Service, properties);

        FilterRegistrationBean<OAuth2Filter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(FilterOrder.OAUTH2);
        registration.addUrlPatterns("/*");

        return registration;
    }
}
