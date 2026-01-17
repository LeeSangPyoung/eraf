package com.eraf.gateway.bot.detector;

import com.eraf.gateway.bot.domain.BotDetectionResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;

/**
 * User-Agent 기반 Bot 탐지기
 */
@Slf4j
public class UserAgentBotDetector implements BotDetector {

    private final Set<String> allowedBotNames;
    private final boolean blockUnknownBots;

    // 알려진 검색 엔진 봇 패턴
    private static final Map<Pattern, BotInfo> KNOWN_BOTS = new LinkedHashMap<>();

    static {
        // 검색 엔진 크롤러
        KNOWN_BOTS.put(Pattern.compile("(?i)googlebot"),
                new BotInfo("Googlebot", BotDetectionResult.BotType.SEARCH_ENGINE_CRAWLER));
        KNOWN_BOTS.put(Pattern.compile("(?i)bingbot"),
                new BotInfo("Bingbot", BotDetectionResult.BotType.SEARCH_ENGINE_CRAWLER));
        KNOWN_BOTS.put(Pattern.compile("(?i)yandexbot"),
                new BotInfo("YandexBot", BotDetectionResult.BotType.SEARCH_ENGINE_CRAWLER));
        KNOWN_BOTS.put(Pattern.compile("(?i)baiduspider"),
                new BotInfo("Baiduspider", BotDetectionResult.BotType.SEARCH_ENGINE_CRAWLER));
        KNOWN_BOTS.put(Pattern.compile("(?i)duckduckbot"),
                new BotInfo("DuckDuckBot", BotDetectionResult.BotType.SEARCH_ENGINE_CRAWLER));
        KNOWN_BOTS.put(Pattern.compile("(?i)slurp"),
                new BotInfo("Yahoo Slurp", BotDetectionResult.BotType.SEARCH_ENGINE_CRAWLER));

        // 소셜 미디어 봇
        KNOWN_BOTS.put(Pattern.compile("(?i)facebookexternalhit"),
                new BotInfo("Facebook", BotDetectionResult.BotType.SOCIAL_MEDIA_BOT));
        KNOWN_BOTS.put(Pattern.compile("(?i)twitterbot"),
                new BotInfo("Twitter", BotDetectionResult.BotType.SOCIAL_MEDIA_BOT));
        KNOWN_BOTS.put(Pattern.compile("(?i)linkedinbot"),
                new BotInfo("LinkedIn", BotDetectionResult.BotType.SOCIAL_MEDIA_BOT));
        KNOWN_BOTS.put(Pattern.compile("(?i)pinterest"),
                new BotInfo("Pinterest", BotDetectionResult.BotType.SOCIAL_MEDIA_BOT));
        KNOWN_BOTS.put(Pattern.compile("(?i)slackbot"),
                new BotInfo("Slack", BotDetectionResult.BotType.SOCIAL_MEDIA_BOT));
        KNOWN_BOTS.put(Pattern.compile("(?i)telegrambot"),
                new BotInfo("Telegram", BotDetectionResult.BotType.SOCIAL_MEDIA_BOT));
        KNOWN_BOTS.put(Pattern.compile("(?i)discordbot"),
                new BotInfo("Discord", BotDetectionResult.BotType.SOCIAL_MEDIA_BOT));

        // 모니터링 봇
        KNOWN_BOTS.put(Pattern.compile("(?i)uptimerobot"),
                new BotInfo("UptimeRobot", BotDetectionResult.BotType.MONITORING_BOT));
        KNOWN_BOTS.put(Pattern.compile("(?i)pingdom"),
                new BotInfo("Pingdom", BotDetectionResult.BotType.MONITORING_BOT));
        KNOWN_BOTS.put(Pattern.compile("(?i)statuscake"),
                new BotInfo("StatusCake", BotDetectionResult.BotType.MONITORING_BOT));
        KNOWN_BOTS.put(Pattern.compile("(?i)newrelicpinger"),
                new BotInfo("New Relic", BotDetectionResult.BotType.MONITORING_BOT));

        // 피드 수집기
        KNOWN_BOTS.put(Pattern.compile("(?i)feedfetcher"),
                new BotInfo("FeedFetcher", BotDetectionResult.BotType.FEED_FETCHER));
        KNOWN_BOTS.put(Pattern.compile("(?i)feedly"),
                new BotInfo("Feedly", BotDetectionResult.BotType.FEED_FETCHER));

        // 자동화 도구
        KNOWN_BOTS.put(Pattern.compile("(?i)curl/"),
                new BotInfo("cURL", BotDetectionResult.BotType.AUTOMATED_TOOL));
        KNOWN_BOTS.put(Pattern.compile("(?i)wget/"),
                new BotInfo("Wget", BotDetectionResult.BotType.AUTOMATED_TOOL));
        KNOWN_BOTS.put(Pattern.compile("(?i)python-requests"),
                new BotInfo("Python Requests", BotDetectionResult.BotType.AUTOMATED_TOOL));
        KNOWN_BOTS.put(Pattern.compile("(?i)python-urllib"),
                new BotInfo("Python urllib", BotDetectionResult.BotType.AUTOMATED_TOOL));
        KNOWN_BOTS.put(Pattern.compile("(?i)java/"),
                new BotInfo("Java HttpClient", BotDetectionResult.BotType.AUTOMATED_TOOL));
        KNOWN_BOTS.put(Pattern.compile("(?i)apache-httpclient"),
                new BotInfo("Apache HttpClient", BotDetectionResult.BotType.AUTOMATED_TOOL));
        KNOWN_BOTS.put(Pattern.compile("(?i)okhttp"),
                new BotInfo("OkHttp", BotDetectionResult.BotType.AUTOMATED_TOOL));
        KNOWN_BOTS.put(Pattern.compile("(?i)postman"),
                new BotInfo("Postman", BotDetectionResult.BotType.AUTOMATED_TOOL));
        KNOWN_BOTS.put(Pattern.compile("(?i)insomnia"),
                new BotInfo("Insomnia", BotDetectionResult.BotType.AUTOMATED_TOOL));

        // 스크래퍼
        KNOWN_BOTS.put(Pattern.compile("(?i)scrapy"),
                new BotInfo("Scrapy", BotDetectionResult.BotType.SCRAPER));
        KNOWN_BOTS.put(Pattern.compile("(?i)selenium"),
                new BotInfo("Selenium", BotDetectionResult.BotType.SCRAPER));
        KNOWN_BOTS.put(Pattern.compile("(?i)phantomjs"),
                new BotInfo("PhantomJS", BotDetectionResult.BotType.SCRAPER));
        KNOWN_BOTS.put(Pattern.compile("(?i)headlesschrome"),
                new BotInfo("Headless Chrome", BotDetectionResult.BotType.SCRAPER));
        KNOWN_BOTS.put(Pattern.compile("(?i)puppeteer"),
                new BotInfo("Puppeteer", BotDetectionResult.BotType.SCRAPER));

        // 악성 봇
        KNOWN_BOTS.put(Pattern.compile("(?i)masscan"),
                new BotInfo("Masscan", BotDetectionResult.BotType.MALICIOUS_BOT));
        KNOWN_BOTS.put(Pattern.compile("(?i)nmap"),
                new BotInfo("Nmap", BotDetectionResult.BotType.MALICIOUS_BOT));
        KNOWN_BOTS.put(Pattern.compile("(?i)nikto"),
                new BotInfo("Nikto", BotDetectionResult.BotType.MALICIOUS_BOT));
        KNOWN_BOTS.put(Pattern.compile("(?i)sqlmap"),
                new BotInfo("SQLMap", BotDetectionResult.BotType.MALICIOUS_BOT));
    }

