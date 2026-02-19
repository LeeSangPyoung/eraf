# ERAF Saga

분산 트랜잭션 관리를 위한 Saga 패턴 구현체

## 개요

ERAF Saga는 마이크로서비스 환경에서 분산 트랜잭션을 관리하기 위한 Saga 패턴 구현을 제공합니다.

### Saga 패턴이란?

Saga 패턴은 분산 시스템에서 데이터 일관성을 유지하기 위한 패턴으로, 각 서비스의 로컬 트랜잭션을 순차적으로 실행하고, 실패 시 보상 트랜잭션(Compensation)을 통해 롤백하는 방식입니다.

### 주요 기능

- **선언적 Saga 정의**: `@Saga`, `@SagaStep`, `@Compensate` 어노테이션 기반
- **자동 보상 트랜잭션**: 실패 시 성공한 단계들을 역순으로 보상
- **재시도 메커니즘**: 각 단계별 재시도 횟수 및 지연 시간 설정
- **실행 상태 추적**: 각 Saga 실행 상태 및 단계별 상태 추적
- **복구 기능**: 실패한 Saga 재시도 및 복구
- **이벤트 발행**: Spring Events 또는 메시징 시스템을 통한 이벤트 발행
- **REST API**: Saga 모니터링 및 관리 API 제공
- **영속화**: InMemory 또는 JPA 기반 저장소 지원
- **자동 클린업**: 완료된 Saga 실행 이력 자동 정리

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-saga</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 빠른 시작

### 1. Saga 정의

```java
import com.eraf.saga.annotation.Saga;
import com.eraf.saga.annotation.SagaStep;
import com.eraf.saga.annotation.Compensate;
import com.eraf.saga.core.SagaContext;
import org.springframework.stereotype.Component;

@Saga(name = "order-saga", description = "주문 처리 Saga", maxRetries = 3)
@Component
public class OrderSaga {

    @SagaStep(order = 1, name = "reserve-inventory", compensate = "cancelReservation")
    public void reserveInventory(OrderRequest request, SagaContext context) {
        // 재고 예약 로직
        log.info("Reserving inventory for order: {}", request.getOrderId());
        inventoryService.reserve(request.getProductId(), request.getQuantity());

        // Context에 데이터 저장 (보상 트랜잭션에서 사용 가능)
        context.put("reservationId", "R-" + request.getOrderId());
    }

    @Compensate
    public void cancelReservation(SagaContext context) {
        // 재고 예약 취소 (보상 트랜잭션)
        String reservationId = context.get("reservationId");
        log.info("Canceling inventory reservation: {}", reservationId);
        inventoryService.cancel(reservationId);
    }

    @SagaStep(order = 2, name = "process-payment", compensate = "refundPayment")
    public void processPayment(OrderRequest request, SagaContext context) {
        // 결제 처리 로직
        log.info("Processing payment for order: {}", request.getOrderId());
        String paymentId = paymentService.charge(request.getAmount());
        context.put("paymentId", paymentId);
    }

    @Compensate
    public void refundPayment(SagaContext context) {
        // 결제 환불 (보상 트랜잭션)
        String paymentId = context.get("paymentId");
        log.info("Refunding payment: {}", paymentId);
        paymentService.refund(paymentId);
    }

    @SagaStep(order = 3, name = "create-order", compensate = "cancelOrder")
    public void createOrder(OrderRequest request, SagaContext context) {
        // 주문 생성 로직
        log.info("Creating order: {}", request.getOrderId());
        Order order = orderService.create(request);
        context.put("orderId", order.getId());
    }

    @Compensate
    public void cancelOrder(SagaContext context) {
        // 주문 취소 (보상 트랜잭션)
        Long orderId = context.get("orderId");
        log.info("Canceling order: {}", orderId);
        orderService.cancel(orderId);
    }
}
```

### 2. Saga 실행

```java
import com.eraf.saga.core.SagaOrchestrator;
import com.eraf.saga.execution.SagaExecution;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final SagaOrchestrator sagaOrchestrator;

    public OrderService(SagaOrchestrator sagaOrchestrator) {
        this.sagaOrchestrator = sagaOrchestrator;
    }

    public OrderResult createOrder(OrderRequest request) {
        // 동기 실행
        SagaExecution execution = sagaOrchestrator.execute("order-saga", request);

        if (execution.getStatus() == SagaStatus.COMPLETED) {
            return OrderResult.success(execution.getId());
        } else {
            return OrderResult.failure(execution.getFailureReason());
        }
    }

    public CompletableFuture<OrderResult> createOrderAsync(OrderRequest request) {
        // 비동기 실행
        return sagaOrchestrator.executeAsync("order-saga", request)
            .thenApply(execution -> {
                if (execution.getStatus() == SagaStatus.COMPLETED) {
                    return OrderResult.success(execution.getId());
                } else {
                    return OrderResult.failure(execution.getFailureReason());
                }
            });
    }

    public OrderResult createOrderWithTracing(OrderRequest request, String traceId) {
        // TraceId와 함께 실행 (분산 추적)
        SagaExecution execution = sagaOrchestrator.execute("order-saga", request, traceId);
        return toOrderResult(execution);
    }
}
```

