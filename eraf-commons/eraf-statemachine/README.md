# ERAF StateMachine

상태 머신 패턴을 위한 선언적 상태 관리 라이브러리

## 개요

ERAF StateMachine은 Spring 기반의 선언적 상태 머신 구현을 제공합니다. 복잡한 워크플로우, 주문 처리, 승인 프로세스 등 상태 기반 비즈니스 로직을 쉽게 구현할 수 있습니다.

### 주요 기능

- **선언적 정의**: `@StateMachine`, `@Transition` 어노테이션 기반
- **이벤트 기반 상태 전이**: 이벤트 전송으로 상태 자동 변경
- **가드 조건**: SpEL 표현식으로 전이 조건 검증
- **액션 실행**: 상태 전이 시 자동으로 액션 메서드 실행
- **상태 저장소**: InMemory, Redis, JDBC 지원
- **컨텍스트 관리**: 상태 간 데이터 공유
- **이벤트 발행**: Spring Events로 상태 변경 알림
- **이력 추적**: 이전 상태 및 변경 시간 추적

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-statemachine</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 빠른 시작

### 1. 상태 머신 정의

```java
import com.eraf.statemachine.StateMachine;
import com.eraf.statemachine.Transition;
import com.eraf.statemachine.StateInfo;
import org.springframework.stereotype.Component;

@StateMachine(
    id = "order-machine",
    initialState = "CREATED",
    states = {"CREATED", "PAID", "SHIPPED", "DELIVERED", "CANCELLED"},
    endStates = {"DELIVERED", "CANCELLED"},
    description = "주문 상태 머신"
)
@Component
public class OrderStateMachine {

    /**
     * 결제 완료 전이
     */
    @Transition(
        event = "PAY",
        source = "CREATED",
        target = "PAID",
        description = "주문 결제 완료"
    )
    public void onPayment(StateInfo stateInfo, Map<String, Object> eventContext) {
        log.info("Payment processed for order: {}", stateInfo.getEntityId());

        // 결제 처리 로직
        String paymentId = (String) eventContext.get("paymentId");
        stateInfo.getContext().put("paymentId", paymentId);

        // 추가 비즈니스 로직
        notificationService.sendPaymentConfirmation(stateInfo.getEntityId());
    }

    /**
     * 배송 시작 전이
     */
    @Transition(
        event = "SHIP",
        source = "PAID",
        target = "SHIPPED",
        description = "배송 시작"
    )
    public void onShipping(StateInfo stateInfo, Map<String, Object> eventContext) {
        log.info("Shipping started for order: {}", stateInfo.getEntityId());

        String trackingNumber = (String) eventContext.get("trackingNumber");
        stateInfo.getContext().put("trackingNumber", trackingNumber);

        // 배송 추적 번호 전송
        notificationService.sendTrackingNumber(
            stateInfo.getEntityId(),
            trackingNumber
        );
    }

    /**
     * 배송 완료 전이
     */
    @Transition(
        event = "DELIVER",
        source = "SHIPPED",
        target = "DELIVERED",
        description = "배송 완료"
    )
    public void onDelivered(StateInfo stateInfo) {
        log.info("Order delivered: {}", stateInfo.getEntityId());

        // 배송 완료 처리
        orderService.completeOrder(stateInfo.getEntityId());
    }

    /**
     * 주문 취소 전이 (여러 상태에서 가능)
     */
    @Transition(
        event = "CANCEL",
        source = "CREATED",
        target = "CANCELLED",
        description = "결제 전 취소"
    )
    @Transition(
        event = "CANCEL",
        source = "PAID",
        target = "CANCELLED",
        description = "결제 후 취소 및 환불",
        guard = "#context['refundable'] == true"
    )
    public void onCancel(StateInfo stateInfo) {
        log.info("Order cancelled: {}", stateInfo.getEntityId());

        String currentState = stateInfo.getPreviousState();

        // 결제 후 취소인 경우 환불 처리
        if ("PAID".equals(currentState)) {
            String paymentId = (String) stateInfo.getContext().get("paymentId");
            paymentService.refund(paymentId);
        }
    }
}
```

