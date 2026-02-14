# ERAF Notification

통합 알림 서비스를 제공하는 모듈입니다.

## 기능

- **Email**: SMTP 기반 이메일 발송
- **SMS**: Twilio, AWS SNS, NHN, Naver SMS 지원
- **Push**: FCM (Firebase), APNs (Apple Push)
- **Webhook**: Slack, Microsoft Teams 연동
- **알림 이력**: 발송 이력 저장 및 조회
- **템플릿**: 다국어 템플릿 지원

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-notification</artifactId>
</dependency>
```

## 설정

### application.yml

```yaml
eraf:
  notification:
    email:
      host: smtp.gmail.com
      port: 587
      username: ${EMAIL_USERNAME}
      password: ${EMAIL_PASSWORD}
      from: noreply@example.com
    sms:
      provider: twilio  # twilio, aws-sns, nhn, naver
      twilio:
        account-sid: ${TWILIO_ACCOUNT_SID}
        auth-token: ${TWILIO_AUTH_TOKEN}
        from-number: +1234567890
    push:
      fcm:
        credentials-path: /path/to/firebase-credentials.json
      apns:
        key-path: /path/to/apns-key.p8
        key-id: ${APNS_KEY_ID}
        team-id: ${APNS_TEAM_ID}
        bundle-id: com.example.app
    webhook:
      slack:
        webhook-url: ${SLACK_WEBHOOK_URL}
      teams:
        webhook-url: ${TEAMS_WEBHOOK_URL}
```

## 사용법

### 1. Email 발송

```java
import com.eraf.notification.NotificationService;
import com.eraf.notification.email.EmailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final NotificationService notificationService;

    public void sendWelcomeEmail(String to, String username) {
        EmailMessage email = EmailMessage.builder()
            .to(to)
            .subject("Welcome to ERAF!")
            .body("Hello " + username + ", welcome to our platform!")
            .html(false)
            .build();

        notificationService.sendEmail(email);
    }

    public void sendHtmlEmail(String to, String htmlContent) {
        EmailMessage email = EmailMessage.builder()
            .to(to)
            .subject("Monthly Report")
            .body(htmlContent)
            .html(true)
            .build();

        notificationService.sendEmail(email);
    }
}
```

### 2. SMS 발송

```java
import com.eraf.notification.sms.SmsMessage;

public void sendVerificationSms(String phoneNumber, String code) {
    SmsMessage sms = SmsMessage.builder()
        .to(phoneNumber)
        .message("Your verification code is: " + code)
        .build();

    notificationService.sendSms(sms);
}

public void sendBulkSms(List<String> phoneNumbers, String message) {
    phoneNumbers.forEach(phone -> {
        SmsMessage sms = SmsMessage.builder()
            .to(phone)
            .message(message)
            .build();
        notificationService.sendSms(sms);
    });
}
```

### 3. Push 알림

```java
import com.eraf.notification.push.PushMessage;

public void sendPushNotification(String deviceToken, String title, String body) {
    PushMessage push = PushMessage.builder()
        .token(deviceToken)
        .title(title)
        .body(body)
        .data(Map.of("action", "open_detail", "id", "123"))
        .build();

    notificationService.sendPush(push);
}

public void sendTopicNotification(String topic, String message) {
    PushMessage push = PushMessage.builder()
        .topic(topic)
        .title("New Update")
        .body(message)
        .build();

    notificationService.sendPush(push);
}
```

### 4. Slack/Teams 알림

```java
import com.eraf.notification.webhook.WebhookMessage;

public void sendSlackAlert(String message) {
    WebhookMessage webhook = WebhookMessage.builder()
        .text(message)
        .channel("#alerts")
        .username("Alert Bot")
        .build();

    notificationService.sendWebhook("slack", webhook);
}

public void sendTeamsNotification(String title, String message) {
    WebhookMessage webhook = WebhookMessage.builder()
        .title(title)
        .text(message)
        .color("#0078D4")
        .build();

    notificationService.sendWebhook("teams", webhook);
}
```

## 고급 기능

### 1. 템플릿 사용

```java
import com.eraf.notification.template.NotificationTemplate;

@Service
public class TemplateNotificationService {

    public void sendOrderConfirmation(String email, Order order) {
        Map<String, Object> variables = Map.of(
            "orderNumber", order.getId(),
            "totalAmount", order.getTotalAmount(),
            "customerName", order.getCustomerName()
        );

        notificationService.sendTemplatedEmail(
            email,
            "order-confirmation",  // 템플릿 이름
            variables
        );
    }
}
```

**템플릿 파일** (`templates/notifications/order-confirmation.html`):

```html
<!DOCTYPE html>
<html>
<body>
    <h1>주문 확인</h1>
    <p>안녕하세요 ${customerName}님,</p>
    <p>주문번호: ${orderNumber}</p>
    <p>총 금액: ${totalAmount}원</p>
</body>
</html>
```

### 2. 비동기 발송

```java
import org.springframework.scheduling.annotation.Async;

@Service
public class AsyncNotificationService {

