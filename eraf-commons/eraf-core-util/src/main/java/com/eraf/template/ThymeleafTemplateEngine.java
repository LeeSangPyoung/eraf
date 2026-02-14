package com.eraf.util.template;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Locale;
import java.util.Map;

/**
 * Thymeleaf 기반 템플릿 엔진 구현체
 */
public class ThymeleafTemplateEngine implements AdvancedTemplateEngine {

    private final TemplateEngine templateEngine;
    private final String prefix;
    private final String suffix;

    /**
     * 기본 생성자 (classpath:/templates/)
     */
    public ThymeleafTemplateEngine() {
        this("classpath:/templates/", ".html");
    }

    /**
     * 커스텀 경로 생성자
     *
     * @param prefix 템플릿 경로 접두사
     * @param suffix 템플릿 파일 확장자
     */
    public ThymeleafTemplateEngine(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.templateEngine = createTemplateEngine();
    }

    private TemplateEngine createTemplateEngine() {
        TemplateEngine engine = new TemplateEngine();

        // ClassPath 기반 Resolver
        ClassLoaderTemplateResolver fileResolver = new ClassLoaderTemplateResolver();
        fileResolver.setPrefix(prefix.replace("classpath:", ""));
        fileResolver.setSuffix(suffix);
        fileResolver.setTemplateMode(TemplateMode.HTML);
        fileResolver.setCharacterEncoding("UTF-8");
        fileResolver.setCacheable(true);
        fileResolver.setOrder(1);

        // String 기반 Resolver (인라인용)
        StringTemplateResolver stringResolver = new StringTemplateResolver();
        stringResolver.setOrder(2);

        engine.addTemplateResolver(fileResolver);
        engine.addTemplateResolver(stringResolver);

        return engine;
    }

    @Override
    public String render(String templateName, Map<String, Object> variables) {
        Context context = new Context(Locale.getDefault(), variables);
        return templateEngine.process(templateName, context);
    }

    @Override
    public String renderInline(String templateContent, Map<String, Object> variables) {
        Context context = new Context(Locale.getDefault(), variables);
        return templateEngine.process(templateContent, context);
    }

    @Override
    public String getEngineName() {
        return "Thymeleaf";
    }

    /**
     * Locale 지정 렌더링
     */
    public String render(String templateName, Map<String, Object> variables, Locale locale) {
        Context context = new Context(locale, variables);
        return templateEngine.process(templateName, context);
    }
}
