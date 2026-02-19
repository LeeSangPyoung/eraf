package com.eraf.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.LocaleResolver;

/**
 * ERAF I18n (Internationalization) Auto-Configuration
 *
 * <p>다국어 메시지 처리, 로케일 결정, 메시지 번역 Aspect를 자동 구성합니다.</p>
 *
 * <p>활성화 조건:</p>
 * <ul>
 *   <li>MessageSource가 빈으로 등록되어 있을 때</li>
 *   <li>Web 애플리케이션일 때 (LocaleResolver)</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // MessageService 사용
 * {@code @Autowired}
 * private MessageService messageService;
 *
 * String message = messageService.get("user.welcome", username);
 *
 * // @Message 어노테이션 사용
 * {@code @Message}("error.not.found")
 * public ApiResponse<?> getUserById(Long id) {
 *     // ...
 * }
 * </pre>
 */
@AutoConfiguration
@ConditionalOnClass(MessageSource.class)
@EnableAspectJAutoProxy
public class ErafI18nAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ErafI18nAutoConfiguration.class);

    /**
     * MessageService 빈 등록
     */
    @Bean
    @ConditionalOnMissingBean
    public MessageService messageService(MessageSource messageSource) {
        log.info("Registering MessageService for internationalization support");
        return new MessageService(messageSource);
    }

    /**
     * MessageAspect 빈 등록 (Aspect가 classpath에 있을 때만)
     */
    @Bean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    @ConditionalOnMissingBean
    public MessageAspect messageAspect(MessageSource messageSource) {
        log.info("Registering MessageAspect - @Message annotation support enabled");
        return new MessageAspect(messageSource);
    }

    /**
     * ErafLocaleResolver 빈 등록 (Web 애플리케이션일 때만)
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnMissingBean(LocaleResolver.class)
    public LocaleResolver localeResolver() {
        log.info("Registering ErafLocaleResolver for locale resolution");
        return new ErafLocaleResolver();
    }
}
