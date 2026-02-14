# Slack/Teams Webhook 알림

Slack과 Microsoft Teams에 웹훅을 통해 실시간 알림을 전송하는 기능입니다.

## 주요 기능

- **Slack Webhook**: 텍스트, 리치 메시지, 블록 메시지 전송
- **Teams Webhook**: MessageCard 형식 메시지, 액션 버튼 지원
- **알림 레벨**: INFO, WARNING, ERROR (자동 색상 적용)
- **리치 메시지**: 제목, 색상, 필드, 버튼 등

## 설정

### 1. Slack 설정

```yaml
eraf:
  notification:
    webhook:
      slack:
        enabled: true
        webhook-url: ${SLACK_WEBHOOK_URL}  # Slack App에서 발급받은 Webhook URL
        default-channel: "#alerts"  # 옵션
```

### 2. Teams 설정

```yaml
eraf:
  notification:
    webhook:
      teams:
        enabled: true
        webhook-url: ${TEAMS_WEBHOOK_URL}  # Teams에서 발급받은 Webhook URL
```

## Webhook URL 생성

### Slack Webhook URL 생성

1. https://api.slack.com/apps 접속
2. **Create New App** 클릭
3. **From scratch** 선택
4. App 이름과 Workspace 선택
5. **Incoming Webhooks** 클릭
6. **Activate Incoming Webhooks** ON
7. **Add New Webhook to Workspace** 클릭
8. 채널 선택 (예: #alerts)
9. **Webhook URL** 복사

**형식:**
```
https://hooks.slack.com/services/{TEAM_ID}/{BOT_ID}/{TOKEN}
```

### Teams Webhook URL 생성

1. Teams 앱 열기
2. 알림을 받을 채널 선택
3. 채널 이름 옆 **...** 클릭
4. **Connectors** 선택
5. **Incoming Webhook** 찾기 → **Configure**
6. Webhook 이름 입력 (예: "App Alerts")
7. **Create** 클릭
8. **Webhook URL** 복사

**형식:**
```
https://outlook.office.com/webhook/{TENANT_ID}/IncomingWebhook/{WEBHOOK_PATH}/{GROUP_ID}
```

## 사용 방법

### 1. Slack - 텍스트 메시지

```java
@Service
public class AlertService {

    @Autowired
    private SlackWebhookSender slackSender;

    public void sendAlert(String message) {
        slackSender.sendText(message);
    }

    public void sendOrderAlert(Order order) {
        slackSender.sendText(
            String.format("새로운 주문: #%d (%s) - %,d원",
                order.getId(), order.getCustomerName(), order.getAmount())
        );
    }
}
```

**Slack 결과:**
```
새로운 주문: #12345 (홍길동) - 150,000원
```

### 2. Slack - 리치 메시지 (색상 + 필드)

```java
@Service
public class ErrorNotificationService {

    @Autowired
    private SlackWebhookSender slackSender;

    public void notifyError(Exception e) {
        Map<String, String> fields = new HashMap<>();
        fields.put("Exception", e.getClass().getSimpleName());
        fields.put("Message", e.getMessage());
        fields.put("Time", LocalDateTime.now().toString());

        slackSender.sendRich(
            ":x: Application Error",
            "An error occurred in the application",
            "danger",  // 빨간색
            fields
        );
    }
}
```

**Slack 결과:**
```
┌─────────────────────────────────┐
│ :x: Application Error           │  (빨간색 막대)
│ An error occurred in application│
│                                 │
│ Exception: NullPointerException │
│ Message: User not found         │
│ Time: 2025-01-15T10:30:00       │
└─────────────────────────────────┘
```

### 3. Slack - 알림 레벨 자동 색상

```java
@Service
public class MonitoringService {

    @Autowired
    private SlackWebhookSender slackSender;

    public void notifySystemStatus(String status, String message) {
        NotificationLevel level;

        if (status.equals("healthy")) {
            level = NotificationLevel.INFO;      // 초록색
        } else if (status.equals("degraded")) {
            level = NotificationLevel.WARNING;   // 주황색
        } else {
            level = NotificationLevel.ERROR;     // 빨간색
        }

        slackSender.sendWithLevel(level,
            "System Status: " + status,
            message);
    }
}
```

### 4. Slack - 에러 알림 (스택 트레이스)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private SlackWebhookSender slackSender;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        // Slack 알림
        slackSender.sendError("Unhandled Exception", e);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(e.getMessage()));
    }
}
```

**Slack 결과:**
```
┌─────────────────────────────────────┐
│ :x: Unhandled Exception             │  (빨간색)
│ User not found                      │
│                                     │
│ Exception: UserNotFoundException    │
│ Message: User not found             │
│ Location: UserService.java:45       │
└─────────────────────────────────────┘
```

### 5. Teams - 텍스트 메시지

```java
@Service
public class TeamsAlertService {

