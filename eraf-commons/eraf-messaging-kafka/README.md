# ERAF Messaging Kafka

Apache Kafka 기반의 이벤트 기반 메시징 시스템입니다. 표준화된 이벤트 포맷, Dead Letter Queue(DLQ), 자동 재시도, 트랜잭션 지원 등 엔터프라이즈급 메시징 기능을 제공합니다.

## 주요 기능

- **표준 이벤트 포맷**: 일관된 구조로 이벤트 발행/소비
- **Dead Letter Queue (DLQ)**: 처리 실패 메시지를 별도 큐로 관리
- **자동 재시도**: 지수 백오프(Exponential Backoff) 전략 지원
- **트랜잭션**: Exactly-Once 시맨틱 지원
- **프로듀서/컨슈머**: 간편한 API로 메시지 송수신
- **AutoConfiguration**: Spring Boot 자동 설정 지원

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-messaging-kafka</artifactId>
</dependency>
```

---

## 1. 기본 설정

### application.yml

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: ${spring.application.name}
      auto-offset-reset: earliest
      enable-auto-commit: false  # 수동 커밋 권장
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all              # 모든 레플리카 확인 (안전성)
      retries: 3
      enable-idempotence: true

# ERAF Kafka 설정
eraf:
  kafka:
    topic-prefix: "eraf-"           # 토픽 접두사
    group-id-prefix: "eraf-group-"  # Consumer 그룹 ID 접두사

    # 재시도 설정
    retry:
      enabled: true
      max-attempts: 3
      backoff-interval: 1000         # 초기 재시도 간격 (1초)
      backoff-multiplier: 2.0        # 지수 백오프 승수
      max-backoff-interval: 30000    # 최대 재시도 간격 (30초)

    # Dead Letter Queue 설정
    dlq:
      enabled: true
      topic-suffix: ".DLQ"           # DLQ 토픽 접미사
      retention-days: 7              # DLQ 보존 기간

    # 트랜잭션 설정
    transaction:
      enabled: false                 # 트랜잭션 활성화 여부
      id-prefix: "eraf-tx-"
      timeout-seconds: 60

    # 프로듀서 설정
    producer:
      compression-type: lz4          # 압축 (none, gzip, snappy, lz4, zstd)
      batch-size: 16384              # 배치 크기
      linger-ms: 10                  # 배치 대기 시간
      acks: all
      idempotence: true
```

---

## 2. 이벤트 발행 (Producer)

### ErafKafkaProducer 사용

```java
import com.eraf.kafka.ErafKafkaProducer;
import com.eraf.kafka.ErafKafkaEvent;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private final ErafKafkaProducer kafkaProducer;

    public OrderEventPublisher(ErafKafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    // 간단한 발행
    public void publishOrderCreated(Order order) {
        kafkaProducer.send("orders",
            "ORDER_CREATED",
            new OrderCreatedPayload(order.getId(), order.getAmount()));
    }

    // 키 지정하여 발행 (같은 키는 같은 파티션으로)
    public void publishWithKey(Order order) {
        kafkaProducer.send("orders",
            order.getUserId(),  // 키: 같은 사용자의 주문은 같은 파티션
            "ORDER_CREATED",
            new OrderCreatedPayload(order.getId(), order.getAmount()));
    }

    // 비동기 발행
    public void publishAsync(Order order) {
        kafkaProducer.sendAsync("orders", "ORDER_CREATED", new OrderCreatedPayload(...))
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event", ex);
                } else {
                    log.info("Event published to partition {} offset {}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });
    }
}
```

### ErafKafkaEvent 빌더 패턴

```java
@Service
public class PaymentEventPublisher {

    private final ErafKafkaProducer kafkaProducer;

    public void publishPaymentCompleted(Payment payment) {
        // Builder 패턴으로 이벤트 생성
        ErafKafkaEvent<PaymentCompletedPayload> event = ErafKafkaEvent.<PaymentCompletedPayload>builder()
            .eventType("PAYMENT_COMPLETED")
            .source("payment-service")
            .traceId(getCurrentTraceId())  // 분산 추적 ID
            .payload(new PaymentCompletedPayload(payment.getId(), payment.getAmount()))
            .metadata(Map.of(
                "userId", payment.getUserId(),
                "paymentMethod", payment.getMethod()
            ))
            .build();

        kafkaProducer.send("payments", event);
    }

    private String getCurrentTraceId() {
        // TraceContext에서 추출
        return "trace-123";
    }
}
```