### 2. 상태 머신 사용

```java
import com.eraf.statemachine.ErafStateMachineService;
import com.eraf.statemachine.StateInfo;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final ErafStateMachineService stateMachineService;

    public OrderService(ErafStateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    public Order createOrder(OrderRequest request) {
        Order order = orderRepository.save(new Order(request));

        // 상태 머신 초기화
        stateMachineService.initialize("order-machine", order.getId().toString());

        return order;
    }

    public void processPayment(Long orderId, String paymentId) {
        // 결제 이벤트 전송
        Map<String, Object> eventContext = Map.of("paymentId", paymentId);
        StateInfo stateInfo = stateMachineService.sendEvent(
            "order-machine",
            orderId.toString(),
            "PAY",
            eventContext
        );

        log.info("Order state: {}", stateInfo.getCurrentState());
    }

    public void ship(Long orderId, String trackingNumber) {
        Map<String, Object> eventContext = Map.of("trackingNumber", trackingNumber);
        stateMachineService.sendEvent(
            "order-machine",
            orderId.toString(),
            "SHIP",
            eventContext
        );
    }

    public void deliver(Long orderId) {
        stateMachineService.sendEvent(
            "order-machine",
            orderId.toString(),
            "DELIVER"
        );
    }

    public void cancel(Long orderId) {
        // 취소 가능 여부 체크
        if (stateMachineService.canSendEvent("order-machine", orderId.toString(), "CANCEL")) {
            stateMachineService.sendEvent(
                "order-machine",
                orderId.toString(),
                "CANCEL"
            );
        } else {
            throw new IllegalStateException("Order cannot be cancelled in current state");
        }
    }

    public String getOrderState(Long orderId) {
        return stateMachineService.getCurrentState(
            "order-machine",
            orderId.toString()
        ).orElse("NOT_FOUND");
    }

    public List<String> getAvailableActions(Long orderId) {
        return stateMachineService.getAvailableEvents(
            "order-machine",
            orderId.toString()
        );
    }
}
```

### 3. 상태 조회 및 관리

```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final ErafStateMachineService stateMachineService;

    @GetMapping("/{id}/state")
    public ResponseEntity<StateResponse> getState(@PathVariable Long id) {
        Optional<StateInfo> stateInfo = stateMachineService.getState(
            "order-machine",
            id.toString()
        );

        return stateInfo
            .map(info -> new StateResponse(
                info.getCurrentState(),
                info.getPreviousState(),
                info.getStateChangedAt(),
                info.getContext()
            ))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/available-actions")
    public List<String> getAvailableActions(@PathVariable Long id) {
        return stateMachineService.getAvailableEvents(
            "order-machine",
            id.toString()
        );
    }

    @GetMapping("/{id}/can-cancel")
    public boolean canCancel(@PathVariable Long id) {
        return stateMachineService.canSendEvent(
            "order-machine",
            id.toString(),
            "CANCEL"
        );
    }
}
```

## 설정

### application.yml

```yaml
eraf:
  statemachine:
    # 상태 머신 모듈 활성화 (기본: true)
    enabled: true

    # 상태 저장소 타입 (memory, redis, jdbc)
    store-type: memory

    # 상태 TTL (Redis 사용 시)
    state-ttl: 7d

    # 자동 테이블 생성 (JDBC 사용 시)
    auto-create-table: true
```

### Redis 저장소 사용

```yaml
eraf:
  statemachine:
    store-type: redis
    state-ttl: 30d

spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### JDBC 저장소 사용

```yaml
eraf:
  statemachine:
    store-type: jdbc
    auto-create-table: true

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: user
    password: pass
