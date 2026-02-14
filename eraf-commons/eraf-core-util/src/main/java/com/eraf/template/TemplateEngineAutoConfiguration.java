package com.eraf.util.template;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 템플릿 엔진 자동 구성
 */
@AutoConfiguration
@EnableConfigurationProperties(TemplateEngineProperties.class)
public class TemplateEngineAutoConfiguration {

    /**
     * Thymeleaf 템플릿 엔진 (Thymeleaf 클래스패스에 있을 때만)
     */
    @Bean
    @ConditionalOnClass(name = "org.thymeleaf.TemplateEngine")
    @ConditionalOnMissingBean(ThymeleafTemplateEngine.class)
    public ThymeleafTemplateEngine thymeleafTemplateEngine(TemplateEngineProperties properties) {
        return new ThymeleafTemplateEngine(
            properties.getThymeleaf().getPrefix(),
            properties.getThymeleaf().getSuffix()
        );
    }

    /**
     * Freemarker 템플릿 엔진 (Freemarker 클래스패스에 있을 때만)
     */
    @Bean
    @ConditionalOnClass(name = "freemarker.template.Configuration")
    @ConditionalOnMissingBean(FreemarkerTemplateEngine.class)
    public FreemarkerTemplateEngine freemarkerTemplateEngine(TemplateEngineProperties properties) {
        return new FreemarkerTemplateEngine(properties.getFreemarker().getTemplatePath());
    }
}
