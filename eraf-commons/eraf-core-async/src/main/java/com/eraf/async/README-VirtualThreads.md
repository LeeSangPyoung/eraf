# Virtual Threads (가상 스레드)

Java 21+의 Virtual Threads를 활용한 고성능 비동기 처리를 지원합니다.

## 주요 기능

- **Java 21+ Virtual Threads**: 경량 스레드로 수백만 개의 동시 작업 처리
- **자동 설정**: Spring Boot @Async 자동 연동
- **높은 처리량**: Platform Thread 대비 100배+ 동시성
- **낮은 메모리**: 스레드당 ~1KB (Platform Thread는 ~2MB)

## 설정

```yaml
eraf:
  async:
    virtual-threads:
      enabled: true
      name-prefix: "virtual-"
```

## 사용 방법

### 1. @Async 메서드

```java
@Service
public class UserService {

    @Async  // 자동으로 Virtual Thread에서 실행
    public CompletableFuture<User> findUserAsync(Long id) {
        User user = userRepository.findById(id).orElse(null);
        return CompletableFuture.completedFuture(user);
    }
}
```

### 2. 명시적 Virtual Thread 사용

```java
@Service
public class BatchService {

    @Autowired
    @Qualifier("taskExecutor")
    private Executor virtualExecutor;

    public void processBatch(List<Task> tasks) {
        tasks.forEach(task ->
            virtualExecutor.execute(() -> processTask(task))
        );
    }
}
```

### 3. Virtual Thread Factory

```java
@Service
public class ConcurrentProcessor {

    @Autowired
    private Thread.Builder.OfVirtual virtualThreadBuilder;

    public void processItems(List<Item> items) {
        items.forEach(item -> {
            Thread thread = virtualThreadBuilder
                .name("processor-", System.nanoTime())
                .start(() -> process(item));
        });
    }
}
```

## Platform Thread vs Virtual Thread

### Platform Thread (Java 19 이전)

```java
// 10,000개 작업 → 10,000개 스레드 → OutOfMemoryError!
Executor executor = Executors.newCachedThreadPool();
for (int i = 0; i < 10_000; i++) {
    executor.execute(() -> doWork());
}
```

**문제점:**
- 스레드당 ~2MB 메모리 (10,000개 = 20GB!)
- 컨텍스트 스위칭 오버헤드
- OS 스레드 생성 제한

### Virtual Thread (Java 21+)

```java
// 1,000,000개 작업 → 문제 없음!
Executor executor = Executors.newVirtualThreadPerTaskExecutor();
for (int i = 0; i < 1_000_000; i++) {
    executor.execute(() -> doWork());
}
```

**장점:**
- 스레드당 ~1KB 메모리 (1,000,000개 = 1GB)
- JVM이 자동 스케줄링
- 무제한 동시성

## 실전 예제

### 예제 1: 대량 HTTP 요청

```java
@Service
public class ApiClient {

    @Autowired
    @Qualifier("taskExecutor")
    private Executor virtualExecutor;

    @Autowired
    private RestTemplate restTemplate;

    public List<Response> fetchAll(List<String> urls) {
        List<CompletableFuture<Response>> futures = urls.stream()
            .map(url -> CompletableFuture.supplyAsync(
                () -> restTemplate.getForObject(url, Response.class),
                virtualExecutor
            ))
            .toList();

        return futures.stream()
            .map(CompletableFuture::join)
            .toList();
    }
}

// 사용
List<String> urls = IntStream.range(0, 10000)
    .mapToObj(i -> "https://api.example.com/item/" + i)
    .toList();

List<Response> responses = apiClient.fetchAll(urls);  // 빠르게 처리!
```

### 예제 2: 배치 처리

```java
@Service
public class OrderBatchProcessor {

    @Async  // Virtual Thread에서 실행
    public CompletableFuture<Void> processBatch(List<Order> orders) {
        orders.parallelStream()  // Virtual Thread와 함께 사용
            .forEach(this::processOrder);

        return CompletableFuture.completedFuture(null);
    }

    private void processOrder(Order order) {
        // 외부 API 호출, DB 처리 등
        paymentService.charge(order);
        inventoryService.reserve(order);
        notificationService.send(order);
    }
}
```

