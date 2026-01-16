package com.eraf.gateway.bot;

import com.eraf.core.utils.PathMatcher;
import com.eraf.gateway.exception.GatewayErrorCode;
import com.eraf.gateway.util.GatewayResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Bot 탐지 필터
 */
@Slf4j
@RequiredArgsConstructor
public class BotDetectionFilter extends OncePerRequestFilter {

    public static final String BOT_DETECTION_RESULT_ATTRIBUTE = "eraf.gateway.botDetectionResult";

    private final BotDetector botDetector;
    private final List<String> excludePatterns;
    private final boolean blockBots;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (shouldExclude(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        BotDetectionResult result = botDetector.detect(request);

        // 결과를 request attribute에 저장 (다른 필터/컨트롤러에서 사용 가능)
        request.setAttribute(BOT_DETECTION_RESULT_ATTRIBUTE, result);

        if (result.isBot()) {
            log.debug("Bot detected: type={}, name={}, confidence={}, allowed={}",
                    result.getBotType(), result.getBotName(), result.getConfidence(), result.isAllowed());

            // 차단 모드이고 허용되지 않은 봇인 경우
            if (blockBots && !result.isAllowed()) {
                log.warn("Blocking bot request: {} - {} from {}",
                        result.getBotName(), result.getBotType(), request.getRemoteAddr());

                response.setHeader("X-Bot-Type", result.getBotType() != null ? result.getBotType().name() : "UNKNOWN");
                response.setHeader("X-Bot-Name", result.getBotName() != null ? result.getBotName() : "Unknown");

                GatewayResponseUtils.sendError(response, GatewayErrorCode.BOT_BLOCKED);
                return;
            }

            // Bot 정보를 헤더에 추가 (디버깅/로깅용)
            response.setHeader("X-Bot-Detected", "true");
            response.setHeader("X-Bot-Type", result.getBotType() != null ? result.getBotType().name() : "UNKNOWN");
            response.setHeader("X-Bot-Name", result.getBotName() != null ? result.getBotName() : "Unknown");
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldExclude(String path) {
        return PathMatcher.matchesAny(path, excludePatterns);
    }
}