---

## 3. 이벤트 소비 (Consumer)

### @KafkaListener 사용

```java
import com.eraf.kafka.ErafKafkaConsumer;
import com.eraf.kafka.ErafKafkaEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private final ErafKafkaConsumer kafkaConsumer;
    private final OrderService orderService;

    public OrderEventConsumer(ErafKafkaConsumer kafkaConsumer, OrderService orderService) {
        this.kafkaConsumer = kafkaConsumer;
        this.orderService = orderService;
    }

    // 기본 소비 (자동 ACK)
    @KafkaListener(topics = "eraf-orders", groupId = "order-consumer-group")
    public void handleOrderEvent(String message) {
        kafkaConsumer.process(message, OrderCreatedPayload.class, event -> {
            log.info("Received event: {}", event.getEventType());

            OrderCreatedPayload payload = event.getPayload();
            orderService.processOrder(payload.getOrderId(), payload.getAmount());
        });
    }

    // 수동 ACK (권장)
    @KafkaListener(
        topics = "eraf-payments",
        groupId = "payment-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(String message, Acknowledgment ack) {
        kafkaConsumer.process(message, PaymentCompletedPayload.class, event -> {
            try {
                // 비즈니스 로직 처리
                orderService.completePayment(event.getPayload().getPaymentId());

                // 성공 시 ACK
                ack.acknowledge();

            } catch (Exception e) {
                log.error("Failed to process payment event", e);
                // 실패 시 ACK 안 하면 재시도
                throw e;
            }
        }, ack);
    }
}
```

### 파티션 및 오프셋 지정

```java
@Component
public class AdvancedConsumer {

    // 특정 파티션 지정
    @KafkaListener(
        topicPartitions = @TopicPartition(
            topic = "eraf-orders",
            partitions = {"0", "1"}  // 파티션 0, 1만 소비
        ),
        groupId = "partition-consumer"
    )
    public void consumeFromPartition(String message) {
        // 처리 로직
    }

    // 파티션별 초기 오프셋 지정
    @KafkaListener(
        topicPartitions = @TopicPartition(
            topic = "eraf-orders",
            partitionOffsets = {
                @PartitionOffset(partition = "0", initialOffset = "100"),
                @PartitionOffset(partition = "1", initialOffset = "200")
            }
        )
    )
    public void consumeFromOffset(String message) {
        // 처리 로직
    }
}
```

---

## 4. Dead Letter Queue (DLQ)

### DLQ 자동 처리

재시도 횟수 초과 시 자동으로 DLQ로 전송합니다.

```yaml
eraf:
  kafka:
    dlq:
      enabled: true
      topic-suffix: ".DLQ"  # 원본 토픽이 "orders"면 "orders.DLQ"로 전송
```

### DLQ 발행 (수동)

```java
import com.eraf.kafka.dlq.DeadLetterQueuePublisher;
import com.eraf.kafka.dlq.DeadLetterMessage;

@Component
public class OrderEventConsumer {

    private final DeadLetterQueuePublisher dlqPublisher;

    @KafkaListener(topics = "eraf-orders", groupId = "order-consumer")
    public void handleOrder(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            // 비즈니스 로직 처리
            orderService.processOrder(record.value());
            ack.acknowledge();

        } catch (NonRetryableException e) {
            // 재시도 불가능한 에러는 바로 DLQ로
            DeadLetterMessage dlqMessage = DeadLetterMessage.builder()
                .originalTopic(record.topic())
                .originalPartition(record.partition())
                .originalOffset(record.offset())
                .originalKey(record.key())
                .originalMessage(record.value())
                .errorMessage(e.getMessage())
                .errorStackTrace(getStackTrace(e))
                .retryCount(0)
                .build();

            dlqPublisher.publish(dlqMessage);
            ack.acknowledge();  // ACK하여 재시도 방지

        } catch (RetryableException e) {
            // 재시도 가능한 에러는 ACK 안 함
            log.warn("Retryable error occurred, will retry: {}", e.getMessage());
            throw e;  // 재시도 유발
        }
    }
}
```