    @Async
    public void sendEmailAsync(EmailMessage email) {
        notificationService.sendEmail(email);
    }

    @Async
    public CompletableFuture<Boolean> sendWithResult(EmailMessage email) {
        try {
            notificationService.sendEmail(email);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(false);
        }
    }
}
```

### 3. 알림 이력 조회

```java
import com.eraf.notification.history.NotificationHistory;
import com.eraf.notification.history.NotificationHistoryRepository;

@Service
public class NotificationHistoryService {

    private final NotificationHistoryRepository historyRepository;

    public List<NotificationHistory> getUserNotifications(Long userId) {
        return historyRepository.findByUserId(userId);
    }

    public List<NotificationHistory> getFailedNotifications() {
        return historyRepository.findByStatus("FAILED");
    }

    public void retryFailed(Long historyId) {
        NotificationHistory history = historyRepository.findById(historyId)
            .orElseThrow();

        // 재발송
        notificationService.retry(history);
    }
}
```

### 4. Email 첨부파일

```java
import org.springframework.core.io.ByteArrayResource;

public void sendEmailWithAttachment(String to, byte[] pdfData) {
    EmailMessage email = EmailMessage.builder()
        .to(to)
        .subject("Invoice")
        .body("Please find attached invoice.")
        .attachment(new ByteArrayResource(pdfData), "invoice.pdf", "application/pdf")
        .build();

    notificationService.sendEmail(email);
}
```

### 5. SMS 국제 발송

```java
public void sendInternationalSms(String phoneNumber, String message) {
    // E.164 형식: +82 10-1234-5678 → +821012345678
    String e164Number = normalizePhoneNumber(phoneNumber);

    SmsMessage sms = SmsMessage.builder()
        .to(e164Number)
        .message(message)
        .build();

    notificationService.sendSms(sms);
}

private String normalizePhoneNumber(String phone) {
    return phone.replaceAll("[^0-9+]", "");
}
```

## 다중 프로바이더 설정

### SMS 프로바이더별 설정

```yaml
eraf:
  notification:
    sms:
      provider: twilio

      twilio:
        account-sid: ${TWILIO_ACCOUNT_SID}
        auth-token: ${TWILIO_AUTH_TOKEN}
        from-number: +1234567890

      aws-sns:
        access-key: ${AWS_ACCESS_KEY}
        secret-key: ${AWS_SECRET_KEY}
        region: ap-northeast-2

      nhn:
        app-key: ${NHN_APP_KEY}
        secret-key: ${NHN_SECRET_KEY}
        sender-number: 01012345678

      naver:
        service-id: ${NAVER_SERVICE_ID}
        access-key: ${NAVER_ACCESS_KEY}
        secret-key: ${NAVER_SECRET_KEY}
        calling-number: 01012345678
```

### 프로바이더 전환

```java
@Service
public class MultiProviderSmsService {

    private final Map<String, SmsSender> smsProviders;

    public void sendSms(String provider, SmsMessage message) {
        SmsSender sender = smsProviders.get(provider);
        if (sender == null) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }
        sender.send(message);
    }
}
```

## Slack Rich Message

```java
import com.eraf.notification.webhook.SlackMessage;

public void sendRichSlackMessage() {
    SlackMessage message = SlackMessage.builder()
        .channel("#general")
        .username("Deploy Bot")
        .iconEmoji(":rocket:")
        .text("Deployment Completed!")
        .attachment(SlackAttachment.builder()
            .title("Production Deployment")
            .text("Version 1.2.3 deployed successfully")
            .color("good")
            .field("Environment", "Production", true)
            .field("Build", "#456", true)
            .field("Duration", "5m 23s", true)
            .build())
        .build();

    notificationService.sendSlack(message);
}
```

## 에러 처리 및 재시도

```java
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

@Service
public class ResilientNotificationService {

    @Retryable(
        value = {MessagingException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendEmailWithRetry(EmailMessage email) {
        try {
            notificationService.sendEmail(email);
        } catch (MessagingException e) {
            log.error("Email send failed", e);
            throw e;  // 재시도
        }
    }

    public void sendWithFallback(String message) {
        try {
            sendPush(message);
        } catch (Exception e) {
            log.warn("Push failed, falling back to email");
            sendEmail(message);
        }
    }
}
```

## 모범 사례

1. **비동기 발송**: @Async로 응답 시간 최소화
2. **템플릿 사용**: 하드코딩 대신 템플릿 파일 관리
3. **이력 저장**: 발송 이력으로 추적 및 디버깅
4. **재시도 로직**: 일시적 실패에 대한 재시도
5. **Fallback**: 주 채널 실패 시 대체 채널 사용
6. **Rate Limiting**: 프로바이더 제한 준수
7. **개인정보 보호**: 이력 저장 시 민감 정보 마스킹

## 참고

- [JavaMail API](https://javaee.github.io/javamail/)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [Twilio SMS API](https://www.twilio.com/docs/sms)
- [Slack Incoming Webhooks](https://api.slack.com/messaging/webhooks)
