# ERAF Gateway Feature - Bot Detection

API Gateway의 Bot Detection 기능을 제공하는 모듈입니다.

## 기능

- **User-Agent 기반 탐지**: User-Agent 헤더를 분석하여 Bot 식별
- **알려진 Bot 패턴**: 검색 엔진, 소셜 미디어, 모니터링 등 다양한 Bot 패턴 지원
- **선택적 차단**: 탐지만 하거나 특정 Bot 차단 가능
- **허용 목록**: 특정 Bot 이름을 허용 목록에 추가 가능
- **경로별 제외**: PathMatcher를 사용한 경로 패턴 제외
- **Bot 정보 헤더**: X-Bot-* 헤더로 Bot 정보 제공

## 포함 내용

### Domain
- `BotDetectionResult`: Bot 탐지 결과 (Bot 유형, 이름, 신뢰도, 허용 여부)

### Detector
- `BotDetector`: Bot 탐지 인터페이스
- `UserAgentBotDetector`: User-Agent 기반 Bot 탐지 구현

### Filter
- `BotDetectionFilter`: HTTP 요청 필터 (Order: HIGHEST + 5)

### Configuration
- `BotDetectionProperties`: 설정 클래스
- `BotDetectionAutoConfiguration`: Spring Boot 자동 설정

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-feature-bot-detection</artifactId>
</dependency>
```

## 설정 예시

```yaml
eraf:
  gateway:
    bot-detection:
      enabled: true
      block-bots: false  # true: 차단, false: 탐지만 (기본값)
      block-unknown-bots: false  # 알 수 없는 Bot 차단 여부
      allowed-bot-names:
        - Googlebot
        - Bingbot
        - Postman
      exclude-patterns:
        - /actuator/**
        - /health/**
        - /public/**
      add-bot-headers: true  # Bot 정보 헤더 추가 (디버깅용)
```

## Bot 유형

### SEARCH_ENGINE_CRAWLER
검색 엔진 크롤러 (기본 허용)
- Googlebot
- Bingbot
- YandexBot
- Baiduspider
- DuckDuckBot
- Yahoo Slurp

### SOCIAL_MEDIA_BOT
소셜 미디어 봇 (기본 허용)
- Facebook
- Twitter
- LinkedIn
- Pinterest
- Slack
- Telegram
- Discord

### MONITORING_BOT
모니터링 봇 (기본 허용)
- UptimeRobot
- Pingdom
- StatusCake
- New Relic

### FEED_FETCHER
피드 수집기
- FeedFetcher
- Feedly

### AUTOMATED_TOOL
자동화 도구
- cURL
- Wget
- Python Requests
- Java HttpClient
- Postman
- Insomnia

### SCRAPER
웹 스크래퍼
- Scrapy
- Selenium
- PhantomJS
- Puppeteer

### MALICIOUS_BOT
악성 봇 (항상 차단)
- Masscan
- Nmap
- Nikto
- SQLMap

### UNKNOWN
알 수 없는 Bot

## 탐지 방법

- **USER_AGENT**: User-Agent 헤더 분석
- **BEHAVIOR_PATTERN**: 행동 패턴 분석 (향후 지원)
- **REQUEST_FINGERPRINT**: 요청 핑거프린트 분석 (향후 지원)
- **IP_REPUTATION**: IP 평판 분석 (향후 지원)
- **RATE_PATTERN**: 요청 패턴 분석 (향후 지원)

## 응답 헤더

### Bot 탐지 시
```
X-Bot-Detected: true
X-Bot-Type: SEARCH_ENGINE_CRAWLER
X-Bot-Name: Googlebot
```

### Bot 차단 시 (403)
```
X-Bot-Type: MALICIOUS_BOT
X-Bot-Name: Nmap
```

## 차단 정책

### 1. 악성 봇
악성 봇(MALICIOUS_BOT)은 항상 차단됩니다.

### 2. 검색 엔진/소셜 미디어/모니터링 봇
기본적으로 허용됩니다.

### 3. 기타 Bot
- `block-unknown-bots: false` (기본값): 허용
- `block-unknown-bots: true`: 차단
- `allowed-bot-names`에 명시된 경우: 항상 허용

## Bot 정보 사용

컨트롤러에서 Bot 정보를 활용할 수 있습니다:

```java
@RestController
@RequestMapping("/api")
public class MyController {

    @GetMapping("/resource")
    public ResponseEntity<?> getResource(HttpServletRequest request) {
        // 필터에서 탐지된 Bot 정보 가져오기
        BotDetectionResult botResult = (BotDetectionResult)
            request.getAttribute(BotDetectionFilter.BOT_DETECTION_RESULT_ATTRIBUTE);

        if (botResult != null && botResult.isBot()) {
            // Bot인 경우 특별한 처리
            String botName = botResult.getBotName();
            BotDetectionResult.BotType botType = botResult.getBotType();

            if (botType == BotDetectionResult.BotType.SEARCH_ENGINE_CRAWLER) {
                // 검색 엔진에 최적화된 응답 반환
                return ResponseEntity.ok(getSeoOptimizedResponse());
            }
        }

        return ResponseEntity.ok(getNormalResponse());
    }
}
```

## 사용 시나리오

### 1. 탐지만 하고 모든 Bot 허용
```yaml
eraf:
  gateway:
    bot-detection:
      enabled: true
      block-bots: false  # 차단 안 함
```

### 2. 악성 Bot만 차단
```yaml
eraf:
  gateway:
    bot-detection:
      enabled: true
      block-bots: true  # 차단 모드
      block-unknown-bots: false  # 알 수 없는 Bot은 허용
```

### 3. 화이트리스트 방식 (허용된 Bot만 통과)
```yaml
eraf:
  gateway:
    bot-detection:
      enabled: true
      block-bots: true
      block-unknown-bots: true  # 알 수 없는 Bot 차단
      allowed-bot-names:
        - Googlebot
        - Bingbot
        - UptimeRobot
```

### 4. 개발 환경 (모든 Bot 허용)
```yaml
eraf:
  gateway:
    bot-detection:
      enabled: true
      block-bots: false
      allowed-bot-names:
        - Postman
        - Insomnia
```

## 사용 방법

1. 모듈 의존성 추가
2. 설정 파일에 bot-detection 설정 추가
3. 필요시 컨트롤러에서 Bot 정보 활용

## 빌드

```bash
mvn clean install
```

## 주의사항

- User-Agent는 쉽게 변조 가능하므로 완벽한 Bot 차단을 보장하지 않습니다
- 중요한 리소스는 API Key나 JWT와 같은 인증 방식과 함께 사용하세요
- Rate Limit과 함께 사용하면 Bot 트래픽 관리에 효과적입니다
- 검색 엔진 봇을 차단하면 SEO에 영향을 줄 수 있으니 주의하세요

## 향후 확장

- 행동 패턴 기반 탐지 (요청 빈도, 탐색 패턴 등)
- 브라우저 핑거프린트 분석
- IP 평판 데이터베이스 연동
- 머신러닝 기반 Bot 탐지
- Challenge-Response 메커니즘 (CAPTCHA 등)
