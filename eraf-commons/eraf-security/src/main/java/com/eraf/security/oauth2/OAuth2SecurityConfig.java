package com.eraf.security.oauth2;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

/**
 * OAuth2/OIDC Security Configuration
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
            if (provider != null) {
                registrations.add(
                    ClientRegistration.withRegistrationId(name)
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
                        .build()
                );
            }
        });

        return new InMemoryClientRegistrationRepository(registrations);
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    @Order(90)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/")
            )
            .oauth2Client(oauth2 -> {});

        if (properties.getResourceServer().isEnabled()) {
            http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwkSetUri(properties.getResourceServer().getJwkSetUri()))
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
