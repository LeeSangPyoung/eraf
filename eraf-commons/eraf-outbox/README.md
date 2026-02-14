# ERAF Outbox Pattern

Transactional Outbox Pattern을 구현하여 안정적인 메시지 발행을 보장하는 모듈입니다.

## 개요

Outbox Pattern은 **데이터베이스 트랜잭션**과 **메시지 발행**의 원자성을 보장하는 패턴입니다.

### 문제점

```java
// ❌ 문제: DB 커밋 후 Kafka 전송 실패 시 불일치 발생
@Transactional
public void createOrder(Order order) {
    orderRepository.save(order);  // DB 저장 성공
    kafkaTemplate.send("orders", order); // 실패하면?
}
```

### 해결책

```java
// ✅ 해결: Outbox 테이블에 메시지 저장 → 별도 프로세스에서 발행
@Transactional
public void createOrder(Order order) {
    orderRepository.save(order);
    outboxRepository.save(new OutboxMessage(
        "Order", "OrderCreated", toJson(order)
    )); // 같은 트랜잭션에서 저장
}
```

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-outbox</artifactId>
</dependency>
```

## 데이터베이스 스키마

```sql
CREATE TABLE outbox_messages (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255),
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    retry_count INT DEFAULT 0,
    error_message TEXT
);

CREATE INDEX idx_outbox_status ON outbox_messages(status, created_at);
```

## 사용법

### 1. 메시지 발행

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxPublisher outboxPublisher;

    @Transactional
    public void createOrder(CreateOrderRequest request) {
        // 1. 비즈니스 로직 실행
        Order order = new Order(request);
        orderRepository.save(order);

        // 2. Outbox 메시지 발행 (같은 트랜잭션)
        outboxPublisher.publish(
            "Order",
            order.getId().toString(),
            "OrderCreated",
            toJson(order)
        );
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.cancel();

        outboxPublisher.publish(
            "Order",
            orderId.toString(),
            "OrderCancelled",
            toJson(order)
        );
    }
}
```

### 2. 메시지 발행 스케줄러 (자동)

OutboxScheduler가 주기적으로 PENDING 상태의 메시지를 처리합니다.

```java
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxMessageRepository outboxRepository;
    private final MessagePublisher messagePublisher;

    @Scheduled(fixedDelay = 1000) // 1초마다 실행
    @Transactional
    public void processOutboxMessages() {
        List<OutboxMessage> pendingMessages = outboxRepository
            .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING,
                PageRequest.of(0, 100));

        for (OutboxMessage message : pendingMessages) {
            try {
                // Kafka/RabbitMQ 등으로 발행
                messagePublisher.publish(
                    message.getEventType(),
                    message.getPayload()
                );

                // 성공 처리
                message.setStatus(OutboxStatus.PROCESSED);
                message.setProcessedAt(LocalDateTime.now());

            } catch (Exception e) {
                // 실패 처리
                message.setStatus(OutboxStatus.FAILED);
                message.setRetryCount(message.getRetryCount() + 1);
                message.setErrorMessage(e.getMessage());
            }

            outboxRepository.save(message);
        }
    }
}
```

### 3. Kafka 통합 예시

```java
@Component
@RequiredArgsConstructor
public class KafkaMessagePublisher implements MessagePublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void publish(String eventType, String payload) {
        String topic = determineTopicFromEventType(eventType);
        kafkaTemplate.send(topic, payload);
    }

    private String determineTopicFromEventType(String eventType) {
        return switch (eventType) {
            case "OrderCreated" -> "orders.created";
            case "OrderCancelled" -> "orders.cancelled";
            case "PaymentCompleted" -> "payments.completed";
            default -> "events.default";
        };
    }
}
```

## 설정

### application.yml

```yaml
spring:
  task:
    scheduling:
      pool:
        size: 2  # Scheduler 스레드 풀 크기

outbox:
  scheduler:
    fixed-delay: 1000  # 처리 주기 (ms)
    batch-size: 100    # 한 번에 처리할 메시지 수
    max-retry: 3       # 최대 재시도 횟수
```

## 장점

1. **원자성 보장**: DB 트랜잭션과 메시지 발행이 동일한 트랜잭션
2. **At-Least-Once 전달**: 메시지가 최소 1번은 전달됨을 보장
3. **장애 복구**: 메시지 발행 실패 시 자동 재시도
4. **추적 가능**: Outbox 테이블에서 모든 이벤트 이력 확인 가능

## 주의사항

### 1. 중복 메시지 처리

Consumer 측에서 멱등성(Idempotency) 보장 필요:

```java
@KafkaListener(topics = "orders.created")
public void handleOrderCreated(String message) {
    String messageId = extractMessageId(message);

    // 중복 처리 방지
    if (processedMessageRepository.existsById(messageId)) {
        log.info("Message already processed: {}", messageId);
        return;
    }

    // 실제 처리
    processOrder(message);

    // 처리 완료 기록
    processedMessageRepository.save(new ProcessedMessage(messageId));
}
```

### 2. Outbox 테이블 정리

처리된 메시지를 주기적으로 삭제:

```java
@Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
@Transactional
public void cleanupProcessedMessages() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
    outboxRepository.deleteByStatusAndProcessedAtBefore(
        OutboxStatus.PROCESSED, cutoffDate
    );
}
```

## 참고

- [Microservices Pattern: Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html)
- [Debezium Outbox Event Router](https://debezium.io/documentation/reference/transformations/outbox-event-router.html)