    @Autowired
    private TeamsWebhookSender teamsSender;

    public void sendDeploymentAlert(String version) {
        teamsSender.sendText(
            String.format("🚀 New version deployed: %s", version)
        );
    }
}
```

### 6. Teams - 리치 메시지

```java
@Service
public class TeamsNotificationService {

    @Autowired
    private TeamsWebhookSender teamsSender;

    public void notifyPaymentSuccess(Payment payment) {
        Map<String, String> fields = Map.of(
            "결제 ID", payment.getId().toString(),
            "고객", payment.getCustomerName(),
            "금액", String.format("%,d원", payment.getAmount()),
            "결제 수단", payment.getMethod()
        );

        teamsSender.sendRich(
            "💳 결제 완료",
            "결제가 성공적으로 완료되었습니다.",
            "good",  // 초록색
            fields
        );
    }
}
```

**Teams 결과:**
```
┌──────────────────────────────────┐
│ 💳 결제 완료                      │  (초록색 상단바)
│ 결제가 성공적으로 완료되었습니다.  │
│                                  │
│ 결제 ID:    12345                │
│ 고객:       홍길동                │
│ 금액:       150,000원             │
│ 결제 수단:   신용카드              │
└──────────────────────────────────┘
```

### 7. Teams - 액션 버튼

```java
@Service
public class ApprovalService {

    @Autowired
    private TeamsWebhookSender teamsSender;

    public void requestApproval(ApprovalRequest request) {
        Map<String, String> fields = Map.of(
            "요청자", request.getRequester(),
            "금액", String.format("%,d원", request.getAmount()),
            "사유", request.getReason()
        );

        List<TeamsWebhookSender.Action> actions = List.of(
            new TeamsWebhookSender.Action(
                "승인",
                "https://myapp.com/approvals/" + request.getId() + "/approve"
            ),
            new TeamsWebhookSender.Action(
                "거부",
                "https://myapp.com/approvals/" + request.getId() + "/reject"
            )
        );

        teamsSender.sendWithActions(
            "📋 승인 요청",
            "새로운 승인 요청이 도착했습니다.",
            "warning",  // 주황색
            fields,
            actions
        );
    }
}
```

**Teams 결과:**
```
┌──────────────────────────────────┐
│ 📋 승인 요청                      │  (주황색)
│ 새로운 승인 요청이 도착했습니다.  │
│                                  │
│ 요청자:  김철수                   │
│ 금액:    1,000,000원              │
│ 사유:    신규 서버 구매            │
│                                  │
│ [ 승인 ]  [ 거부 ]                │  (클릭 가능)
└──────────────────────────────────┘
```

### 8. 배치 작업 완료 알림

```java
@Component
public class BatchJobListener {

    @Autowired
    private SlackWebhookSender slackSender;

    @Autowired
    private TeamsWebhookSender teamsSender;