### DLQ 메시지 조회 및 재처리

```java
@Component
public class DlqConsumer {

    private final DeadLetterQueuePublisher dlqPublisher;

    // DLQ 메시지 모니터링
    @KafkaListener(topics = "eraf-orders.DLQ", groupId = "dlq-monitor")
    public void monitorDlq(String message) {
        DeadLetterMessage dlqMessage = objectMapper.readValue(message, DeadLetterMessage.class);

        log.error("DLQ Message: topic={}, error={}, retryCount={}",
            dlqMessage.getOriginalTopic(),
            dlqMessage.getErrorMessage(),
            dlqMessage.getRetryCount());

        // 알림 발송 (Slack, Email 등)
        notificationService.sendAlert("DLQ message detected", dlqMessage);
    }

    // DLQ 메시지 재처리
    public void reprocessDlqMessage(String dlqTopic, long offset) {
        // DLQ에서 메시지 조회
        DeadLetterMessage dlqMessage = fetchFromDlq(dlqTopic, offset);

        // 원본 토픽으로 재발행
        dlqPublisher.reprocess(dlqMessage);
    }
}
```

---

## 5. 재시도 전략

### 지수 백오프 (Exponential Backoff)

```yaml
eraf:
  kafka:
    retry:
      enabled: true
      max-attempts: 5
      backoff-interval: 1000      # 1초
      backoff-multiplier: 2.0     # 2배씩 증가
      max-backoff-interval: 60000 # 최대 60초
```

**재시도 간격**:
- 1차: 1초
- 2차: 2초
- 3차: 4초
- 4차: 8초
- 5차: 16초 → 실패 시 DLQ로

### 커스텀 재시도 로직

```java
@Component
public class CustomRetryConsumer {

    @KafkaListener(topics = "eraf-orders")
    @RetryableTopic(
        attempts = "5",
        backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 60000),
        dltTopicSuffix = ".DLQ",
        include = {RetryableException.class},  // 재시도할 예외
        exclude = {NonRetryableException.class}  // 재시도 안 할 예외
    )
    public void handleWithRetry(String message) {
        orderService.processOrder(message);
    }

    // DLT (Dead Letter Topic) 핸들러
    @DltHandler
    public void handleDlt(String message,
                          @Header(KafkaHeaders.ORIGINAL_TOPIC) String originalTopic,
                          @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage) {
        log.error("Message sent to DLT from {}: error={}", originalTopic, errorMessage);
        // 알림 또는 수동 처리
    }
}
```

---

## 6. 트랜잭션 (Exactly-Once Semantics)

### 트랜잭션 활성화

```yaml
eraf:
  kafka:
    transaction:
      enabled: true
      id-prefix: "order-service-tx-"
      timeout-seconds: 60

spring:
  kafka:
    producer:
      transaction-id-prefix: ${eraf.kafka.transaction.id-prefix}
```

### 트랜잭션 프로듀서

```java
@Service
public class TransactionalOrderService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderRepository orderRepository;

    @Transactional
    public void createOrder(OrderRequest request) {
        // 1. DB 저장 (JPA 트랜잭션)
        Order order = orderRepository.save(new Order(request));

        // 2. Kafka 전송 (Kafka 트랜잭션)
        kafkaTemplate.executeInTransaction(ops -> {
            // 여러 메시지를 원자적으로 전송
            ops.send("orders", buildOrderEvent(order));
            ops.send("notifications", buildNotificationEvent(order));
            ops.send("analytics", buildAnalyticsEvent(order));
            return true;
        });

        // 둘 다 성공하거나 둘 다 롤백
    }
}
```

### 트랜잭션 컨슈머

