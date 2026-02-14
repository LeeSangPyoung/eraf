# ERAF Data Redis

Redis 기반 데이터 액세스 및 캐싱 모듈

## 주요 기능

### 1. 기본 기능

- **Redis Template**: JSON 직렬화/역직렬화 자동 설정
- **분산 락 (Distributed Lock)**: Redis 기반 분산 잠금
- **시퀀스 생성기**: 분산 환경에서 고유 ID 생성
- **멱등성 저장소**: API 멱등성 보장

### 2. 캐시 통계 (Cache Statistics) ✨NEW

캐시 히트율, 미스율, 응답 시간 등 상세 통계 추적

```java
@Autowired
private RedisCacheStatistics statistics;

// 캐시 히트 기록
statistics.recordHit("users");

// 전역 통계 조회
Map<String, Object> stats = statistics.getGlobalStatistics();
// => {totalHits: 1000, totalMisses: 50, hitRate: 95.2, ...}

// 특정 캐시 통계
Map<String, Object> userStats = statistics.getCacheStatistics("users");
// => {hits: 500, misses: 25, hitRate: 95.2, averageLatencyMs: 12.5, ...}
```

**추적 항목:**
- 히트/미스 카운트
- 히트율/미스율
- 평균 응답 시간
- 총 저장 크기
- 제거(eviction) 횟수

### 3. 캐시 워밍 (Cache Warming) ✨NEW

애플리케이션 시작 시 자주 사용되는 데이터를 미리 캐시에 로드

```java
@Component
public class MyCacheWarmer {
    @Autowired
    private CacheWarmer cacheWarmer;

    @PostConstruct
    public void registerWarmers() {
        // 사용자 데이터 워밍
        cacheWarmer.register("users", () -> userRepository.findAll());

        // 상품 데이터 워밍 (TTL 지정)
        cacheWarmer.register("products",
            () -> productRepository.findTop100ByOrderBySalesDesc(),
            Duration.ofHours(1));
    }
}
```

**특징:**
- ApplicationReadyEvent 자동 실행
- 병렬 워밍 (기본 4 스레드)
- 수동 워밍 지원
- TTL 지정 가능

### 4. 분산 캐시 무효화 (Distributed Cache Invalidation) ✨NEW

Redis Pub/Sub 기반 멀티 인스턴스 캐시 동기화

```java
@Autowired
private RedisCacheInvalidator invalidator;

// 특정 키 무효화 (모든 인스턴스에 전파)
invalidator.invalidate("users", "user:123");

// 캐시 전체 무효화
invalidator.invalidateAll("products");

// 패턴 기반 무효화 (와일드카드)
invalidator.invalidatePattern("users", "user:admin:*");
```

**동작 원리:**
1. Instance A가 캐시 무효화 → 로컬 캐시 제거 + Pub/Sub 메시지 발행
2. Instance B, C가 Pub/Sub 메시지 수신 → 로컬 캐시 제거
3. 모든 인스턴스 캐시 일관성 유지

### 5. 캐시 모니터링 API ✨NEW

Admin API를 통한 캐시 관리 및 모니터링

```bash
# 전역 통계 조회
GET /admin/cache/statistics/global

# 특정 캐시 통계
GET /admin/cache/statistics/users

# 모든 캐시 통계
GET /admin/cache/statistics/all

# 통계 초기화
DELETE /admin/cache/statistics

# 수동 워밍
POST /admin/cache/warm

# 캐시 무효화
DELETE /admin/cache/invalidate/users
DELETE /admin/cache/invalidate/users/user:123
DELETE /admin/cache/invalidate/users/pattern?keyPattern=user:*

# 헬스 체크
GET /admin/cache/health
```

## 설정

### application.yml

```yaml
eraf:
  redis:
    # 기본 설정
    default-ttl: 1h
    enable-prefix: true
    key-prefix: "eraf:"

    # 분산 락
    lock:
      enabled: true
      default-lease-time: 30s
      default-wait-time: 5s

    # 시퀀스 생성기
    sequence:
      enabled: true

    # 멱등성
    idempotent:
      enabled: true
      ttl: 24h

    # 캐시 통계 ✨NEW
    statistics:
      enabled: true
      async: true              # 비동기 통계 업데이트

    # 캐시 워밍 ✨NEW
    warming:
      enabled: true
      auto-warm-on-startup: true
      parallel-threads: 4      # 병렬 워밍 스레드 수

    # 캐시 무효화 ✨NEW
    invalidation:
      enabled: true
      channel-prefix: "cache:invalidation:"

# Redis 연결
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 3s
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2
          max-wait: -1ms
```

## 사용 예제

### 1. 분산 락

```java
@Autowired
private LockProvider lockProvider;

public void processOrder(Long orderId) {
    String lockKey = "order:lock:" + orderId;

    // 락 획득 시도 (최대 5초 대기, 30초 유지)
    if (lockProvider.tryLock(lockKey, Duration.ofSeconds(5), Duration.ofSeconds(30))) {
        try {
            // 임계 영역 코드
            orderService.process(orderId);
        } finally {
            lockProvider.unlock(lockKey);
        }
    } else {
        throw new BusinessException("Order is being processed by another instance");
    }
}
```

### 2. 시퀀스 생성기

```java
@Autowired
private RedisSequenceGenerator sequenceGenerator;

// 단일 시퀀스
long seq = sequenceGenerator.nextValue("order-seq");
// => 1, 2, 3, ...

// 배치 시퀀스
List<Long> seqList = sequenceGenerator.nextBatch("invoice-seq", 100);
// => [1, 2, 3, ..., 100]

// 초기값 지정
sequenceGenerator.setInitialValue("user-seq", 10000);
long seq = sequenceGenerator.nextValue("user-seq");
// => 10000, 10001, 10002, ...
```

