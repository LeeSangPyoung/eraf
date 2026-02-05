package com.eraf.security;

import com.eraf.security.apikey.ApiKeyAuthenticationFilter;
import com.eraf.security.apikey.ApiKeyProperties;
import com.eraf.security.audit.SecurityAuditLogger;
import com.eraf.security.audit.SecurityEventListener;
import com.eraf.security.cors.CorsProperties;
import com.eraf.security.jwt.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * ERAF Security Auto Configuration
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(SecurityFilterChain.class)
@EnableConfigurationProperties({
        ErafSecurityProperties.class,
        JwtProperties.class,
        ApiKeyProperties.class,
        CorsProperties.class
})
@EnableWebSecurity
public class ErafSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ===== JWT =====

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.security.jwt.enabled", havingValue = "true")
    public JwtTokenProvider jwtTokenProvider(JwtProperties properties) {
        return new JwtTokenProvider(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.security.jwt.enabled", havingValue = "true")
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider tokenProvider, JwtProperties properties) {
        return new JwtAuthenticationFilter(tokenProvider, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.security.jwt.enabled", havingValue = "true")
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.security.jwt.enabled", havingValue = "true")
    public JwtAccessDeniedHandler jwtAccessDeniedHandler() {
        return new JwtAccessDeniedHandler();
    }

    // ===== API Key =====

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.security.api-key.enabled", havingValue = "true")
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter(ApiKeyProperties properties) {
        return new ApiKeyAuthenticationFilter(properties);
    }

    // ===== Audit =====

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.security.audit.enabled", havingValue = "true", matchIfMissing = true)
    public SecurityAuditLogger securityAuditLogger() {
        return new SecurityAuditLogger();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.security.audit.enabled", havingValue = "true", matchIfMissing = true)
    public SecurityEventListener securityEventListener(SecurityAuditLogger auditLogger) {
        return new SecurityEventListener(auditLogger);
    }

    // ===== CORS =====

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "eraf.security.cors.enabled", havingValue = "true", matchIfMissing = true)
    public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();

        if (!properties.getAllowedOrigins().isEmpty()) {
            configuration.setAllowedOrigins(properties.getAllowedOrigins());
        }
        if (!properties.getAllowedOriginPatterns().isEmpty()) {
            configuration.setAllowedOriginPatterns(properties.getAllowedOriginPatterns());
        }

        configuration.setAllowedMethods(properties.getAllowedMethods());
        configuration.setAllowedHeaders(properties.getAllowedHeaders());
        configuration.setExposedHeaders(properties.getExposedHeaders());
        configuration.setAllowCredentials(properties.isAllowCredentials());
        configuration.setMaxAge(properties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(properties.getPathPattern(), configuration);
        return source;
    }

    // ===== Security Filter Chain (JWT) =====

    @Bean
    @ConditionalOnProperty(name = "eraf.security.jwt.enabled", havingValue = "true")
    public SecurityFilterChain jwtSecurityFilterChain(
            HttpSecurity http,
            ErafSecurityProperties properties,
            JwtProperties jwtProperties,
            JwtAuthenticationFilter jwtFilter,
            JwtAuthenticationEntryPoint entryPoint,
            JwtAccessDeniedHandler accessDeniedHandler,
            CorsConfigurationSource corsSource) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(properties.getPermitAllPatterns()).permitAll()
                        .requestMatchers(jwtProperties.getSkipPatterns()).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        if (properties.isDisableFrameOptions()) {
            http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        }

        return http.build();
    }

    // ===== Security Filter Chain (API Key Only) =====

    @Bean
    @ConditionalOnProperty(name = "eraf.security.api-key.enabled", havingValue = "true")
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain apiKeySecurityFilterChain(
            HttpSecurity http,
            ErafSecurityProperties properties,
            ApiKeyAuthenticationFilter apiKeyFilter,
            CorsConfigurationSource corsSource) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(properties.getPermitAllPatterns()).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class);

        if (properties.isDisableFrameOptions()) {
            http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        }

        return http.build();
    }

    // ===== Security Filter Chain (Default - Form Login) =====

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http,
            ErafSecurityProperties properties,
            CorsConfigurationSource corsSource) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsSource));

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

        if (properties.isFormLoginEnabled()) {
            http.formLogin(form -> form
                    .loginPage(properties.getLoginPage())
                    .defaultSuccessUrl(properties.getLoginSuccessUrl())
                    .permitAll()
            );

            http.logout(logout -> logout
                    .logoutUrl(properties.getLogoutUrl())
                    .logoutSuccessUrl(properties.getLogoutSuccessUrl())
                    .permitAll()
            );
        }

        return http.build();
    }
}
