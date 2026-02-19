package com.eraf.security.oauth2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

/**
 * OAuth2/OIDC Security Configuration
 *
 * <p>Provider에 {@code issuerUri}가 설정된 경우 OIDC 자동발견(well-known)으로
 * endpoints를 자동으로 구성합니다. 그렇지 않으면 개별 URI를 직접 사용합니다.</p>
 */
@Configuration
@ConditionalOnProperty(name = "eraf.security.oauth2.enabled", havingValue = "true")
@EnableConfigurationProperties(OAuth2Properties.class)
public class OAuth2SecurityConfig {

    private final OAuth2Properties properties;

    public OAuth2SecurityConfig(OAuth2Properties properties) {
        this.properties = properties;
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();

        properties.getClient().getRegistration().forEach((name, reg) -> {
            OAuth2Properties.Provider provider = properties.getProviders().get(name);
            ClientRegistration registration;

            if (provider != null && provider.getIssuerUri() != null) {
                // OIDC 자동발견: issuerUri 기반으로 endpoints 자동 구성
                registration = ClientRegistrations.fromIssuerLocation(provider.getIssuerUri())
                        .registrationId(name)
                        .clientId(reg.getClientId())
                        .clientSecret(reg.getClientSecret())
                        .scope(reg.getScope().split(","))
                        .redirectUri(reg.getRedirectUri())
                        .authorizationGrantType(new AuthorizationGrantType(reg.getAuthorizationGrantType()))
                        .build();
            } else if (provider != null) {
                // 수동 URI 설정
                registration = ClientRegistration.withRegistrationId(name)
                        .clientId(reg.getClientId())
                        .clientSecret(reg.getClientSecret())
                        .scope(reg.getScope().split(","))
                        .authorizationUri(provider.getAuthorizationUri())
                        .tokenUri(provider.getTokenUri())
                        .userInfoUri(provider.getUserInfoUri())
                        .userNameAttributeName(provider.getUserNameAttribute())
                        .jwkSetUri(provider.getJwkSetUri())
                        .redirectUri(reg.getRedirectUri())
                        .authorizationGrantType(new AuthorizationGrantType(reg.getAuthorizationGrantType()))
                        .build();
            } else {
                return;
            }

            registrations.add(registration);
        });

        return new InMemoryClientRegistrationRepository(registrations);
    }

    @Bean
    @ConditionalOnMissingBean(ErafOAuth2LoginSuccessHandler.class)
    public ErafOAuth2LoginSuccessHandler oauth2LoginSuccessHandler(ApplicationEventPublisher eventPublisher) {
        return new ErafOAuth2LoginSuccessHandler(eventPublisher, properties.getSuccessUrl());
    }

    @Bean
    @ConditionalOnMissingBean(ErafOAuth2LoginFailureHandler.class)
    public ErafOAuth2LoginFailureHandler oauth2LoginFailureHandler(ApplicationEventPublisher eventPublisher) {
        return new ErafOAuth2LoginFailureHandler(eventPublisher, properties.getFailureUrl());
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    @Order(90)
    public SecurityFilterChain oauth2SecurityFilterChain(
            HttpSecurity http,
            ErafOAuth2LoginSuccessHandler successHandler,
            ErafOAuth2LoginFailureHandler failureHandler) throws Exception {

        http
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
            )
            .oauth2Client(oauth2 -> {});

        if (properties.getResourceServer().isEnabled()) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {
                    String issuerUri = properties.getResourceServer().getIssuerUri();
                    String jwkSetUri = properties.getResourceServer().getJwkSetUri();
                    if (issuerUri != null) {
                        jwt.jwkSetUri(issuerUri + "/protocol/openid-connect/certs");
                    } else if (jwkSetUri != null) {
                        jwt.jwkSetUri(jwkSetUri);
                    }
                })
            );
        }

        return http.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OAuth2AuthorizedClientService.class)
    public OAuth2UserInfoService oauth2UserInfoService(OAuth2AuthorizedClientService authorizedClientService) {
        return new OAuth2UserInfoService(authorizedClientService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(OAuth2UserInfoService.class)
    public OAuth2TokenRelayInterceptor oauth2TokenRelayInterceptor(OAuth2UserInfoService userInfoService) {
        return new OAuth2TokenRelayInterceptor(userInfoService);
    }
}