```java
@Component
public class TransactionalConsumer {

    @KafkaListener(topics = "eraf-orders")
    @Transactional  // Kafka 트랜잭션 + DB 트랜잭션
    public void handleOrder(String message, Acknowledgment ack) {
        // 1. 메시지 처리
        Order order = parseOrder(message);

        // 2. DB 저장
        orderRepository.save(order);

        // 3. 다른 토픽으로 메시지 전송
        kafkaTemplate.send("order-processed", toJson(order));

        // 4. ACK
        ack.acknowledge();

        // 모두 성공하거나 모두 롤백
    }
}
```

---

## 7. 실전 예제

### 주문 시스템 이벤트 플로우

```java
// 1. Order Service - 주문 생성 및 이벤트 발행
@Service
public class OrderService {

    private final ErafKafkaProducer kafkaProducer;
    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(OrderRequest request) {
        // 주문 생성
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setItems(request.getItems());
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);

        // 주문 생성 이벤트 발행
        OrderCreatedEvent payload = new OrderCreatedEvent(
            order.getId(),
            order.getUserId(),
            order.getTotalAmount(),
            order.getItems()
        );

        kafkaProducer.send("orders",
            order.getUserId(),  // 같은 사용자는 같은 파티션
            "ORDER_CREATED",
            payload);

        return order;
    }
}

// 2. Inventory Service - 재고 확인
@Component
public class InventoryEventConsumer {

    private final InventoryService inventoryService;
    private final ErafKafkaProducer kafkaProducer;

    @KafkaListener(topics = "eraf-orders", groupId = "inventory-service")
    public void handleOrderCreated(String message, Acknowledgment ack) {
        kafkaConsumer.process(message, OrderCreatedEvent.class, event -> {
            if (!"ORDER_CREATED".equals(event.getEventType())) {
                ack.acknowledge();
                return;
            }

            OrderCreatedEvent payload = event.getPayload();

            // 재고 확인
            boolean available = inventoryService.checkAvailability(payload.getItems());

            if (available) {
                // 재고 차감
                inventoryService.reserve(payload.getItems());

                // 재고 예약 완료 이벤트 발행
                kafkaProducer.send("inventory",
                    "INVENTORY_RESERVED",
                    new InventoryReservedEvent(payload.getOrderId(), payload.getItems()));
            } else {
                // 재고 부족 이벤트 발행
                kafkaProducer.send("inventory",
                    "INVENTORY_INSUFFICIENT",
                    new InventoryInsufficientEvent(payload.getOrderId()));
            }

            ack.acknowledge();
        }, ack);
    }
}

// 3. Payment Service - 결제 처리
@Component
public class PaymentEventConsumer {

    @KafkaListener(topics = "eraf-inventory", groupId = "payment-service")
    public void handleInventoryReserved(String message, Acknowledgment ack) {
        kafkaConsumer.process(message, InventoryReservedEvent.class, event -> {
            if (!"INVENTORY_RESERVED".equals(event.getEventType())) {
                ack.acknowledge();
                return;
            }

            InventoryReservedEvent payload = event.getPayload();

            // 결제 처리
            PaymentResult result = paymentService.processPayment(payload.getOrderId());

            if (result.isSuccess()) {
                kafkaProducer.send("payments",
                    "PAYMENT_COMPLETED",
                    new PaymentCompletedEvent(payload.getOrderId(), result.getTransactionId()));
            } else {
                kafkaProducer.send("payments",
                    "PAYMENT_FAILED",
                    new PaymentFailedEvent(payload.getOrderId(), result.getErrorMessage()));
            }

            ack.acknowledge();
        }, ack);
    }
}

// 4. Order Service - 주문 상태 업데이트
@Component
public class OrderStatusConsumer {

    @KafkaListener(topics = "eraf-payments", groupId = "order-status-updater")
    public void handlePaymentCompleted(String message, Acknowledgment ack) {
        kafkaConsumer.process(message, PaymentCompletedEvent.class, event -> {
            if ("PAYMENT_COMPLETED".equals(event.getEventType())) {
                orderService.updateOrderStatus(event.getPayload().getOrderId(), OrderStatus.PAID);
            } else if ("PAYMENT_FAILED".equals(event.getEventType())) {
                orderService.updateOrderStatus(event.getPayload().getOrderId(), OrderStatus.PAYMENT_FAILED);
            }

            ack.acknowledge();
        }, ack);
    }
}
```

