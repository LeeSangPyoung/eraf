# ERAF Workflow Engine

간단하고 유연한 워크플로우 엔진 모듈입니다.

## 개요

복잡한 비즈니스 프로세스를 단계별로 나누어 실행하고 관리할 수 있는 경량 워크플로우 엔진입니다.

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-workflow</artifactId>
</dependency>
```

## 핵심 개념

- **WorkflowStep**: 단일 작업 단위
- **WorkflowEngine**: Step들을 순차 실행하는 엔진
- **Context**: Step 간 데이터 공유를 위한 Map

## 사용법

### 1. 기본 워크플로우

```java
@Service
public class OrderProcessingService {

    public void processOrder(Order order) {
        WorkflowEngine workflow = new WorkflowEngine();
        Map<String, Object> context = new HashMap<>();
        context.put("order", order);

        // Step 1: 재고 확인
        workflow.addStep("validate-inventory", ctx -> {
            Order ord = (Order) ctx.get("order");
            boolean available = inventoryService.checkAvailability(ord);
            ctx.put("inventory-available", available);
            return available;
        });

        // Step 2: 결제 처리
        workflow.addStep("process-payment", ctx -> {
            Order ord = (Order) ctx.get("order");
            Payment payment = paymentService.charge(ord);
            ctx.put("payment", payment);
            return payment.isSuccess();
        });

        // Step 3: 배송 준비
        workflow.addStep("prepare-shipment", ctx -> {
            Order ord = (Order) ctx.get("order");
            Shipment shipment = shippingService.prepare(ord);
            ctx.put("shipment", shipment);
            return true;
        });

        // Step 4: 완료 처리
        workflow.addStep("complete-order", ctx -> {
            Order ord = (Order) ctx.get("order");
            ord.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(ord);
            return true;
        });

        // 워크플로우 실행
        boolean success = workflow.execute(context);

        if (!success) {
            handleFailure(context);
        }
    }
}
```

### 2. 조건부 실행

```java
WorkflowEngine workflow = new WorkflowEngine();

workflow.addStep("check-premium", ctx -> {
    User user = (User) ctx.get("user");
    boolean isPremium = user.isPremiumMember();
    ctx.put("is-premium", isPremium);
    return true;
});

workflow.addStep("apply-discount", ctx -> {
    Boolean isPremium = (Boolean) ctx.get("is-premium");
    if (isPremium) {
        Order order = (Order) ctx.get("order");
        order.applyDiscount(0.2); // 20% 할인
    }
    return true;
});
```

### 3. 에러 처리

```java
WorkflowEngine workflow = new WorkflowEngine();

workflow.addStep("risky-operation", ctx -> {
    try {
        externalService.call();
        return true;
    } catch (Exception e) {
        ctx.put("error", e.getMessage());
        return false; // 실패 시 워크플로우 중단
    }
});

workflow.addStep("this-wont-execute-on-error", ctx -> {
    // 이전 Step이 실패하면 실행되지 않음
    return true;
});

boolean success = workflow.execute(context);

if (!success) {
    String error = (String) context.get("error");
    log.error("Workflow failed: {}", error);
}
```

### 4. 복잡한 예시 - 사용자 등록 프로세스

```java
@Service
@RequiredArgsConstructor
public class UserRegistrationWorkflow {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public boolean registerUser(UserRegistrationRequest request) {
        WorkflowEngine workflow = new WorkflowEngine();
        Map<String, Object> context = new HashMap<>();
        context.put("request", request);

        // Step 1: 입력 검증
        workflow.addStep("validate-input", ctx -> {
            UserRegistrationRequest req = (UserRegistrationRequest) ctx.get("request");
            if (req.getEmail() == null || req.getPassword() == null) {
                ctx.put("error", "Missing required fields");
                return false;
            }
            return true;
        });

        // Step 2: 중복 사용자 확인
        workflow.addStep("check-duplicate", ctx -> {
            UserRegistrationRequest req = (UserRegistrationRequest) ctx.get("request");
            if (userRepository.existsByEmail(req.getEmail())) {
                ctx.put("error", "Email already registered");
                return false;
            }
            return true;
        });

        // Step 3: 사용자 생성
        workflow.addStep("create-user", ctx -> {
            UserRegistrationRequest req = (UserRegistrationRequest) ctx.get("request");
            User user = new User(req.getEmail(), req.getPassword());
            user = userRepository.save(user);
            ctx.put("user", user);
            return true;
        });

        // Step 4: 환영 이메일 발송
        workflow.addStep("send-welcome-email", ctx -> {
            User user = (User) ctx.get("user");
            try {
                emailService.sendWelcomeEmail(user.getEmail());
                return true;
            } catch (Exception e) {
                // 이메일 실패해도 계속 진행
                log.warn("Failed to send welcome email", e);
                return true;
            }
        });

        // Step 5: Slack 알림
        workflow.addStep("notify-admins", ctx -> {
            User user = (User) ctx.get("user");
            notificationService.notifyNewUser(user);
            return true;
        });

        // Step 6: 감사 로그 기록
        workflow.addStep("audit-log", ctx -> {
            User user = (User) ctx.get("user");
            auditService.log("USER_REGISTERED", user.getId());
            return true;
        });

        return workflow.execute(context);
    }
}
```

### 5. Step 재사용

```java
public class CommonSteps {

    public static WorkflowStep validateUser() {
        return ctx -> {
            User user = (User) ctx.get("user");
            if (user == null || !user.isActive()) {
                ctx.put("error", "Invalid user");
                return false;
            }
            return true;
        };
    }

    public static WorkflowStep logExecution(String message) {
        return ctx -> {
            log.info(message, ctx);
            return true;
        };
    }

    public static WorkflowStep sendNotification(String template) {
        return ctx -> {
            User user = (User) ctx.get("user");
            notificationService.send(user, template, ctx);
            return true;
        };
    }
}

// 사용
workflow.addStep("validate", CommonSteps.validateUser());
workflow.addStep("log", CommonSteps.logExecution("Processing order"));
workflow.addStep("notify", CommonSteps.sendNotification("order-confirmed"));
```

## Context 활용

```java
// 데이터 저장
context.put("key", value);

// 데이터 조회
String value = (String) context.get("key");

// 타입 안전하게 사용
record WorkflowContext(
    Order order,
    Payment payment,
    Shipment shipment
) {}

// 또는 전용 Context 클래스 생성
public class OrderWorkflowContext {
    private Order order;
    private Payment payment;
    private boolean inventoryAvailable;

    // getters, setters
}
```

## 고급 기능 (향후 확장)

```java
// 1. 조건부 분기
workflow.addStep("branch", ctx -> {
    return condition ? workflow.executeSubflow(branchA)
                     : workflow.executeSubflow(branchB);
});

// 2. 병렬 실행
workflow.addParallelStep(Arrays.asList(
    step1, step2, step3
));

// 3. 재시도
workflow.addStep("retry-operation", ctx -> {
    return retry(3, () -> externalService.call());
});

// 4. 보상 트랜잭션 (Saga Pattern)
workflow.addStep("create-order", ctx -> {
    ctx.put("compensate", () -> orderRepository.delete(order));
    return createOrder();
});
```

## 참고

- [Microservices Pattern: Saga](https://microservices.io/patterns/data/saga.html)
- [Workflow Patterns](http://www.workflowpatterns.com/)