### 3. 실행 상태 조회

```java
@Service
public class SagaMonitoringService {

    private final SagaOrchestrator sagaOrchestrator;

    public SagaExecution getExecutionStatus(String executionId) {
        return sagaOrchestrator.getExecution(executionId);
    }

    public void printExecutionDetails(String executionId) {
        SagaExecution execution = sagaOrchestrator.getExecution(executionId);

        System.out.println("Saga: " + execution.getSagaName());
        System.out.println("Status: " + execution.getStatus());
        System.out.println("Current Step: " + execution.getCurrentStep());

        execution.getSteps().forEach(step -> {
            System.out.printf("  Step %d: %s - %s%n",
                step.getOrder(), step.getName(), step.getStatus());
        });
    }
}
```

## 설정

### application.yml

```yaml
eraf:
  saga:
    # Saga 모듈 활성화 (기본: true)
    enabled: true

    # 저장소 타입 (memory, jpa)
    repository-type: memory

    # 이벤트 발행자 타입 (spring, messaging)
    event-publisher-type: spring

    # REST API 활성화 (기본: true)
    api-enabled: true

    # REST API 경로 (기본: /api/saga)
    api-path: /api/saga

    # 자동 클린업 활성화 (기본: true)
    cleanup-enabled: true

    # 클린업 주기 (cron 표현식, 기본: 매일 새벽 3시)
    cleanup-cron: "0 0 3 * * ?"

    # 완료된 Saga 보관 기간 (일, 기본: 7일)
    retention-days: 7

    # 복구 설정
    recovery:
      # 자동 복구 활성화 (기본: true)
      auto-recovery-enabled: true

      # 복구 주기 (cron 표현식, 기본: 매 5분)
      recovery-cron: "0 */5 * * * ?"

      # 복구 대상 최대 재시도 횟수 (기본: 3)
      max-recovery-attempts: 3
```

### JPA 저장소 사용

```yaml
eraf:
  saga:
    repository-type: jpa

spring:
  jpa:
    hibernate:
      ddl-auto: update
```

JPA 엔티티는 자동으로 생성됩니다.

### 메시징 이벤트 발행자 사용

```yaml
eraf:
  saga:
    event-publisher-type: messaging
```

`MessagePublisher` 빈이 필요합니다 (eraf-messaging 모듈).

## 어노테이션

### @Saga

클래스에 적용하여 Saga를 정의합니다.

```java
@Saga(
    name = "order-saga",           // Saga 이름 (필수, 고유 식별자)
    description = "주문 처리 Saga",  // Saga 설명
    timeout = 300000,               // 타임아웃 (밀리초, 기본: 300000)
    maxRetries = 3                  // 최대 재시도 횟수 (기본: 3)
)
```

### @SagaStep

메서드에 적용하여 Saga의 단계를 정의합니다.

```java
@SagaStep(
    order = 1,                      // 단계 순서 (필수, 1부터 시작)
    name = "reserve-inventory",     // 단계 이름
    compensate = "cancelReservation", // 보상 트랜잭션 메서드 이름
    timeout = 0,                    // 타임아웃 (0이면 Saga 기본값 사용)
    retries = 0,                    // 재시도 횟수 (0이면 Saga 기본값 사용)
    retryDelay = 1000               // 재시도 간격 (밀리초, 기본: 1000)
)
```

### @Compensate

보상 트랜잭션 메서드에 적용합니다.

```java
@Compensate
public void cancelReservation(SagaContext context) {
    // 보상 로직
}
```

또는 `@SagaStep`의 `compensate` 속성으로 메서드 이름을 지정합니다.

## SagaContext

`SagaContext`는 Saga 실행 중 데이터를 공유하기 위한 컨텍스트입니다.

```java
@SagaStep(order = 1, name = "step1")
public void step1(OrderRequest request, SagaContext context) {
    // 데이터 저장
    context.put("key", "value");
    context.put("orderId", 12345L);

    // 입력 데이터 가져오기
    OrderRequest input = context.getInput();
}

@Compensate
public void compensateStep1(SagaContext context) {
    // 저장된 데이터 가져오기
    String value = context.get("key");
    Long orderId = context.get("orderId");
}
```

메서드 파라미터는 다음 형식을 지원합니다:

```java
// 파라미터 없음
@SagaStep(order = 1)
public void step1() { }

// SagaContext만
@SagaStep(order = 2)
public void step2(SagaContext context) { }

// Input만
@SagaStep(order = 3)
public void step3(OrderRequest input) { }

// Input과 SagaContext
@SagaStep(order = 4)
public void step4(OrderRequest input, SagaContext context) { }
```

## Saga 실행 상태

### SagaStatus

- `STARTED`: Saga 실행 시작
- `IN_PROGRESS`: Saga 실행 중
- `COMPLETED`: Saga 성공 완료
- `COMPENSATING`: 보상 트랜잭션 실행 중
- `COMPENSATED`: 보상 트랜잭션 완료 (롤백 성공)
- `FAILED`: 보상 트랜잭션 실패

### StepStatus

- `PENDING`: 단계 대기 중
- `RUNNING`: 단계 실행 중
- `SUCCESS`: 단계 성공
- `FAILED`: 단계 실패
- `COMPENSATING`: 보상 트랜잭션 실행 중
- `COMPENSATED`: 보상 완료

## REST API

Saga 모니터링 및 관리를 위한 REST API를 제공합니다.

### 실행 상태 조회

```http
GET /api/saga/executions/{executionId}
```

**응답:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "sagaName": "order-saga",
  "traceId": "trace-123",
  "status": "COMPLETED",
  "currentStep": 3,
  "totalSteps": 3,
  "startedAt": "2024-01-01T10:00:00Z",
  "completedAt": "2024-01-01T10:00:05Z",
  "retryCount": 0,
  "steps": [
    {
      "order": 1,
      "name": "reserve-inventory",
      "status": "SUCCESS",
      "retryCount": 0
    },
    {
      "order": 2,
      "name": "process-payment",
      "status": "SUCCESS",
      "retryCount": 1
    },
    {
      "order": 3,
      "name": "create-order",
      "status": "SUCCESS",
      "retryCount": 0
    }
  ]
}
```

### 실행 목록 조회

```http
# TraceId로 조회
GET /api/saga/executions?traceId=trace-123

# Saga 이름과 상태로 조회
GET /api/saga/executions?sagaName=order-saga&status=FAILED

# 상태로만 조회
GET /api/saga/executions?status=COMPENSATING
```

### 통계 조회

```http
GET /api/saga/stats
```

**응답:**
```json
{
  "startedCount": 5,
  "in_progressCount": 2,
  "completedCount": 100,
  "compensatingCount": 1,
  "compensatedCount": 10,
  "failedCount": 3
}
```

### 실패한 Saga 재시도

```http
POST /api/saga/executions/{executionId}/retry
```

### Saga 복구

```http
POST /api/saga/executions/{executionId}/recover
```

### Saga 취소

```http
POST /api/saga/executions/{executionId}/cancel
```

## 복구 기능

### 자동 복구

설정된 주기마다 실패한 Saga를 자동으로 복구 시도합니다.

```yaml
eraf:
  saga:
    recovery:
      auto-recovery-enabled: true
      recovery-cron: "0 */5 * * * ?"  # 매 5분
      max-recovery-attempts: 3
```

### 수동 복구

```java
@Service
public class SagaAdminService {

    private final SagaRecoveryService recoveryService;

    public void retryFailedSaga(String executionId) {
        // 실패한 Saga 재시도
        SagaExecution execution = recoveryService.retry(executionId);
    }

    public void recoverSaga(String executionId) {
        // Saga 복구 (보상 트랜잭션 재실행)
        SagaExecution execution = recoveryService.recover(executionId);
    }

    public void cancelSaga(String executionId) {
        // Saga 취소 (보상 트랜잭션 실행)
        SagaExecution execution = recoveryService.cancel(executionId);
    }
}
```

## 클린업

완료된 Saga 실행 이력을 자동으로 정리합니다.

```yaml
eraf:
  saga:
    cleanup-enabled: true
    cleanup-cron: "0 0 3 * * ?"  # 매일 새벽 3시
    retention-days: 7             # 7일 이상 된 이력 삭제