### 3. 멱등성 보장

```java
@Autowired
private IdempotencyStore idempotencyStore;

public void processPayment(String requestId, PaymentRequest request) {
    // 중복 요청 체크
    if (idempotencyStore.exists(requestId)) {
        throw new BusinessException("Duplicate request: " + requestId);
    }

    try {
        // 결제 처리
        PaymentResult result = paymentService.process(request);

        // 결과 저장 (24시간 유지)
        idempotencyStore.store(requestId, result);

    } catch (Exception e) {
        // 실패한 요청은 저장하지 않음 (재시도 가능)
        throw e;
    }
}
```

### 4. 캐시 통계 활용

```java
@Autowired
private RedisCacheStatistics statistics;

@Around("@annotation(Cacheable)")
public Object trackCacheHit(ProceedingJoinPoint joinPoint) throws Throwable {
    String cacheName = extractCacheName(joinPoint);
    long startTime = System.currentTimeMillis();

    Object result = joinPoint.proceed();

    long latency = System.currentTimeMillis() - startTime;

    if (result != null) {
        statistics.recordHit(cacheName);
    } else {
        statistics.recordMiss(cacheName);
    }

    statistics.recordLatency(cacheName, latency);

    return result;
}
```

### 5. 캐시 워밍 전략

```java
@Component
public class ProductCacheWarmer {
    @Autowired
    private CacheWarmer cacheWarmer;

    @Autowired
    private ProductRepository productRepository;

    @PostConstruct
    public void setup() {
        // 전략 1: 인기 상품 워밍
        cacheWarmer.register("popular-products", () ->
            productRepository.findTop100ByOrderBySalesDesc()
        );

        // 전략 2: 신상품 워밍 (1시간 TTL)
        cacheWarmer.register("new-products", () ->
            productRepository.findTop50ByOrderByCreatedAtDesc(),
            Duration.ofHours(1)
        );

        // 전략 3: 카테고리별 워밍
        for (Category category : Category.values()) {
            cacheWarmer.register("products:" + category, () ->
                productRepository.findByCategory(category)
            );
        }
    }

    // 수동 워밍 (예: 마케팅 이벤트 전)
    public void warmBeforePromotion() {
        cacheWarmer.warmCache("popular-products");
        cacheWarmer.warmCache("new-products");
    }
}
```

### 6. 분산 캐시 무효화 패턴

```java
@Service
public class UserService {
    @Autowired
    private RedisCacheInvalidator invalidator;

    @Transactional
    public void updateUser(Long userId, UserUpdateRequest request) {
        // 1. DB 업데이트
        userRepository.update(userId, request);

        // 2. 모든 인스턴스의 캐시 무효화
        invalidator.invalidate("users", "user:" + userId);

        // 사용자가 속한 그룹 캐시도 무효화
        if (request.getGroupId() != null) {
            invalidator.invalidatePattern("groups", "group:" + request.getGroupId() + ":*");
        }
    }

    @Transactional
    public void deleteAllUsers() {
        userRepository.deleteAll();

        // 전체 사용자 캐시 무효화
        invalidator.invalidateAll("users");
    }
}
```

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-data-redis</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 테스트

```bash
# 모든 테스트 실행
mvn test

# 특정 테스트만 실행
mvn test -Dtest=RedisCacheStatisticsTest
mvn test -Dtest=CacheWarmerTest
mvn test -Dtest=RedisCacheInvalidatorTest
```

## 성능 고려사항

### 캐시 통계
- 비동기 업데이트 권장 (`async: true`)
- 통계 수집이 캐시 성능에 영향 최소화
- In-memory 카운터 사용 (Redis 부하 없음)

### 캐시 워밍
- 병렬 스레드 수 조정 (`parallel-threads`)
- 너무 많은 데이터 워밍 시 메모리 고려
- ApplicationReadyEvent 사용으로 초기화 지연 최소화

### 캐시 무효화
- Pub/Sub 메시지 크기 최소화
- 패턴 기반 무효화는 Redis KEYS 명령 사용 (주의 필요)
- 대량 무효화 시 `invalidateAll()` 사용 권장

## 모니터링

### 캐시 통계 확인

```bash
# 전역 통계
curl http://localhost:8080/admin/cache/statistics/global
{
  "totalHits": 95000,
  "totalMisses": 5000,
  "hitRate": 95.0,
  "missRate": 5.0,
  "totalRequests": 100000,
  "cacheCount": 5,
  "uptimeSeconds": 3600,
  "redisConnected": true
}

# 특정 캐시
curl http://localhost:8080/admin/cache/statistics/users
{
  "cacheName": "users",
  "hits": 45000,
  "misses": 2000,
  "hitRate": 95.74,
  "averageLatencyMs": 12.5,
  "totalSizeBytes": 2048576,
  "ageSeconds": 3600
}
```

### 헬스 체크

```bash
curl http://localhost:8080/admin/cache/health
{
  "status": "UP",
  "statistics": {...},
  "warmer": {"available": true},
  "invalidator": {"available": true}
}
```

## 문제 해결

### Redis 연결 실패
```yaml
# 연결 타임아웃 증가
spring:
  data:
    redis:
      timeout: 10s
      lettuce:
        pool:
          max-wait: 5s
```

### 캐시 워밍 시간 초과
```yaml
# 병렬 스레드 증가 또는 데이터 양 감소
eraf:
  redis:
    warming:
      parallel-threads: 8
```

### Pub/Sub 메시지 수신 안 됨
- RedisMessageListenerContainer 빈 등록 확인
- 채널 이름 일치 확인 (`channel-prefix` 설정)
- Redis 네트워크 연결 확인

## 라이센스

Copyright © 2024 ERAF. All rights reserved.
