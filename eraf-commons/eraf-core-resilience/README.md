# ERAF Core - Resilience

회복탄력성, 락, 멱등성 등 안정성 관련 기능을 제공하는 모듈입니다.

## 📦 주요 기능

### 1. Circuit Breaker
- **@CircuitBreaker**: 서킷 브레이커 패턴
- **CircuitBreakerRegistry**: 서킷 브레이커 관리
- Failure rate 기반 자동 차단/복구

### 2. Rate Limiter
- **@RateLimit**: 요청 속도 제한
- **RateLimiter**: Token bucket 알고리즘
- 초당/분당 요청 수 제한

### 3. Retry
- **@Retry**: 재시도 패턴
- **RetryAspect**: AOP 기반 자동 재시도
- 지수 백오프, 최대 재시도 횟수 설정

### 4. Bulkhead
- **@Bulkhead**: 격벽 패턴
- 동시 실행 수 제한

### 5. Timeout
- **@Timeout**: 타임아웃 설정
- 응답 시간 제한

### 6. Lock
- **@DistributedLock**: 분산 락
- **OptimisticRetry**: 낙관적 락 재시도
- Redis/DB 기반 락 구현

### 7. Idempotency
- **@Idempotent**: 멱등성 보장
- **IdempotencyKeyGenerator**: 멱등성 키 생성
- 중복 요청 방지

## 🔗 의존성

**ERAF 모듈**:
- eraf-core-util (유틸리티)
- eraf-core-exception (예외 처리)
- eraf-core-crypto (해시)

**외부 라이브러리**:
- Spring Boot AOP
- Spring Data Commons (optional)
- Spring TX (optional)

## 📝 사용 예시

### Circuit Breaker
```java
@CircuitBreaker(
    name = "paymentService",
    failureRateThreshold = 50,      // 50% 실패 시 차단
    waitDurationInOpenState = 60000 // 60초 후 복구 시도
)
public Payment processPayment(PaymentRequest request) {
    return externalPaymentApi.process(request);
}
```

### Rate Limiter
```java
@RateLimit(
    permitsPerSecond = 10,   // 초당 10개 요청
    timeout = 5000           // 5초 대기
)
public List<User> getUsers() {
    return userRepository.findAll();
}
```

### Retry
```java
@Retry(
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public String callExternalApi() {
    return restTemplate.getForObject(url, String.class);
}
```

### Distributed Lock
```java
@DistributedLock(
    key = "#productId",
    waitTime = 10,
    leaseTime = 30
)
public void updateStock(Long productId, int quantity) {
    Product product = productRepository.findById(productId);
    product.decreaseStock(quantity);
    productRepository.save(product);
}
```

### Idempotency
```java
@Idempotent(key = "#orderId")
@PostMapping("/orders/{orderId}/pay")
public ApiResponse<Payment> processPayment(
    @PathVariable String orderId,
    @RequestBody PaymentRequest request
) {
    Payment payment = paymentService.process(orderId, request);
    return ApiResponse.success(payment);
}
```

### Optimistic Lock Retry
```java
@OptimisticRetry(maxAttempts = 5)
public void updateUserBalance(Long userId, BigDecimal amount) {
    User user = userRepository.findById(userId);
    user.increaseBalance(amount);
    userRepository.save(user); // OptimisticLockException 발생 시 재시도
}
```

## 🏗️ 주요 클래스/어노테이션

**Resilience Patterns**:
- `@CircuitBreaker`, `@RateLimit`, `@Retry`, `@Bulkhead`, `@Timeout`
- `CircuitBreakerRegistry`, `RateLimiter`

**Lock**:
- `@DistributedLock` - Redis/DB 분산 락
- `@OptimisticRetry` - 낙관적 락 재시도
- `LockService`, `LockRepository`

**Idempotency**:
- `@Idempotent` - 멱등성 보장
- `IdempotencyKeyGenerator` - 키 생성기
- `IdempotentService` - 멱등성 처리

## 📚 패턴 설명

### Circuit Breaker
실패율이 임계값을 초과하면 일정 시간 동안 요청을 차단하여 시스템 보호

### Rate Limiter
단위 시간당 요청 수를 제한하여 과부하 방지

### Retry
일시적인 오류에 대해 자동으로 재시도하여 성공률 향상

### Distributed Lock
분산 환경에서 동시성 제어를 위한 락

### Idempotency
동일한 요청을 여러 번 수행해도 결과가 동일하도록 보장
