package com.eraf.util.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Freemarker 기반 템플릿 엔진 구현체
 */
public class FreemarkerTemplateEngine implements AdvancedTemplateEngine {

    private final Configuration configuration;

    /**
     * 기본 생성자 (classpath:/templates/)
     */
    public FreemarkerTemplateEngine() {
        this("/templates");
    }

    /**
     * 커스텀 경로 생성자
     *
     * @param templatePath 템플릿 경로
     */
    public FreemarkerTemplateEngine(String templatePath) {
        this.configuration = createConfiguration(templatePath);
    }

    private Configuration createConfiguration(String templatePath) {
        Configuration config = new Configuration(Configuration.VERSION_2_3_32);
        config.setClassForTemplateLoading(this.getClass(), templatePath);
        config.setDefaultEncoding("UTF-8");
        config.setNumberFormat("0.######");
        return config;
    }

    @Override
    public String render(String templateName, Map<String, Object> variables) {
        try {
            Template template = configuration.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(variables, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new TemplateRenderException("Failed to render template: " + templateName, e);
        }
    }

    @Override
    public String renderInline(String templateContent, Map<String, Object> variables) {
        try {
            Template template = new Template(
                "inline",
                new StringReader(templateContent),
                configuration
            );
            StringWriter writer = new StringWriter();
            template.process(variables, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new TemplateRenderException("Failed to render inline template", e);
        }
    }

    @Override
    public String getEngineName() {
        return "Freemarker";
    }

    /**
     * 템플릿 렌더링 예외
     */
    public static class TemplateRenderException extends RuntimeException {
        public TemplateRenderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