```

## 어노테이션

### @StateMachine

클래스에 적용하여 상태 머신을 정의합니다.

```java
@StateMachine(
    id = "order-machine",           // 상태 머신 ID (필수, 고유 식별자)
    initialState = "CREATED",        // 초기 상태 (필수)
    states = {"CREATED", "PAID", "SHIPPED", "DELIVERED", "CANCELLED"}, // 상태 목록 (필수)
    endStates = {"DELIVERED", "CANCELLED"}, // 종료 상태 목록
    description = "주문 상태 머신"    // 설명
)
```

### @Transition

메서드에 적용하여 상태 전이 및 액션을 정의합니다.

```java
@Transition(
    event = "PAY",                   // 이벤트 이름 (필수)
    source = "CREATED",              // 소스 상태 (필수)
    target = "PAID",                 // 타겟 상태 (필수)
    guard = "#context['amount'] > 0", // 가드 조건 (SpEL 표현식)
    description = "결제 완료"         // 설명
)
```

### @Transitions

하나의 메서드에 여러 전이를 정의할 때 사용합니다.

```java
@Transition(event = "CANCEL", source = "CREATED", target = "CANCELLED")
@Transition(event = "CANCEL", source = "PAID", target = "CANCELLED")
@Transition(event = "CANCEL", source = "SHIPPED", target = "CANCELLED")
public void handleCancel(StateInfo stateInfo) {
    // 여러 상태에서 공통 취소 로직
}
```

## 가드 조건 (Guard)

SpEL 표현식을 사용하여 전이 조건을 검증합니다.

### 사용 가능한 변수

- `#state`: `StateInfo` 객체 (현재 상태 정보)
- `#context`: 상태 컨텍스트 맵
- `#event`: 이벤트 컨텍스트 맵

### 예제

```java
// 컨텍스트 값 체크
@Transition(
    event = "APPROVE",
    source = "PENDING",
    target = "APPROVED",
    guard = "#context['amount'] < 1000000"
)

// 이벤트 데이터 체크
@Transition(
    event = "SUBMIT",
    source = "DRAFT",
    target = "SUBMITTED",
    guard = "#event['approved'] == true"
)

// 복합 조건
@Transition(
    event = "ESCALATE",
    source = "REVIEW",
    target = "ESCALATED",
    guard = "#context['priority'] == 'HIGH' and #context['attempts'] > 3"
)

// null 체크
@Transition(
    event = "REFUND",
    source = "PAID",
    target = "REFUNDED",
    guard = "#context['paymentId'] != null and #context['refundable'] == true"
)
```

## StateInfo

상태 정보 및 컨텍스트를 담는 객체입니다.

```java
public class StateInfo {
    private String machineId;           // 상태 머신 ID
    private String entityId;            // 엔티티 ID
    private String currentState;        // 현재 상태
    private String previousState;       // 이전 상태
    private Instant stateChangedAt;     // 상태 변경 시간
    private Map<String, Object> context; // 컨텍스트 데이터
}
```

### 컨텍스트 사용 예제

```java
@Transition(event = "PAY", source = "CREATED", target = "PAID")
public void onPayment(StateInfo stateInfo, Map<String, Object> eventContext) {
    // 이벤트 데이터에서 가져오기
    String paymentId = (String) eventContext.get("paymentId");
    Double amount = (Double) eventContext.get("amount");

    // 컨텍스트에 저장 (다른 전이에서 사용 가능)
    stateInfo.getContext().put("paymentId", paymentId);
    stateInfo.getContext().put("paidAmount", amount);
    stateInfo.getContext().put("paidAt", Instant.now());
}

@Transition(event = "REFUND", source = "PAID", target = "REFUNDED")
public void onRefund(StateInfo stateInfo) {
    // 이전 전이에서 저장한 데이터 가져오기
    String paymentId = (String) stateInfo.getContext().get("paymentId");
    Double amount = (Double) stateInfo.getContext().get("paidAmount");

    // 환불 처리
    paymentService.refund(paymentId, amount);
}
```

## 액션 메서드 시그니처

전이 액션 메서드는 다음 형식을 지원합니다:

```java
// 파라미터 없음
@Transition(event = "START", source = "IDLE", target = "RUNNING")
public void onStart() {
    // 액션 로직
}

// StateInfo만
@Transition(event = "COMPLETE", source = "RUNNING", target = "COMPLETED")
public void onComplete(StateInfo stateInfo) {
    String entityId = stateInfo.getEntityId();
}

// 이벤트 컨텍스트만
@Transition(event = "UPDATE", source = "DRAFT", target = "UPDATED")
public void onUpdate(Map<String, Object> eventContext) {
    String data = (String) eventContext.get("data");
}

// StateInfo + 이벤트 컨텍스트
@Transition(event = "PROCESS", source = "PENDING", target = "PROCESSED")
public void onProcess(StateInfo stateInfo, Map<String, Object> eventContext) {
    // 둘 다 사용
}
```

## 이벤트

상태 변경 시 `StateChangeEvent`가 자동으로 발행됩니다.

```java
import com.eraf.statemachine.StateChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StateChangeListener {

    @EventListener
    public void onStateChange(StateChangeEvent event) {
        log.info("State changed: machineId={}, entityId={}, {} -> {} (event={})",
            event.getMachineId(),
            event.getEntityId(),
            event.getFromState(),
            event.getToState(),
            event.getEvent()
        );

        // 상태 변경 알림
        if ("order-machine".equals(event.getMachineId())) {
            notificationService.notifyStateChange(
                event.getEntityId(),
                event.getToState()
            );
        }

        // 외부 시스템 연동
        if ("COMPLETED".equals(event.getToState())) {
            externalSystemService.notifyCompletion(event.getEntityId());
        }
    }
}
```

## 고급 사용법

### 1. 초기화 시 컨텍스트 설정

```java
public Order createOrder(OrderRequest request) {
    Order order = orderRepository.save(new Order(request));

    // 초기 컨텍스트 설정
    Map<String, Object> initialContext = Map.of(
        "customerId", request.getCustomerId(),
        "amount", request.getTotalAmount(),
        "priority", request.getPriority(),
        "refundable", true
    );

    stateMachineService.initialize(
        "order-machine",
        order.getId().toString(),
        initialContext
    );

    return order;
}
```

### 2. 동적 가드 조건

```java
@Transition(
    event = "APPROVE",
    source = "PENDING",
    target = "APPROVED",
    guard = "#context['amount'] < 1000000 or #context['managerApproved'] == true"
)
public void onApprove(StateInfo stateInfo, Map<String, Object> eventContext) {
    // 소액이거나 매니저 승인이 있으면 자동 승인
    Boolean managerApproved = (Boolean) eventContext.get("managerApproved");

    if (Boolean.TRUE.equals(managerApproved)) {
        stateInfo.getContext().put("approvedBy", "MANAGER");
    } else {
        stateInfo.getContext().put("approvedBy", "AUTO");
    }
}
```

### 3. 종료 상태 확인

```java
public void processOrder(Long orderId) {
    String entityId = orderId.toString();

    // 종료 상태 도달 여부 확인
    if (stateMachineService.isInEndState("order-machine", entityId)) {
        log.info("Order {} is in end state, no further processing", orderId);
        return;
    }

    // 다음 작업 수행
    List<String> availableEvents = stateMachineService.getAvailableEvents(
        "order-machine",
        entityId
    );

    log.info("Available actions: {}", availableEvents);
}
```

### 4. 상태 강제 변경 (관리자용)

```java
@PreAuthorize("hasRole('ADMIN')")
public void forceState(Long orderId, String targetState) {
    stateMachineService.forceState(
        "order-machine",
        orderId.toString(),
        targetState
    );

    log.warn("Order {} state forced to {}", orderId, targetState);
}
```

### 5. 분산 환경에서 Redis 사용

