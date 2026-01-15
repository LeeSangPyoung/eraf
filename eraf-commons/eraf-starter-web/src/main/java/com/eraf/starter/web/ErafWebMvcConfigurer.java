package com.eraf.starter.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ERAF Web MVC Configurer
 */
public class ErafWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    private ErafWebProperties properties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (properties.isCorsEnabled()) {
            registry.addMapping("/**")
                    .allowedOrigins(properties.getCorsAllowedOrigins())
                    .allowedMethods(properties.getCorsAllowedMethods())
                    .allowedHeaders(properties.getCorsAllowedHeaders())
                    .maxAge(properties.getCorsMaxAge());
        }
    }
}
