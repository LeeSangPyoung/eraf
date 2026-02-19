# ERAF Messaging RabbitMQ

RabbitMQ 기반 메시징 솔루션

## 개요

ERAF Messaging RabbitMQ는 RabbitMQ와의 통합을 위한 자동 구성 및 유틸리티를 제공합니다.

### 주요 기능

- **Spring AMQP 자동 구성**
- **메시지 발행/구독 패턴**
- **Dead Letter Queue (DLQ) 지원**
- **재시도 메커니즘**
- **JSON 메시지 자동 변환**

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-messaging-rabbitmq</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 빠른 시작

### 1. 설정

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /

eraf:
  rabbitmq:
    # 자동 재시도
    retry:
      enabled: true
      max-attempts: 3
      initial-interval: 1000
      multiplier: 2.0

    # Dead Letter Queue
    dlq:
      enabled: true
      ttl: 86400000  # 24시간
```

### 2. 메시지 발행

```java
@Service
public class OrderService {

    private final RabbitTemplate rabbitTemplate;

    public void createOrder(Order order) {
        // 주문 처리
        orderRepository.save(order);

        // 이벤트 발행
        rabbitTemplate.convertAndSend(
            "orders.exchange",
            "order.created",
            new OrderCreatedEvent(order.getId(), order.getCustomerId())
        );
    }

    public void sendNotification(String userId, String message) {
        // 메시지 발행 (타임아웃 설정)
        rabbitTemplate.convertAndSend(
            "notifications.exchange",
            "notification.send",
            new NotificationMessage(userId, message),
            msg -> {
                msg.getMessageProperties().setExpiration("60000"); // 60초
                msg.getMessageProperties().setPriority(5);
                return msg;
            }
        );
    }
}
```

### 3. 메시지 구독

```java
@Component
public class OrderEventListener {

    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Order created: {}", event.getOrderId());

        // 주문 생성 후처리
        emailService.sendOrderConfirmation(event.getCustomerId());
        inventoryService.reserve(event.getOrderId());
    }

    @RabbitListener(queues = "order.cancelled.queue")
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Order cancelled: {}", event.getOrderId());

        // 주문 취소 처리
        paymentService.refund(event.getOrderId());
        inventoryService.release(event.getOrderId());
    }
}
```

### 4. Queue 및 Exchange 설정

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable("order.created.queue")
            .withArgument("x-dead-letter-exchange", "dlx.exchange")
            .withArgument("x-message-ttl", 300000) // 5분
            .build();
    }

    @Bean
    public DirectExchange ordersExchange() {
        return new DirectExchange("orders.exchange");
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder
            .bind(orderCreatedQueue())
            .to(ordersExchange())
            .with("order.created");
    }

    // Dead Letter Queue
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("dlq.queue").build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("dlx.exchange");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
            .bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with("#");
    }
}
```

## 실전 예제

### 메시지 재시도

```java
@Component
public class PaymentEventListener {

    @RabbitListener(queues = "payment.process.queue",
                    containerFactory = "retryRabbitListenerContainerFactory")
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void processPayment(PaymentEvent event) {
        // 결제 처리 (실패 시 자동 재시도)
        paymentGateway.charge(event.getPaymentId(), event.getAmount());
    }
}
```

### Fanout Exchange (Pub/Sub)

```java
@Configuration
public class EventBroadcastConfig {

    @Bean
    public FanoutExchange eventBroadcastExchange() {
        return new FanoutExchange("events.fanout");
    }

    @Bean
    public Queue emailQueue() {
        return new Queue("event.email.queue");
    }

    @Bean
    public Queue smsQueue() {
        return new Queue("event.sms.queue");
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(eventBroadcastExchange());
    }

    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsQueue()).to(eventBroadcastExchange());
    }
}

@Service
public class NotificationService {

    @RabbitListener(queues = "event.email.queue")
    public void sendEmail(Event event) {
        emailService.send(event);
    }

    @RabbitListener(queues = "event.sms.queue")
    public void sendSms(Event event) {
        smsService.send(event);
    }
}
```

## 참고 자료

- [Spring AMQP](https://spring.io/projects/spring-amqp)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)

## 라이선스

Copyright 2024 ERAF Platform
