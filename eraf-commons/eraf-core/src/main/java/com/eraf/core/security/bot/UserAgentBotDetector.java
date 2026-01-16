package com.eraf.core.security.bot;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * User-Agent 기반 봇 탐지기
 *
 * 알려진 봇 패턴:
 * - 검색 엔진 (Google, Bing, Yandex, Baidu 등)
 * - 소셜 미디어 (Facebook, Twitter, LinkedIn 등)
 * - 모니터링 (UptimeRobot, Pingdom 등)
 * - 자동화 도구 (curl, wget, Postman 등)
 * - 악성 봇 (Masscan, Nmap, SQLMap 등)
 *
 * 사용 예:
 * <pre>
 * BotDetector detector = new UserAgentBotDetector(
 *     Set.of("Postman", "cURL"),  // 허용할 봇 이름
 *     true                         // 알 수 없는 봇 차단
 * );
 *
 * BotDetectionResult result = detector.detect(request);
 * if (result.isBot() && !result.isAllowed()) {
 *     // 차단 처리
 * }
 * </pre>
 */
public class UserAgentBotDetector implements BotDetector {

    private static final Logger log = LoggerFactory.getLogger(UserAgentBotDetector.class);

    private final Set<String> allowedBotNames;
    private final boolean blockUnknownBots;

    // 알려진 봇 패턴
    private static final Map<Pattern, BotInfo> KNOWN_BOTS = new LinkedHashMap<>();

    static {
        // 검색 엔진 크롤러
        addBot("(?i)googlebot", "Googlebot", BotType.SEARCH_ENGINE_CRAWLER);
        addBot("(?i)bingbot", "Bingbot", BotType.SEARCH_ENGINE_CRAWLER);
        addBot("(?i)yandexbot", "YandexBot", BotType.SEARCH_ENGINE_CRAWLER);
        addBot("(?i)baiduspider", "Baiduspider", BotType.SEARCH_ENGINE_CRAWLER);
        addBot("(?i)duckduckbot", "DuckDuckBot", BotType.SEARCH_ENGINE_CRAWLER);
        addBot("(?i)slurp", "Yahoo Slurp", BotType.SEARCH_ENGINE_CRAWLER);
        addBot("(?i)naverbot", "NaverBot", BotType.SEARCH_ENGINE_CRAWLER);
        addBot("(?i)daumoa", "Daumoa", BotType.SEARCH_ENGINE_CRAWLER);

        // 소셜 미디어 봇
        addBot("(?i)facebookexternalhit", "Facebook", BotType.SOCIAL_MEDIA_BOT);
        addBot("(?i)twitterbot", "Twitter", BotType.SOCIAL_MEDIA_BOT);
        addBot("(?i)linkedinbot", "LinkedIn", BotType.SOCIAL_MEDIA_BOT);
        addBot("(?i)pinterest", "Pinterest", BotType.SOCIAL_MEDIA_BOT);
        addBot("(?i)slackbot", "Slack", BotType.SOCIAL_MEDIA_BOT);
        addBot("(?i)telegrambot", "Telegram", BotType.SOCIAL_MEDIA_BOT);
        addBot("(?i)discordbot", "Discord", BotType.SOCIAL_MEDIA_BOT);
        addBot("(?i)kakaotalk", "KakaoTalk", BotType.SOCIAL_MEDIA_BOT);

        // 모니터링 봇
        addBot("(?i)uptimerobot", "UptimeRobot", BotType.MONITORING_BOT);
        addBot("(?i)pingdom", "Pingdom", BotType.MONITORING_BOT);
        addBot("(?i)statuscake", "StatusCake", BotType.MONITORING_BOT);
        addBot("(?i)newrelicpinger", "New Relic", BotType.MONITORING_BOT);
        addBot("(?i)site24x7", "Site24x7", BotType.MONITORING_BOT);

        // 피드 수집기
        addBot("(?i)feedfetcher", "FeedFetcher", BotType.FEED_FETCHER);
        addBot("(?i)feedly", "Feedly", BotType.FEED_FETCHER);

        // 자동화 도구
        addBot("(?i)curl/", "cURL", BotType.AUTOMATED_TOOL);
        addBot("(?i)wget/", "Wget", BotType.AUTOMATED_TOOL);
        addBot("(?i)python-requests", "Python Requests", BotType.AUTOMATED_TOOL);
        addBot("(?i)python-urllib", "Python urllib", BotType.AUTOMATED_TOOL);
        addBot("(?i)java/", "Java HttpClient", BotType.AUTOMATED_TOOL);
        addBot("(?i)apache-httpclient", "Apache HttpClient", BotType.AUTOMATED_TOOL);
        addBot("(?i)okhttp", "OkHttp", BotType.AUTOMATED_TOOL);
        addBot("(?i)postman", "Postman", BotType.AUTOMATED_TOOL);
        addBot("(?i)insomnia", "Insomnia", BotType.AUTOMATED_TOOL);
        addBot("(?i)axios", "Axios", BotType.AUTOMATED_TOOL);
        addBot("(?i)httpie", "HTTPie", BotType.AUTOMATED_TOOL);

        // 스크래퍼
        addBot("(?i)scrapy", "Scrapy", BotType.SCRAPER);
        addBot("(?i)selenium", "Selenium", BotType.SCRAPER);
        addBot("(?i)phantomjs", "PhantomJS", BotType.SCRAPER);
        addBot("(?i)headlesschrome", "Headless Chrome", BotType.SCRAPER);
        addBot("(?i)puppeteer", "Puppeteer", BotType.SCRAPER);
        addBot("(?i)playwright", "Playwright", BotType.SCRAPER);

        // 악성 봇
        addBot("(?i)masscan", "Masscan", BotType.MALICIOUS_BOT);
        addBot("(?i)nmap", "Nmap", BotType.MALICIOUS_BOT);
        addBot("(?i)nikto", "Nikto", BotType.MALICIOUS_BOT);
        addBot("(?i)sqlmap", "SQLMap", BotType.MALICIOUS_BOT);
        addBot("(?i)zgrab", "ZGrab", BotType.MALICIOUS_BOT);
        addBot("(?i)gobuster", "Gobuster", BotType.MALICIOUS_BOT);
    }