```

`COMPLETED` 및 `COMPENSATED` 상태의 Saga만 삭제됩니다. `FAILED`, `COMPENSATING` 상태는 유지됩니다.

## 이벤트

Saga 실행 중 다음 이벤트가 발행됩니다:

- `SagaStartedEvent`: Saga 시작
- `SagaCompletedEvent`: Saga 완료
- `SagaFailedEvent`: Saga 실패
- `SagaCompensatedEvent`: 보상 완료
- `StepStartedEvent`: 단계 시작
- `StepCompletedEvent`: 단계 완료
- `StepFailedEvent`: 단계 실패

### Spring Events 리스너

```java
import com.eraf.saga.event.SagaCompletedEvent;
import com.eraf.saga.event.SagaFailedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SagaEventListener {

    @EventListener
    public void onSagaCompleted(SagaCompletedEvent event) {
        log.info("Saga completed: {} (id={})",
            event.getSagaName(), event.getExecutionId());
    }

    @EventListener
    public void onSagaFailed(SagaFailedEvent event) {
        log.error("Saga failed: {} (id={}), reason: {}",
            event.getSagaName(), event.getExecutionId(), event.getReason());

        // 알림 전송, 모니터링 등
        notificationService.sendAlert(event);
    }
}
```

## 고급 사용법

### 조건부 단계 실행

```java
@SagaStep(order = 2, name = "send-notification")
public void sendNotification(OrderRequest request, SagaContext context) {
    // Context에서 이전 단계 결과 확인
    Boolean isVipCustomer = context.get("isVipCustomer");

    if (Boolean.TRUE.equals(isVipCustomer)) {
        // VIP 고객에게만 SMS 전송
        smsService.send(request.getPhoneNumber(), "Order confirmed!");
    }
}
```

### 병렬 단계 실행 (향후 지원 예정)

현재는 순차 실행만 지원하며, 병렬 실행은 향후 버전에서 지원 예정입니다.

### 타임아웃 설정

```java
@Saga(name = "payment-saga", timeout = 60000) // Saga 전체 타임아웃 60초
@Component
public class PaymentSaga {

    @SagaStep(order = 1, name = "authorize", timeout = 10000) // 단계별 타임아웃 10초
    public void authorize(PaymentRequest request) {
        // 결제 승인
    }
}
```

## 모범 사례

### 1. 멱등성(Idempotency) 보장

각 단계와 보상 트랜잭션은 멱등성을 보장해야 합니다.

```java
@SagaStep(order = 1, name = "reserve-inventory")
public void reserveInventory(OrderRequest request, SagaContext context) {
    // 이미 예약되었는지 확인 (멱등성)
    String reservationId = "R-" + request.getOrderId();
    if (inventoryService.isReserved(reservationId)) {
        log.info("Already reserved: {}", reservationId);
        return;
    }

    inventoryService.reserve(reservationId, request.getProductId(), request.getQuantity());
    context.put("reservationId", reservationId);
}
```

### 2. 보상 트랜잭션 설계

모든 중요한 단계에는 보상 트랜잭션을 정의해야 합니다.

```java
@SagaStep(order = 1, name = "charge-payment", compensate = "refundPayment")
public void chargePayment(OrderRequest request, SagaContext context) {
    String paymentId = paymentService.charge(request);
    context.put("paymentId", paymentId);
}

@Compensate
public void refundPayment(SagaContext context) {
    String paymentId = context.get("paymentId");
    if (paymentId != null) {
        paymentService.refund(paymentId);
    }
}
```

### 3. Context 활용

단계 간 데이터 공유는 Context를 통해 수행합니다.

```java
@SagaStep(order = 1, name = "validate")
public void validate(OrderRequest request, SagaContext context) {
    Customer customer = customerService.findById(request.getCustomerId());
    context.put("customer", customer);
    context.put("isVipCustomer", customer.isVip());
}

@SagaStep(order = 2, name = "calculate-discount")
public void calculateDiscount(OrderRequest request, SagaContext context) {
    Boolean isVipCustomer = context.get("isVipCustomer");
    double discount = isVipCustomer ? 0.2 : 0.0;
    context.put("discount", discount);
}
```

### 4. TraceId 활용

분산 추적을 위해 TraceId를 전달합니다.

```java
public void createOrder(OrderRequest request) {
    String traceId = MDC.get("traceId"); // 또는 다른 추적 시스템
    sagaOrchestrator.execute("order-saga", request, traceId);
}
```

### 5. 재시도 전략

중요한 단계는 재시도를 설정합니다.

```java
@SagaStep(
    order = 1,
    name = "external-api-call",
    retries = 5,           // 최대 5번 재시도
    retryDelay = 2000      // 2초 간격
)
public void callExternalApi(Request request) {
    // 외부 API 호출 (일시적 실패 가능)
    externalService.call(request);
}
```

## 제약사항

- 현재 순차 실행만 지원 (병렬 실행 미지원)
- 타임아웃 기능은 구현 예정
- Saga 정의는 Spring Bean으로 등록되어야 함
- 보상 트랜잭션이 실패하면 Saga는 `FAILED` 상태가 됨

## 참고 자료

- [Saga Pattern - Microsoft](https://learn.microsoft.com/azure/architecture/reference-architectures/saga/saga)
- [Pattern: Saga - Microservices.io](https://microservices.io/patterns/data/saga.html)
- ERAF Platform 개발 로드맵: `/docs/eraf-platform-roadmap.xlsx`

## 라이선스

Copyright 2024 ERAF Platform
