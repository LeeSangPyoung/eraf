package com.eraf.util.template;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 템플릿 엔진 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "eraf.template")
public class TemplateEngineProperties {

    private ThymeleafProperties thymeleaf = new ThymeleafProperties();
    private FreemarkerProperties freemarker = new FreemarkerProperties();

    public ThymeleafProperties getThymeleaf() {
        return thymeleaf;
    }

    public void setThymeleaf(ThymeleafProperties thymeleaf) {
        this.thymeleaf = thymeleaf;
    }

    public FreemarkerProperties getFreemarker() {
        return freemarker;
    }

    public void setFreemarker(FreemarkerProperties freemarker) {
        this.freemarker = freemarker;
    }

    public static class ThymeleafProperties {
        private String prefix = "classpath:/templates/";
        private String suffix = ".html";

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }
    }

    public static class FreemarkerProperties {
        private String templatePath = "/templates";

        public String getTemplatePath() {
            return templatePath;
        }

        public void setTemplatePath(String templatePath) {
            this.templatePath = templatePath;
        }
    }
}
