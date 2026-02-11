package com.eraf.openapi.features.filter.impl;

import com.eraf.core.logging.AuditLogger;
import com.eraf.openapi.core.domain.GatewayPlugin;
import com.eraf.openapi.core.exception.GatewayErrorCode;
import com.eraf.openapi.features.filter.PluginGatewayFilter;
import com.eraf.openapi.features.util.GatewayResponseHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Bot Detection Gateway Filter
 * User-Agent 기반 봇 감지 및 차단
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BotDetectionGatewayFilter implements PluginGatewayFilter, Ordered {

    // 일반적인 봇 패턴
    private static final List<String> DEFAULT_BOT_PATTERNS = List.of(
            "(?i).*bot.*",
            "(?i).*crawler.*",
            "(?i).*spider.*",
            "(?i).*scraper.*",
            "(?i).*curl.*",
            "(?i).*wget.*",
            "(?i).*python.*",
            "(?i).*java.*httpclient.*"
    );

    // 허용할 좋은 봇들 (Google, Bing 등)
    private static final List<String> GOOD_BOT_PATTERNS = List.of(
            "(?i).*googlebot.*",
            "(?i).*bingbot.*",
            "(?i).*slackbot.*",
            "(?i).*facebookexternalhit.*"
    );

    @Override
    public String getPluginName() {
        return "bot-detection";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 150;
    }

    @Override
    public GatewayFilter createFilter(GatewayPlugin plugin) {
        Map<String, Object> config = plugin.getConfig();

        // Bot Detection 설정 추출
        String action = (String) config.getOrDefault("action", "block"); // block or allow
        boolean allowGoodBots = (boolean) config.getOrDefault("allow_good_bots", true);
        List<String> customBotPatterns = (List<String>) config.getOrDefault("custom_patterns", List.of());

        return (exchange, chain) -> applyBotDetection(exchange, chain, action, allowGoodBots, customBotPatterns);
    }

    private Mono<Void> applyBotDetection(ServerWebExchange exchange, GatewayFilterChain chain,
                                           String action, boolean allowGoodBots, List<String> customPatterns) {
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");

        if (userAgent == null || userAgent.isEmpty()) {
            log.warn("Request with empty User-Agent");
            if ("block".equals(action)) {
                AuditLogger.log(AuditLogger.AuditEvent.builder()
                        .action("BOT_BLOCKED")
                        .resource(exchange.getRequest().getMethod() + " " + exchange.getRequest().getPath().value())
                        .failure()
                        .detail("reason", "empty User-Agent")
                        .build());
                return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.IP_RESTRICTED, "Bot detected: empty User-Agent");
            }
        }

        // Good Bot 체크
        if (allowGoodBots && isGoodBot(userAgent)) {
            log.debug("Good bot detected: {}", userAgent);
            return chain.filter(exchange);
        }

        // Bot 패턴 매칭
        boolean isBot = isBotUserAgent(userAgent, customPatterns);

        if (isBot) {
            log.warn("Bot detected: {}", userAgent);

            if ("block".equals(action)) {
                AuditLogger.log(AuditLogger.AuditEvent.builder()
                        .action("BOT_BLOCKED")
                        .resource(exchange.getRequest().getMethod() + " " + exchange.getRequest().getPath().value())
                        .failure()
                        .detail("user_agent", userAgent)
                        .build());
                exchange.getResponse().getHeaders().add("X-Bot-Detected", "true");
                return GatewayResponseHelper.sendError(exchange, GatewayErrorCode.IP_RESTRICTED, "Bot detected");
            } else {
                // allow 모드에서는 헤더만 추가하고 통과
                exchange.getRequest().mutate()
                        .header("X-Bot-Detected", "true")
                        .build();
            }
        }

        return chain.filter(exchange);
    }

    private boolean isGoodBot(String userAgent) {
        return GOOD_BOT_PATTERNS.stream()
                .anyMatch(pattern -> Pattern.matches(pattern, userAgent));
    }

    private boolean isBotUserAgent(String userAgent, List<String> customPatterns) {
        // 기본 봇 패턴 체크
        boolean matchesDefault = DEFAULT_BOT_PATTERNS.stream()
                .anyMatch(pattern -> Pattern.matches(pattern, userAgent));

        if (matchesDefault) {
            return true;
        }

        // 커스텀 패턴 체크
        return customPatterns.stream()
                .anyMatch(pattern -> Pattern.matches(pattern, userAgent));
    }
}