```java
// application.yml
eraf:
  statemachine:
    store-type: redis
    state-ttl: 30d

// 여러 서버 인스턴스에서 동일한 상태 공유
// Redis를 사용하면 상태가 분산 저장되어 서버 재시작 후에도 유지됨
```

## 실전 예제

### 승인 워크플로우

```java
@StateMachine(
    id = "approval-workflow",
    initialState = "DRAFT",
    states = {"DRAFT", "SUBMITTED", "REVIEWING", "APPROVED", "REJECTED", "CANCELLED"},
    endStates = {"APPROVED", "REJECTED", "CANCELLED"}
)
@Component
public class ApprovalWorkflow {

    @Transition(event = "SUBMIT", source = "DRAFT", target = "SUBMITTED")
    public void onSubmit(StateInfo stateInfo, Map<String, Object> eventContext) {
        stateInfo.getContext().put("submittedBy", eventContext.get("userId"));
        stateInfo.getContext().put("submittedAt", Instant.now());
    }

    @Transition(
        event = "START_REVIEW",
        source = "SUBMITTED",
        target = "REVIEWING"
    )
    public void onStartReview(StateInfo stateInfo, Map<String, Object> eventContext) {
        String reviewerId = (String) eventContext.get("reviewerId");
        stateInfo.getContext().put("reviewerId", reviewerId);
        stateInfo.getContext().put("reviewStartedAt", Instant.now());
    }

    @Transition(
        event = "APPROVE",
        source = "REVIEWING",
        target = "APPROVED",
        guard = "#event['authorized'] == true"
    )
    public void onApprove(StateInfo stateInfo, Map<String, Object> eventContext) {
        stateInfo.getContext().put("approvedBy", eventContext.get("userId"));
        stateInfo.getContext().put("approvedAt", Instant.now());
        stateInfo.getContext().put("comments", eventContext.get("comments"));
    }

    @Transition(
        event = "REJECT",
        source = "REVIEWING",
        target = "REJECTED"
    )
    public void onReject(StateInfo stateInfo, Map<String, Object> eventContext) {
        stateInfo.getContext().put("rejectedBy", eventContext.get("userId"));
        stateInfo.getContext().put("rejectedAt", Instant.now());
        stateInfo.getContext().put("reason", eventContext.get("reason"));
    }

    @Transition(event = "CANCEL", source = "DRAFT", target = "CANCELLED")
    @Transition(event = "CANCEL", source = "SUBMITTED", target = "CANCELLED")
    public void onCancel(StateInfo stateInfo, Map<String, Object> eventContext) {
        stateInfo.getContext().put("cancelledBy", eventContext.get("userId"));
        stateInfo.getContext().put("cancelledAt", Instant.now());
    }
}
```

### 티켓 처리 시스템

```java
@StateMachine(
    id = "ticket-machine",
    initialState = "OPEN",
    states = {"OPEN", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED", "REOPENED"},
    endStates = {"CLOSED"}
)
@Component
public class TicketStateMachine {

    @Transition(event = "ASSIGN", source = "OPEN", target = "ASSIGNED")
    @Transition(event = "ASSIGN", source = "REOPENED", target = "ASSIGNED")
    public void onAssign(StateInfo stateInfo, Map<String, Object> eventContext) {
        stateInfo.getContext().put("assignedTo", eventContext.get("assigneeId"));
        stateInfo.getContext().put("assignedAt", Instant.now());
    }

    @Transition(event = "START", source = "ASSIGNED", target = "IN_PROGRESS")
    public void onStart(StateInfo stateInfo) {
        stateInfo.getContext().put("startedAt", Instant.now());
    }

    @Transition(
        event = "RESOLVE",
        source = "IN_PROGRESS",
        target = "RESOLVED"
    )
    public void onResolve(StateInfo stateInfo, Map<String, Object> eventContext) {
        stateInfo.getContext().put("resolution", eventContext.get("resolution"));
        stateInfo.getContext().put("resolvedAt", Instant.now());
    }

    @Transition(event = "CLOSE", source = "RESOLVED", target = "CLOSED")
    public void onClose(StateInfo stateInfo, Map<String, Object> eventContext) {
        stateInfo.getContext().put("closedBy", eventContext.get("userId"));
        stateInfo.getContext().put("closedAt", Instant.now());
        stateInfo.getContext().put("satisfaction", eventContext.get("satisfaction"));
    }

    @Transition(event = "REOPEN", source = "CLOSED", target = "REOPENED")
    public void onReopen(StateInfo stateInfo, Map<String, Object> eventContext) {
        stateInfo.getContext().put("reopenedBy", eventContext.get("userId"));
        stateInfo.getContext().put("reopenedAt", Instant.now());
        stateInfo.getContext().put("reopenReason", eventContext.get("reason"));
    }
}
```

