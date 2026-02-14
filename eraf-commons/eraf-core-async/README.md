# ERAF Core - Async

비동기 처리, 이벤트, 메시징 등 비동기 기능을 제공하는 모듈입니다.

## 📦 주요 기능

### 1. 비동기 실행
- **AsyncExecutor**: 비동기 작업 실행기
- **@Async**: Spring Async 지원

### 2. 이벤트 처리
- **EventPublisher**: 애플리케이션 이벤트 발행
- **@AfterCommit**: 트랜잭션 커밋 후 이벤트 처리
- **DomainEvent**: 도메인 이벤트 베이스 클래스

### 3. 메시징
- **MessageProducer**: 메시지 발행
- **MessageConsumer**: 메시지 소비
- **MessageChannel**: 메시지 채널

## 🔗 의존성

**ERAF 모듈**:
- eraf-core-util (유틸리티)
- eraf-core-exception (예외 처리)

**외부 라이브러리**:
- Spring Context Support
- Spring TX (optional - 트랜잭션 이벤트용)

## 📝 사용 예시

### 비동기 메서드 실행
```java
@Service
public class EmailService {

    @Async
    public CompletableFuture<Void> sendEmail(String to, String subject, String body) {
        // 이메일 발송 로직
        mailSender.send(to, subject, body);
        return CompletableFuture.completedFuture(null);
    }
}
```

### 도메인 이벤트 발행
```java
@Service
public class OrderService {

    @Autowired
    private EventPublisher eventPublisher;

    public Order createOrder(OrderRequest request) {
        Order order = new Order(request);
        orderRepository.save(order);

        // 이벤트 발행
        eventPublisher.publish(new OrderCreatedEvent(order));

        return order;
    }
}
```

### 트랜잭션 커밋 후 이벤트 처리
```java
@Component
public class OrderEventListener {

    @AfterCommit
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 트랜잭션 커밋이 성공한 후에만 실행됨
        notificationService.sendOrderConfirmation(event.getOrder());
    }
}
```

### 도메인 이벤트 정의
```java
public class OrderCreatedEvent extends DomainEvent {
    private final Order order;

    public OrderCreatedEvent(Order order) {
        super(order.getId());
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}
```

### 메시지 발행/구독
```java
@Service
public class NotificationService {

    @Autowired
    private MessageProducer messageProducer;

    public void sendNotification(String userId, String message) {
        messageProducer.send("notification-channel", new NotificationMessage(userId, message));
    }
}

@Component
public class NotificationConsumer {

    @MessageListener("notification-channel")
    public void handleNotification(NotificationMessage message) {
        // 메시지 처리
        fcmService.send(message.getUserId(), message.getMessage());
    }
}
```

## 🏗️ 주요 클래스

**비동기**:
- `AsyncExecutor` - 비동기 실행기
- `@Async` - Spring 비동기 어노테이션

**이벤트**:
- `EventPublisher` - 이벤트 발행자
- `@AfterCommit` - 트랜잭션 커밋 후 처리
- `DomainEvent` - 도메인 이벤트 베이스

**메시징**:
- `MessageProducer` - 메시지 발행
- `MessageConsumer` - 메시지 소비
- `MessageChannel` - 메시지 채널

## 📚 패턴 설명

### 비동기 실행
시간이 오래 걸리는 작업을 백그라운드에서 실행하여 응답 시간 개선

### 도메인 이벤트
도메인 로직 간 결합도를 낮추고 확장성 향상

### @AfterCommit
트랜잭션 성공 후에만 이벤트 처리 (메일 발송, 알림 등)

### 메시징
시스템 간 느슨한 결합과 비동기 통신

## ⚠️ 주의사항

- `@Async` 메서드는 public이어야 함
- `@Async` 메서드는 같은 클래스 내에서 호출 시 비동기 동작 안 함
- `@AfterCommit`은 트랜잭션 내에서만 동작
- 이벤트 리스너에서 예외 발생 시 원본 트랜잭션 롤백되지 않음