    // 일반적인 Bot 식별 패턴
    private static final List<Pattern> GENERIC_BOT_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)bot"),
            Pattern.compile("(?i)crawler"),
            Pattern.compile("(?i)spider"),
            Pattern.compile("(?i)scraper"),
            Pattern.compile("(?i)fetcher")
    );

    private static void addBot(String regex, String name, BotType type) {
        KNOWN_BOTS.put(Pattern.compile(regex), new BotInfo(name, type));
    }

    /**
     * 기본 설정으로 생성 (알 수 없는 봇 허용)
     */
    public UserAgentBotDetector() {
        this(Collections.emptySet(), false);
    }

    /**
     * @param allowedBotNames 허용할 봇 이름 목록
     * @param blockUnknownBots 알 수 없는 봇 차단 여부
     */
    public UserAgentBotDetector(Set<String> allowedBotNames, boolean blockUnknownBots) {
        this.allowedBotNames = allowedBotNames != null ? allowedBotNames : Collections.emptySet();
        this.blockUnknownBots = blockUnknownBots;
    }

    @Override
    public BotDetectionResult detect(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        if (userAgent == null || userAgent.isEmpty()) {
            if (blockUnknownBots) {
                return BotDetectionResult.blockedBot(
                        BotType.UNKNOWN,
                        "No User-Agent",
                        0.9,
                        DetectionMethod.USER_AGENT
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
                            DetectionMethod.USER_AGENT
                    );
                } else {
                    return BotDetectionResult.blockedBot(
                            botInfo.type,
                            botInfo.name,
                            0.95,
                            DetectionMethod.USER_AGENT
                    );
                }
            }
        }

        // 일반적인 Bot 패턴 검사
        for (Pattern pattern : GENERIC_BOT_PATTERNS) {
            if (pattern.matcher(userAgent).find()) {
                if (blockUnknownBots) {
                    return BotDetectionResult.blockedBot(
                            BotType.UNKNOWN,
                            "Unknown Bot",
                            0.7,
                            DetectionMethod.USER_AGENT
                    );
                }
                return BotDetectionResult.allowedBot(
                        BotType.UNKNOWN,
                        "Unknown Bot",
                        0.7,
                        DetectionMethod.USER_AGENT
                );
            }
        }

        return BotDetectionResult.notBot();
    }

    private boolean isAllowed(BotInfo botInfo) {
        // 악성 봇은 항상 차단
        if (botInfo.type == BotType.MALICIOUS_BOT) {
            return false;
        }

        // 검색 엔진, 소셜 미디어, 모니터링 봇은 기본적으로 허용
        if (botInfo.type == BotType.SEARCH_ENGINE_CRAWLER ||
                botInfo.type == BotType.SOCIAL_MEDIA_BOT ||
                botInfo.type == BotType.MONITORING_BOT) {
            return true;
        }

        // 허용 목록에 있는지 확인
        if (!allowedBotNames.isEmpty()) {
            return allowedBotNames.stream()
                    .anyMatch(name -> name.equalsIgnoreCase(botInfo.name));
        }

        // 기본적으로 허용
        return !blockUnknownBots;
    }

    private static class BotInfo {
        final String name;
        final BotType type;

        BotInfo(String name, BotType type) {
            this.name = name;
            this.type = type;
        }
    }
}