## 저장소 타입 비교

| 타입 | 장점 | 단점 | 사용 사례 |
|------|------|------|----------|
| **InMemory** | - 빠른 속도<br>- 추가 인프라 불필요 | - 서버 재시작 시 손실<br>- 단일 인스턴스만 사용 | 개발/테스트 환경, 임시 워크플로우 |
| **Redis** | - 분산 환경 지원<br>- 서버 재시작 후 유지<br>- TTL 지원 | - Redis 인프라 필요<br>- 네트워크 오버헤드 | 프로덕션 환경, 분산 시스템, 세션 기반 워크플로우 |
| **JDBC** | - 영구 저장<br>- 트랜잭션 지원<br>- 복잡한 쿼리 가능 | - DB 의존성<br>- 상대적으로 느림 | 감사 추적이 중요한 경우, 장기 보관 필요 |

## 모범 사례

### 1. 명확한 상태 정의

```java
// 좋은 예: 명확하고 구체적인 상태
states = {"DRAFT", "SUBMITTED", "REVIEWING", "APPROVED", "REJECTED"}

// 나쁜 예: 모호한 상태
states = {"INIT", "PROCESSING", "DONE"}
```

### 2. 종료 상태 명시

```java
@StateMachine(
    id = "process-machine",
    initialState = "START",
    states = {"START", "RUNNING", "SUCCESS", "FAILED", "CANCELLED"},
    endStates = {"SUCCESS", "FAILED", "CANCELLED"} // 명시적으로 종료 상태 선언
)
```

### 3. 가드 조건 활용

```java
// 조건부 전이는 가드로 처리
@Transition(
    event = "APPROVE",
    source = "PENDING",
    target = "APPROVED",
    guard = "#context['amount'] < 1000000"
)

// 가드 실패는 예외가 아닌 정상 흐름
```

### 4. 컨텍스트 활용

```java
// 상태 간 데이터 공유
@Transition(event = "SUBMIT", source = "DRAFT", target = "SUBMITTED")
public void onSubmit(StateInfo stateInfo, Map<String, Object> eventContext) {
    // 나중에 사용할 데이터 저장
    stateInfo.getContext().put("submittedBy", eventContext.get("userId"));
    stateInfo.getContext().put("submittedAt", Instant.now());
}
```

### 5. 이벤트 리스닝

```java
// 상태 변경을 모니터링하고 부가 작업 수행
@EventListener
public void onStateChange(StateChangeEvent event) {
    // 로깅, 알림, 외부 시스템 연동 등
}
```

## 제약사항

- 상태 머신 Bean은 `@Component` 등으로 등록되어야 함
- 액션 메서드는 public이어야 함
- 가드 표현식은 SpEL 문법을 따라야 함
- 종료 상태에서는 더 이상 전이 불가
- 동일한 (source, event) 조합은 하나의 전이만 가능 (가드로 구분)

## 참고 자료

- [Spring State Machine](https://spring.io/projects/spring-statemachine)
- [State Machine Pattern](https://refactoring.guru/design-patterns/state)
- ERAF Platform 개발 로드맵: `/docs/eraf-platform-roadmap.xlsx`

## 라이선스

Copyright 2024 ERAF Platform