    public void onBatchComplete(BatchJob job) {
        String message = String.format(
            "배치 작업 완료\n" +
            "작업명: %s\n" +
            "처리 건수: %,d\n" +
            "실행 시간: %d초\n" +
            "상태: %s",
            job.getName(),
            job.getProcessedCount(),
            job.getDurationSeconds(),
            job.getStatus()
        );

        NotificationLevel level = job.getStatus().equals("SUCCESS")
                ? NotificationLevel.INFO
                : NotificationLevel.ERROR;

        // Slack과 Teams 동시 전송
        slackSender.sendWithLevel(level, "Batch Job Complete", message);
        teamsSender.sendWithLevel(level, "배치 작업 완료", message);
    }
}
```

## 실전 시나리오

### 시나리오 1: 서버 모니터링 알림

```java
@Component
@Scheduled(fixedRate = 60000)  // 1분마다
public class ServerHealthChecker {

    @Autowired
    private SlackWebhookSender slackSender;

    public void checkHealth() {
        HealthStatus health = getHealthStatus();

        if (health.getCpuUsage() > 80) {
            Map<String, String> fields = Map.of(
                "CPU", health.getCpuUsage() + "%",
                "Memory", health.getMemoryUsage() + "%",
                "Disk", health.getDiskUsage() + "%"
            );

            slackSender.sendRich(
                ":warning: High CPU Usage",
                "서버 CPU 사용률이 80%를 초과했습니다.",
                NotificationLevel.WARNING.getSlackColor(),
                fields
            );
        }
    }
}
```

### 시나리오 2: 주문 알림 (판매 팀)

```java
@Service
public class OrderService {

    @Autowired
    private TeamsWebhookSender teamsSender;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = orderRepository.save(new Order(request));

        // 큰 주문은 Teams로 알림
        if (order.getAmount() >= 1_000_000) {
            Map<String, String> fields = Map.of(
                "주문 번호", order.getId().toString(),
                "고객명", order.getCustomerName(),
                "금액", String.format("%,d원", order.getAmount()),
                "상품", order.getProductName()
            );

            teamsSender.sendRich(
                "🎉 대형 주문 발생",
                "100만원 이상의 주문이 접수되었습니다.",
                "good",
                fields
            );
        }

        return order;
    }
}
```

### 시나리오 3: 배포 알림 (DevOps)

```java
@Service
public class DeploymentService {

    @Autowired
    private SlackWebhookSender slackSender;

    public void deployApplication(String version, String environment) {
        try {
            // 배포 시작 알림
            slackSender.sendText(
                String.format(":rocket: Deploying %s to %s...",
                    version, environment)
            );

            // 실제 배포
            performDeployment(version, environment);

            // 성공 알림
            slackSender.sendWithLevel(
                NotificationLevel.INFO,
                "Deployment Success",
                String.format("Version %s deployed to %s successfully!",
                    version, environment)
            );

        } catch (Exception e) {
            // 실패 알림
            slackSender.sendError("Deployment Failed", e);
            throw e;
        }
    }
}
```

### 시나리오 4: 보안 이벤트 알림

```java
@Component
public class SecurityEventListener {

    @Autowired
    private SlackWebhookSender slackSender;

    @Autowired
    private TeamsWebhookSender teamsSender;

