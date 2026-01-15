package com.eraf.starter.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ERAF Security Auto Configuration
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(SecurityFilterChain.class)
@EnableConfigurationProperties(ErafSecurityProperties.class)
@EnableWebSecurity
public class ErafSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain erafSecurityFilterChain(HttpSecurity http, ErafSecurityProperties properties) throws Exception {
        if (properties.isDisableCsrf()) {
            http.csrf(csrf -> csrf.disable());
        }

        if (properties.isDisableFrameOptions()) {
            http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        }

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(properties.getPermitAllPatterns()).permitAll()
                .anyRequest().authenticated()
        );

        return http.build();
    }
}
