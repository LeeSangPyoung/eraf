package com.eraf.starter.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * ERAF Swagger Auto Configuration
 * SpringDoc OpenAPI 자동 설정
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "eraf.swagger.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ErafSwaggerProperties.class)
public class ErafSwaggerAutoConfiguration {

    private final ErafSwaggerProperties properties;

    public ErafSwaggerAutoConfiguration(ErafSwaggerProperties properties) {
        this.properties = properties;
    }

    /**
     * OpenAPI 설정
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenAPI erafOpenAPI() {
        ErafSwaggerProperties.ApiInfo apiInfo = properties.getApiInfo();
        ErafSwaggerProperties.Security security = properties.getSecurity();

        OpenAPI openAPI = new OpenAPI()
                .info(createInfo(apiInfo));

        // JWT 인증 스키마 추가
        if (security.isEnabled()) {
            openAPI.addSecurityItem(new SecurityRequirement().addList(security.getSchemeName()))
                    .components(new Components()
                            .addSecuritySchemes(security.getSchemeName(),
                                    new SecurityScheme()
                                            .name(security.getSchemeName())
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme(security.getScheme())
                                            .bearerFormat(security.getBearerFormat())));
        }

        return openAPI;
    }

    /**
     * 기본 API 그룹 설정
     */
    @Bean
    @ConditionalOnMissingBean
    public GroupedOpenApi erafGroupedOpenApi() {
        ErafSwaggerProperties.Group group = properties.getGroup();

        GroupedOpenApi.Builder builder = GroupedOpenApi.builder()
                .group(group.getDefaultGroup())
                .pathsToMatch(group.getPathsToMatch())
                .pathsToExclude(group.getPathsToExclude());

        if (group.getPackagesToScan() != null && group.getPackagesToScan().length > 0) {
            builder.packagesToScan(group.getPackagesToScan());
        }

        return builder.build();
    }

    /**
     * API 정보 생성
     */
    private Info createInfo(ErafSwaggerProperties.ApiInfo apiInfo) {
        Info info = new Info()
                .title(apiInfo.getTitle())
                .description(apiInfo.getDescription())
                .version(apiInfo.getVersion());

        if (apiInfo.getTermsOfService() != null && !apiInfo.getTermsOfService().isEmpty()) {
            info.termsOfService(apiInfo.getTermsOfService());
        }

        // Contact 정보
        ErafSwaggerProperties.Contact contactInfo = apiInfo.getContact();
        if (contactInfo != null && hasValue(contactInfo.getName(), contactInfo.getEmail(), contactInfo.getUrl())) {
            Contact contact = new Contact()
                    .name(contactInfo.getName())
                    .email(contactInfo.getEmail())
                    .url(contactInfo.getUrl());
            info.contact(contact);
        }

        // License 정보
        ErafSwaggerProperties.License licenseInfo = apiInfo.getLicense();
        if (licenseInfo != null && hasValue(licenseInfo.getName())) {
            License license = new License()
                    .name(licenseInfo.getName())
                    .url(licenseInfo.getUrl());
            info.license(license);
        }

        return info;
    }

    /**
     * 값이 하나라도 있는지 확인
     */
    private boolean hasValue(String... values) {
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
