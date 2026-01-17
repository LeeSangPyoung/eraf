package com.eraf.gateway.bot.config;

import com.eraf.gateway.bot.detector.BotDetector;
import com.eraf.gateway.bot.detector.UserAgentBotDetector;
import com.eraf.gateway.bot.filter.BotDetectionFilter;
import com.eraf.gateway.common.filter.FilterOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bot Detection 자동 설정
 */
@Slf4j
@Configuration
@ConditionalOnClass(BotDetectionFilter.class)
@ConditionalOnProperty(prefix = "eraf.gateway.bot-detection", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(BotDetectionProperties.class)
public class BotDetectionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BotDetector botDetector(BotDetectionProperties properties) {
        log.info("Initializing UserAgentBotDetector with allowedBots: {}, blockUnknownBots: {}",
                properties.getAllowedBotNames(), properties.isBlockUnknownBots());
        return new UserAgentBotDetector(
                properties.getAllowedBotNames(),
                properties.isBlockUnknownBots()
        );
    }

    @Bean
    public FilterRegistrationBean<BotDetectionFilter> botDetectionFilterRegistration(
            BotDetector botDetector,
            BotDetectionProperties properties) {
        log.info("Registering BotDetectionFilter with order: {}", FilterOrder.BOT_DETECTION);

        BotDetectionFilter filter = new BotDetectionFilter(
                botDetector,
                properties.getExcludePatterns(),
                properties.isBlockBots()
        );

        FilterRegistrationBean<BotDetectionFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(FilterOrder.BOT_DETECTION);
        registration.addUrlPatterns("/*");

        return registration;
    }
}