---

## 8. 이벤트 포맷 (ErafKafkaEvent)

### 표준 이벤트 구조

```json
{
  "eventId": "550e8400-e29b-41d4-a716-446655440000",
  "eventType": "ORDER_CREATED",
  "timestamp": "2025-02-12T10:30:00Z",
  "source": "order-service",
  "traceId": "trace-123",
  "version": "1.0",
  "payload": {
    "orderId": 1001,
    "userId": "user-123",
    "totalAmount": 50000,
    "items": [
      {"productId": "P001", "quantity": 2, "price": 25000}
    ]
  },
  "metadata": {
    "region": "ap-northeast-2",
    "environment": "production"
  }
}
```

### 이벤트 필드

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `eventId` | String | ✅ | 이벤트 고유 ID (UUID, 자동 생성) |
| `eventType` | String | ✅ | 이벤트 타입 (예: ORDER_CREATED) |
| `timestamp` | Instant | ✅ | 이벤트 발생 시간 (자동 생성) |
| `source` | String | ❌ | 소스 서비스명 |
| `traceId` | String | ❌ | 분산 추적 ID |
| `version` | String | ✅ | 이벤트 버전 (기본: 1.0) |
| `payload` | Generic | ✅ | 이벤트 데이터 |
| `metadata` | Map | ❌ | 추가 메타데이터 |

---

## 9. 토픽 관리

### 토픽 명명 규칙

```
eraf-{domain}.{event-type}

예시:
- eraf-orders.created
- eraf-payments.completed
- eraf-inventory.reserved
- eraf-notifications.sent
```

### 토픽 생성 (Kafka CLI)

```bash
# 토픽 생성
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic eraf-orders \
  --partitions 3 \
  --replication-factor 2

# DLQ 토픽 생성
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic eraf-orders.DLQ \
  --partitions 1 \
  --replication-factor 2 \
  --config retention.ms=604800000  # 7일 보존

# 토픽 목록 조회
kafka-topics.sh --list \
  --bootstrap-server localhost:9092

# 토픽 상세 정보
kafka-topics.sh --describe \
  --bootstrap-server localhost:9092 \
  --topic eraf-orders
```

### 파티션 전략

```java
// 1. 키 기반 파티셔닝 (같은 키는 같은 파티션)
kafkaProducer.send("orders",
    order.getUserId(),  // 키
    "ORDER_CREATED",
    payload);

// 2. 라운드 로빈 (키 없음)
kafkaProducer.send("orders", "ORDER_CREATED", payload);

// 3. 커스텀 파티셔너
@Component
public class CustomPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes,
                         Object value, byte[] valueBytes, Cluster cluster) {
        // 커스텀 로직
        int numPartitions = cluster.partitionCountForTopic(topic);
        return Math.abs(key.hashCode()) % numPartitions;
    }
}
```

---

## 10. 모니터링 및 운영

### Consumer Lag 모니터링

```bash
# Consumer 그룹 목록
kafka-consumer-groups.sh --list \
  --bootstrap-server localhost:9092

# Consumer Lag 확인
kafka-consumer-groups.sh --describe \
  --bootstrap-server localhost:9092 \
  --group order-consumer-group

# 출력:
# TOPIC      PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
# eraf-orders    0          1000            1050         50
# eraf-orders    1          2000            2000          0
# eraf-orders    2          1500            1600        100
```

### 메트릭 수집

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    tags:
      application: ${spring.application.name}
```

**Kafka 메트릭**:
- `kafka.consumer.records.consumed.total` - 소비된 레코드 수
- `kafka.consumer.records.lag` - Consumer Lag
- `kafka.producer.record.send.total` - 발행된 레코드 수
- `kafka.producer.record.error.total` - 발행 실패 수

---

## 11. 모범 사례

### 1. 이벤트 타입 네이밍

```java
// 좋은 예 - 명확하고 일관성 있음
"ORDER_CREATED"
"ORDER_UPDATED"
"ORDER_CANCELLED"
"PAYMENT_COMPLETED"
"PAYMENT_FAILED"

