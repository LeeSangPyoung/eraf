package com.eraf.gateway.validation.config;

import com.eraf.gateway.common.filter.FilterOrder;
import com.eraf.gateway.validation.filter.ValidationFilter;
import com.eraf.gateway.validation.repository.ValidationRuleRepository;
import com.eraf.gateway.validation.service.ValidationService;
import com.eraf.gateway.validation.validator.ContentTypeValidator;
import com.eraf.gateway.validation.validator.JsonSchemaValidator;
import com.eraf.gateway.validation.validator.OpenApiValidator;
import com.eraf.gateway.validation.validator.RequestSizeValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Validation Auto Configuration
 * 검증 자동 설정
 */
@Slf4j
@Configuration
@ConditionalOnClass(ValidationFilter.class)
@ConditionalOnProperty(prefix = "eraf.gateway.validation", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ValidationProperties.class)
public class ValidationAutoConfiguration {

    /**
     * ObjectMapper 빈 (JSON 처리용)
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * JSON Schema Validator
     */
    @Bean
    @ConditionalOnMissingBean
    public JsonSchemaValidator jsonSchemaValidator(ObjectMapper objectMapper) {
        log.info("Initializing JsonSchemaValidator");
        return new JsonSchemaValidator(objectMapper);
    }

    /**
     * OpenAPI Validator
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenApiValidator openApiValidator() {
        log.info("Initializing OpenApiValidator");
        return new OpenApiValidator();
    }

    /**
     * Request Size Validator
     */
    @Bean
    @ConditionalOnMissingBean
    public RequestSizeValidator requestSizeValidator() {
        log.info("Initializing RequestSizeValidator");
        return new RequestSizeValidator();
    }

    /**
     * Content-Type Validator
     */
    @Bean
    @ConditionalOnMissingBean
    public ContentTypeValidator contentTypeValidator() {
        log.info("Initializing ContentTypeValidator");
        return new ContentTypeValidator();
    }

    /**
     * Validation Rule Repository
     */
    @Bean
    @ConditionalOnMissingBean
    public ValidationRuleRepository validationRuleRepository() {
        log.info("Initializing ValidationRuleRepository");
        return new ValidationRuleRepository();
    }

    /**
     * Validation Service
     */
    @Bean
    @ConditionalOnMissingBean
    public ValidationService validationService(
            JsonSchemaValidator jsonSchemaValidator,
            OpenApiValidator openApiValidator,
            RequestSizeValidator requestSizeValidator,
            ContentTypeValidator contentTypeValidator) {
        log.info("Initializing ValidationService");
        return new ValidationService(
                jsonSchemaValidator,
                openApiValidator,
                requestSizeValidator,
                contentTypeValidator
        );
    }

    /**
     * Validation Filter
     */
    @Bean
    public FilterRegistrationBean<ValidationFilter> validationFilterRegistration(
            ValidationService validationService,
            ValidationRuleRepository ruleRepository,
            ValidationProperties properties) {

        log.info("Registering ValidationFilter with order: {}", FilterOrder.VALIDATION);

        ValidationFilter filter = new ValidationFilter(
                validationService,
                ruleRepository,
                properties.isEnabled()
        );
        filter.setExcludePatterns(properties.getExcludePatterns());

        FilterRegistrationBean<ValidationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(FilterOrder.VALIDATION);
        registration.addUrlPatterns("/*");

        return registration;
    }
}