    @EventListener
    public void onSecurityEvent(SecurityEvent event) {
        if (event.getSeverity() == Severity.HIGH) {
            Map<String, String> fields = Map.of(
                "이벤트", event.getType(),
                "IP 주소", event.getIpAddress(),
                "사용자", event.getUsername(),
                "시간", event.getTimestamp().toString()
            );

            String title = ":rotating_light: 보안 이벤트 감지";
            String message = event.getDescription();

            // Slack과 Teams 모두 알림
            slackSender.sendRich(title, message, "danger", fields);
            teamsSender.sendRich(title, message, "danger", fields);
        }
    }
}
```

## 메시지 포맷팅

### Slack - Markdown 지원

```java
slackSender.sendText(
    "*굵은 글씨*\n" +
    "_이탤릭_\n" +
    "~취소선~\n" +
    "`코드`\n" +
    "```\n" +
    "코드 블록\n" +
    "```\n" +
    "<https://example.com|링크>"
);
```

### Teams - Markdown 지원

```java
teamsSender.sendText(
    "**굵은 글씨**\n" +
    "*이탤릭*\n" +
    "`코드`\n" +
    "```\n" +
    "코드 블록\n" +
    "```\n" +
    "[링크](https://example.com)"
);
```

## 색상 코드

### Slack 색상

- `"good"` → 초록색 (#36a64f)
- `"warning"` → 주황색 (#ff9800)
- `"danger"` → 빨간색 (#f44336)
- `"#RRGGBB"` → 커스텀 Hex 색상

### Teams 색상

- `"good"` 또는 `"36a64f"` → 초록색
- `"warning"` 또는 `"ff9800"` → 주황색
- `"danger"` 또는 `"f44336"` → 빨간색
- `"RRGGBB"` → 커스텀 Hex (# 없이)

## Best Practices

### 1. 알림 빈도 제한

너무 많은 알림은 피로를 유발합니다.

```java
@Service
public class RateLimitedNotificationService {

    private final Map<String, Instant> lastNotified = new ConcurrentHashMap<>();
    private final Duration minInterval = Duration.ofMinutes(5);

    @Autowired
    private SlackWebhookSender slackSender;

    public void notifyIfNeeded(String key, String message) {
        Instant now = Instant.now();
        Instant last = lastNotified.get(key);

        if (last == null || Duration.between(last, now).compareTo(minInterval) > 0) {
            slackSender.sendText(message);
            lastNotified.put(key, now);
        }
    }
}
```

### 2. 환경별 알림 설정

```yaml
# application-dev.yml
eraf:
  notification:
    webhook:
      slack:
        enabled: false  # 개발 환경에서는 비활성화

# application-prod.yml
eraf:
  notification:
    webhook:
      slack:
        enabled: true
        webhook-url: ${SLACK_WEBHOOK_URL}  # 환경 변수
```

### 3. 비동기 전송

```java
@Service
public class AsyncNotificationService {

    @Autowired
    private SlackWebhookSender slackSender;

    @Async
    public CompletableFuture<Void> sendAsync(String message) {
        slackSender.sendText(message);
        return CompletableFuture.completedFuture(null);
    }
}
```

### 4. 에러 처리

```java
@Service
public class SafeNotificationService {

    @Autowired
    private SlackWebhookSender slackSender;

    private static final Logger log = LoggerFactory.getLogger(SafeNotificationService.class);

    public void sendSafely(String message) {
        try {
            slackSender.sendText(message);
        } catch (Exception e) {
            log.error("Failed to send Slack notification", e);
            // 알림 실패가 비즈니스 로직을 방해하지 않도록
        }
    }
}
```

## 트러블슈팅

### 문제: 메시지가 전송되지 않음

**원인**: Webhook URL이 잘못되었거나 만료됨

**해결**: Webhook URL 재생성

### 문제: 한글이 깨짐

**원인**: UTF-8 인코딩 문제

**해결**: RestTemplate에 UTF-8 인코딩 설정
```java
RestTemplate restTemplate = new RestTemplate();
restTemplate.getMessageConverters()
    .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
```

### 문제: "No service" 에러 (Slack)

**원인**: Webhook URL이 유효하지 않음

**해결**: Slack App 설정에서 Webhook 활성화 확인

### 문제: "400 Bad Request" (Teams)

**원인**: MessageCard 형식 오류

**해결**: payload 구조 확인, @type과 @context 필수

## 참고 자료

- [Slack Incoming Webhooks](https://api.slack.com/messaging/webhooks)
- [Slack Block Kit Builder](https://app.slack.com/block-kit-builder/)
- [Teams Incoming Webhooks](https://learn.microsoft.com/en-us/microsoftteams/platform/webhooks-and-connectors/how-to/add-incoming-webhook)
- [Teams MessageCard Reference](https://learn.microsoft.com/en-us/microsoftteams/platform/task-modules-and-cards/cards/cards-reference#office-365-connector-card)