// 나쁜 예 - 불명확하고 일관성 없음
"create_order"
"UpdatedOrder"
"order-cancel"
```

### 2. 키 선택

```java
// 좋은 예 - 같은 엔티티는 순서 보장
kafkaProducer.send("orders",
    order.getUserId(),  // 같은 사용자는 순서 보장
    "ORDER_CREATED",
    payload);

// 나쁜 예 - 순서 보장 안 됨
kafkaProducer.send("orders", "ORDER_CREATED", payload);
```

### 3. 멱등성 (Idempotency)

```java
// 이벤트 ID로 중복 처리 방지
@KafkaListener(topics = "eraf-orders")
public void handleOrder(String message, Acknowledgment ack) {
    ErafKafkaEvent<OrderCreatedEvent> event = kafkaConsumer.parse(message, OrderCreatedEvent.class);

    // 중복 체크
    if (processedEventRepository.existsByEventId(event.getEventId())) {
        log.warn("Duplicate event: {}", event.getEventId());
        ack.acknowledge();
        return;
    }

    // 처리
    orderService.processOrder(event.getPayload());

    // 처리 기록
    processedEventRepository.save(new ProcessedEvent(event.getEventId()));

    ack.acknowledge();
}
```

### 4. 에러 핸들링

```java
// 재시도 가능/불가능 구분
try {
    orderService.processOrder(payload);
} catch (ValidationException e) {
    // 재시도 불가능 → DLQ로
    sendToDlq(message, e);
    ack.acknowledge();
} catch (TemporaryException e) {
    // 재시도 가능 → throw (재시도 유발)
    throw e;
}
```

### 5. 배치 처리

```java
// 대량 메시지는 배치로 처리
@KafkaListener(topics = "eraf-orders", containerFactory = "batchListenerContainerFactory")
public void handleBatch(List<String> messages, Acknowledgment ack) {
    List<Order> orders = messages.stream()
        .map(msg -> kafkaConsumer.parse(msg, OrderCreatedEvent.class))
        .map(event -> event.getPayload())
        .collect(Collectors.toList());

    orderService.processBatch(orders);
    ack.acknowledge();
}
```

---

## 12. 문제 해결

### Consumer Lag 증가

**원인**: 처리 속도 < 발행 속도

**해결책**:
1. Consumer 인스턴스 증가 (파티션 수만큼 가능)
2. 파티션 수 증가
3. 배치 처리 적용
4. 처리 로직 최적화

### 메시지 누락

**원인**: ACK 전에 애플리케이션 종료

**해결책**:
```yaml
spring:
  kafka:
    consumer:
      enable-auto-commit: false  # 수동 커밋
```

### 중복 메시지

**원인**: 재시도, 네트워크 이슈

**해결책**: 멱등성 보장 (이벤트 ID 중복 체크)

### DLQ 메시지 쌓임

**원인**: 비즈니스 로직 오류

**해결책**:
1. DLQ 모니터링 알림 설정
2. 정기적 DLQ 메시지 검토
3. 버그 수정 후 재처리

---

## 참고 자료

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)
- [Confluent Documentation](https://docs.confluent.io/)
- [eraf-outbox Documentation](../eraf-outbox/) - Outbox Pattern 통합

---

## eraf-messaging-kafka vs eraf-outbox

| 항목 | eraf-messaging-kafka | eraf-outbox |
|------|---------------------|-------------|
| 목적 | Kafka 메시징 | Outbox Pattern (DB + 메시징) |
| 데이터 손실 방지 | 재시도, DLQ | 트랜잭션 Outbox |
| 사용 시점 | 일반적인 이벤트 발행 | DB 트랜잭션과 함께 |
| 메시징 시스템 | Kafka 전용 | 메시징 시스템 무관 |

**권장 사용**:
- **eraf-messaging-kafka**: 일반적인 Kafka 기반 이벤트 스트리밍
- **eraf-outbox**: DB 트랜잭션과 메시징을 원자적으로 처리해야 하는 경우
