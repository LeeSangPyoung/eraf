package com.eraf.swagger;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        if (security.isEnabled()) {
            Components components = new Components();

            // JWT Bearer 인증 스키마 추가
            SecurityRequirement securityRequirement = new SecurityRequirement();
            securityRequirement.addList(security.getSchemeName());
            components.addSecuritySchemes(security.getSchemeName(),
                    new SecurityScheme()
                            .name(security.getSchemeName())
                            .type(SecurityScheme.Type.HTTP)
                            .scheme(security.getScheme())
                            .bearerFormat(security.getBearerFormat()));

            // API Key 보안 스키마 추가
            ErafSwaggerProperties.ApiKey apiKey = security.getApiKey();
            if (apiKey.isEnabled()) {
                String apiKeySchemeName = apiKey.getName();
                SecurityScheme.In location = "query".equalsIgnoreCase(apiKey.getIn())
                        ? SecurityScheme.In.QUERY
                        : SecurityScheme.In.HEADER;
                components.addSecuritySchemes(apiKeySchemeName,
                        new SecurityScheme()
                                .name(apiKeySchemeName)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(location));
                securityRequirement.addList(apiKeySchemeName);
            }

            openAPI.addSecurityItem(securityRequirement)
                    .components(components);
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
        return buildGroupedOpenApi(group.getDefaultGroup(), group);
    }

    /**
     * 추가 API 그룹 설정
     * properties에 정의된 additional-groups를 GroupedOpenApi 빈으로 등록
     */
    @Bean
    @ConditionalOnMissingBean(name = "erafAdditionalGroupedOpenApis")
    public List<GroupedOpenApi> erafAdditionalGroupedOpenApis() {
        List<GroupedOpenApi> additionalGroups = new ArrayList<>();
        Map<String, ErafSwaggerProperties.Group> groups = properties.getAdditionalGroups();
        if (groups != null) {
            groups.forEach((groupName, groupConfig) -> {
                additionalGroups.add(buildGroupedOpenApi(groupName, groupConfig));
            });
        }
        return additionalGroups;
    }

    /**
     * GroupedOpenApi 빌드 헬퍼
     */
    private GroupedOpenApi buildGroupedOpenApi(String groupName, ErafSwaggerProperties.Group group) {
        GroupedOpenApi.Builder builder = GroupedOpenApi.builder()
                .group(groupName)
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