    // 일반적인 Bot 식별 패턴
    private static final List<Pattern> GENERIC_BOT_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)bot"),
            Pattern.compile("(?i)crawler"),
            Pattern.compile("(?i)spider"),
            Pattern.compile("(?i)scraper"),
            Pattern.compile("(?i)fetcher")
    );

    public UserAgentBotDetector(Set<String> allowedBotNames, boolean blockUnknownBots) {
        this.allowedBotNames = allowedBotNames != null ? allowedBotNames : Collections.emptySet();
        this.blockUnknownBots = blockUnknownBots;
    }

    @Override
    public BotDetectionResult detect(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        if (userAgent == null || userAgent.isEmpty()) {
            // User-Agent가 없으면 의심스러운 요청
            if (blockUnknownBots) {
                return BotDetectionResult.blockedBot(
                        BotDetectionResult.BotType.UNKNOWN,
                        "No User-Agent",
                        0.9,
                        BotDetectionResult.DetectionMethod.USER_AGENT
                );
            }
            return BotDetectionResult.notBot();
        }

        // 알려진 봇 검사
        for (Map.Entry<Pattern, BotInfo> entry : KNOWN_BOTS.entrySet()) {
            if (entry.getKey().matcher(userAgent).find()) {
                BotInfo botInfo = entry.getValue();
                boolean allowed = isAllowed(botInfo);

                if (allowed) {
                    return BotDetectionResult.allowedBot(
                            botInfo.type,
                            botInfo.name,
                            0.95,
                            BotDetectionResult.DetectionMethod.USER_AGENT
                    );
                } else {
                    return BotDetectionResult.blockedBot(
                            botInfo.type,
                            botInfo.name,
                            0.95,
                            BotDetectionResult.DetectionMethod.USER_AGENT
                    );
                }
            }
        }

        // 일반적인 Bot 패턴 검사
        for (Pattern pattern : GENERIC_BOT_PATTERNS) {
            if (pattern.matcher(userAgent).find()) {
                if (blockUnknownBots) {
                    return BotDetectionResult.blockedBot(
                            BotDetectionResult.BotType.UNKNOWN,
                            "Unknown Bot",
                            0.7,
                            BotDetectionResult.DetectionMethod.USER_AGENT
                    );
                }
                return BotDetectionResult.allowedBot(
                        BotDetectionResult.BotType.UNKNOWN,
                        "Unknown Bot",
                        0.7,
                        BotDetectionResult.DetectionMethod.USER_AGENT
                );
            }
        }

        return BotDetectionResult.notBot();
    }

    private boolean isAllowed(BotInfo botInfo) {
        // 악성 봇은 항상 차단
        if (botInfo.type == BotDetectionResult.BotType.MALICIOUS_BOT) {
            return false;
        }

        // 검색 엔진, 소셜 미디어, 모니터링 봇은 기본적으로 허용
        if (botInfo.type == BotDetectionResult.BotType.SEARCH_ENGINE_CRAWLER ||
                botInfo.type == BotDetectionResult.BotType.SOCIAL_MEDIA_BOT ||
                botInfo.type == BotDetectionResult.BotType.MONITORING_BOT) {
            return true;
        }

        // 허용 목록에 있는지 확인
        if (allowedBotNames != null && !allowedBotNames.isEmpty()) {
            return allowedBotNames.stream()
                    .anyMatch(name -> name.equalsIgnoreCase(botInfo.name));
        }

        // 기본적으로 허용
        return !blockUnknownBots;
    }

    private static class BotInfo {
        final String name;
        final BotDetectionResult.BotType type;

        BotInfo(String name, BotDetectionResult.BotType type) {
            this.name = name;
            this.type = type;
        }
    }
}