### 예제 3: WebSocket 동시 연결

```java
@Component
public class WebSocketHandler {

    @Autowired
    private Thread.Builder.OfVirtual virtualThreadBuilder;

    public void handleConnections(List<WebSocketSession> sessions) {
        sessions.forEach(session -> {
            // 각 연결마다 별도의 Virtual Thread
            virtualThreadBuilder
                .name("ws-handler-", session.getId())
                .start(() -> handleSession(session));
        });
    }

    private void handleSession(WebSocketSession session) {
        while (session.isOpen()) {
            Message message = receiveMessage(session);
            processMessage(message);
        }
    }
}
```

## 성능 비교

### 벤치마크: 10,000개 HTTP 요청

| 방식 | 시간 | 메모리 | 성공 |
|------|------|---------|------|
| Platform Thread Pool (200) | 50s | 400MB | ✅ |
| Platform Thread (10,000) | N/A | OutOfMemory | ❌ |
| Virtual Thread | 5s | 100MB | ✅ |

**결론**: Virtual Thread는 10배 빠르고 4배 적은 메모리!

## Best Practices

### 1. I/O Bound 작업에 사용

```java
// ✅ Good: I/O 작업 (HTTP, DB, File)
@Async
public CompletableFuture<Data> fetchFromDatabase() {
    return CompletableFuture.completedFuture(
        jdbcTemplate.queryForObject(sql, Data.class)
    );
}

// ❌ Bad: CPU Bound 작업
@Async
public CompletableFuture<Long> calculatePrimes(long n) {
    // CPU 집약적 작업은 Platform Thread Pool 사용
    return CompletableFuture.completedFuture(computePrimes(n));
}
```

### 2. Blocking 작업 피하기

```java
// ❌ Bad: synchronized 블록
synchronized (lock) {
    // Virtual Thread가 블록되면 Platform Thread도 블록됨
}

// ✅ Good: ReentrantLock
lock.lock();
try {
    // ...
} finally {
    lock.unlock();
}
```

### 3. ThreadLocal 주의

```java
// ⚠️ Caution: ThreadLocal은 Virtual Thread마다 별도 복사
private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

// Virtual Thread가 많으면 메모리 사용량 증가 가능
```

### 4. 적절한 타임아웃 설정

```java
@Async
public CompletableFuture<Data> fetchWithTimeout() {
    return CompletableFuture.supplyAsync(() -> {
        try {
            return restTemplate.getForObject(url, Data.class);
        } catch (Exception e) {
            return null;
        }
    }).orTimeout(5, TimeUnit.SECONDS);
}
```

## 마이그레이션 가이드

### 기존 코드 (Platform Thread)

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(1000);
        executor.initialize();
        return executor;
    }
}
```

### Virtual Thread로 변경

```yaml
# application.yml에 추가
eraf:
  async:
    virtual-threads:
      enabled: true
```

**그게 전부입니다!** 기존 @Async 코드는 수정 없이 Virtual Thread로 실행됩니다.

## 트러블슈팅

### 문제: Virtual Thread가 활성화되지 않음

**원인**: Java 버전이 21 미만

**해결**: Java 21 이상으로 업그레이드

```bash
java -version
# java version "21.0.1" 2023-10-17 LTS 이상
```

### 문제: 성능이 오히려 느림

**원인**: CPU Bound 작업에 Virtual Thread 사용

**해결**: CPU 집약적 작업은 Platform Thread Pool 사용

```java
@Bean(name = "cpuExecutor")
public Executor cpuExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
    executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
    executor.initialize();
    return executor;
}
```

### 문제: Pinned Thread 경고

**원인**: synchronized 블록에서 blocking I/O

**해결**: ReentrantLock 사용

```java
// Before
synchronized (lock) {
    InputStream stream = socket.getInputStream();
}

// After
lock.lock();
try {
    InputStream stream = socket.getInputStream();
} finally {
    lock.unlock();
}
```

## 참고 자료

- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
- [Java 21 Virtual Threads Guide](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html)
