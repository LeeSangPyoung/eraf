# ERAF Data Cache - 고도화된 캐시 설정

## 📋 개요

Spring Cache를 기반으로 한 고급 캐싱 솔루션입니다.

**주요 기능**:
- ✅ 다양한 캐시 타입 지원 (Simple, Caffeine, Redis, Multi-Level)
- ✅ Named Cache별 개별 TTL/크기 설정
- ✅ Caffeine 고성능 로컬 캐시
- ✅ Redis 분산 캐시 통합
- ✅ 캐시 통계 모니터링
- ✅ 자동 설정 (Auto Configuration)

## 🚀 사용 방법

### 1. 의존성 추가

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-data-cache</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- Caffeine 사용 시 -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>

<!-- Redis 사용 시 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 📦 캐시 타입별 설정

### 1. Simple Cache (기본)

가장 기본적인 `ConcurrentHashMap` 기반 캐시

```yaml
eraf:
  cache:
    type: simple  # 또는 설정 생략 (기본값)
    caches:
      userCache:
        max-size: 1000
      productCache:
        max-size: 5000
```

### 2. Caffeine Cache (권장)

고성능 로컬 캐시 (Guava Cache의 후속)

```yaml
eraf:
  cache:
    type: caffeine
    statistics-enabled: true
    caffeine:
      max-size: 10000
      expire-after-write: 1h
      expire-after-access: 30m
      initial-capacity: 100
      record-stats: true
    caches:
      # 사용자 캐시 - 5분 TTL
      userCache:
        max-size: 1000
        expire-after-write: 5m
      # 상품 캐시 - 1시간 TTL
      productCache:
        max-size: 5000
        expire-after-write: 1h
      # 세션 캐시 - 마지막 액세스 후 30분
      sessionCache:
        max-size: 2000
        expire-after-access: 30m
```

**Caffeine 설정 옵션**:
- `max-size`: 최대 엔트리 개수
- `expire-after-write`: 쓰기 후 만료 시간
- `expire-after-access`: 마지막 액세스 후 만료 시간
- `initial-capacity`: 초기 용량
- `record-stats`: 통계 기록 여부

### 3. Redis Cache (분산 캐시)

Redis를 이용한 분산 캐시

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379

eraf:
  cache:
    type: redis
    key-prefix: "myapp:cache:"
    redis:
      enable-null-values: true
      use-key-prefix: true
      ttl: 30m
    caches:
      userCache:
        ttl: 10m
      productCache:
        ttl: 1h
```

### 4. Multi-Level Cache (예정)

L1(Caffeine) + L2(Redis) 2단계 캐싱

```yaml
eraf:
  cache:
    type: multi-level
    multi-level:
      l1-cache-type: caffeine
      l2-cache-type: redis
      l1-max-size: 1000
      l1-ttl: 5m
      l2-ttl: 30m
```

## 💻 애플리케이션 코드

### 기본 사용 - @Cacheable

```java
@Service
public class UserService {

    @Cacheable(value = "userCache", key = "#id")
    public User findById(Long id) {
        // DB 조회 (캐시 미스 시에만 실행)
        return userRepository.findById(id).orElse(null);
    }

    @CachePut(value = "userCache", key = "#user.id")
    public User save(User user) {
        // 저장 후 캐시 갱신
        return userRepository.save(user);
    }

    @CacheEvict(value = "userCache", key = "#id")
    public void delete(Long id) {
        // 삭제 후 캐시 제거
        userRepository.deleteById(id);
    }

    @CacheEvict(value = "userCache", allEntries = true)
    public void clearAll() {
        // 전체 캐시 삭제
    }
}
```

### 조건부 캐싱

```java
@Cacheable(value = "userCache", key = "#id", condition = "#id != null")
public User findById(Long id) {
    return userRepository.findById(id).orElse(null);
}

@Cacheable(value = "userCache", key = "#id", unless = "#result == null")
public User findByIdUnlessNull(Long id) {
    return userRepository.findById(id).orElse(null);
}
```

### 복합 키 사용

```java
@Cacheable(value = "productCache", key = "#category + ':' + #brand")
public List<Product> findByCategoryAndBrand(String category, String brand) {
    return productRepository.findByCategoryAndBrand(category, brand);
}

// SpEL을 이용한 복잡한 키
@Cacheable(value = "orderCache", key = "T(String).format('%s:%s', #userId, #orderId)")
public Order findOrder(Long userId, Long orderId) {
    return orderRepository.findByUserIdAndId(userId, orderId);
}
```

## 📊 캐시 통계 모니터링

### 통계 활성화

```yaml
eraf:
  cache:
    type: caffeine
    statistics-enabled: true
    caffeine:
      record-stats: true
```

### 통계 조회

```java
@Service
public class CacheMonitoringService {

    @Autowired
    private CacheStatisticsCollector statsCollector;

    public void printAllStatistics() {
        Collection<CacheStatistics> allStats = statsCollector.getAllStatistics();

        for (CacheStatistics stats : allStats) {
            System.out.println(stats);
            // CacheStatistics{cacheName='userCache', hitCount=150, missCount=50, hitRate=75.00%, size=45}
        }
    }

    public CacheStatistics getUserCacheStats() {
        return statsCollector.getStatistics("userCache");
    }

    public Map<String, CacheStatistics> getAllStatsMap() {
        return statsCollector.getStatisticsMap();
    }

    // 캐시 삭제
    public void clearUserCache() {
        statsCollector.clear("userCache");
    }

    public void clearAllCaches() {
        statsCollector.clearAll();
    }
}
```

### REST API로 통계 노출

```java
@RestController
@RequestMapping("/admin/cache")
public class CacheAdminController {

    @Autowired
    private CacheStatisticsCollector statsCollector;

    @GetMapping("/statistics")
    public Map<String, CacheStatistics> getStatistics() {
        return statsCollector.getStatisticsMap();
    }

    @GetMapping("/statistics/{cacheName}")
    public CacheStatistics getCacheStatistics(@PathVariable String cacheName) {
        return statsCollector.getStatistics(cacheName);
    }

    @DeleteMapping("/{cacheName}")
    public void clearCache(@PathVariable String cacheName) {
        statsCollector.clear(cacheName);
    }

    @DeleteMapping("/all")
    public void clearAllCaches() {
        statsCollector.clearAll();
    }
}
```

## 🎯 실전 시나리오

### 시나리오 1: 사용자 프로필 캐싱

```yaml
eraf:
  cache:
    type: caffeine
    caches:
      userProfileCache:
        max-size: 10000
        expire-after-write: 10m  # 10분마다 갱신
```

```java
@Service
public class UserProfileService {

    @Cacheable(value = "userProfileCache", key = "#userId")
    public UserProfile getProfile(Long userId) {
        // DB 조회 (캐시 미스 시에만)
        return profileRepository.findByUserId(userId);
    }

    @CachePut(value = "userProfileCache", key = "#profile.userId")
    public UserProfile updateProfile(UserProfile profile) {
        return profileRepository.save(profile);
    }
}
```

### 시나리오 2: 상품 목록 캐싱 (Redis)

```yaml
eraf:
  cache:
    type: redis
    caches:
      productListCache:
        ttl: 1h  # 1시간 캐싱
```

```java
@Service
public class ProductService {

    @Cacheable(value = "productListCache", key = "#category")
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @CacheEvict(value = "productListCache", allEntries = true)
    @Scheduled(cron = "0 0 * * * *")  # 매시간 전체 캐시 삭제
    public void evictAllCaches() {
        log.info("All product cache evicted");
    }
}
```

### 시나리오 3: API 응답 캐싱

```java
@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/users/{id}")
    @Cacheable(value = "apiResponseCache", key = "'user:' + #id")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    @GetMapping("/products")
    @Cacheable(value = "apiResponseCache", key = "'products:' + #category + ':' + #page")
    public ResponseEntity<Page<ProductDTO>> getProducts(
            @RequestParam String category,
            @RequestParam int page) {

        Page<Product> products = productService.findByCategory(category, PageRequest.of(page, 20));
        return ResponseEntity.ok(products.map(productMapper::toDTO));
    }
}
```

## ⚙️ 고급 설정

### Named Cache별 개별 설정

```yaml
eraf:
  cache:
    type: caffeine
    caffeine:
      # 기본 설정
      max-size: 1000
      expire-after-write: 30m
    caches:
      # 짧은 TTL
      sessionCache:
        max-size: 5000
        expire-after-access: 5m
      # 긴 TTL
      staticDataCache:
        max-size: 100
        expire-after-write: 24h
      # 크기 제한만
      tempCache:
        max-size: 500
```

### 캐시 키 생성 전략

```java
@Configuration
public class CacheConfig {

    @Bean
    public KeyGenerator customKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName());
            sb.append(":");
            sb.append(method.getName());
            for (Object param : params) {
                sb.append(":").append(param);
            }
            return sb.toString();
        };
    }
}

// 사용
@Cacheable(value = "myCache", keyGenerator = "customKeyGenerator")
public User findUser(Long id) {
    return userRepository.findById(id).orElse(null);
}
```

## 🔍 문제 해결

### Q1: 캐시가 작동하지 않습니다

**해결**:
1. `@EnableCaching` 확인 (ERAF는 자동 활성화)
2. 메서드가 `public`인지 확인
3. 같은 클래스 내부 호출이 아닌지 확인 (프록시 우회)

```java
// ❌ 작동 안함 - 내부 호출
@Service
public class UserService {
    public User getUser(Long id) {
        return findById(id);  // 프록시 우회
    }

    @Cacheable("userCache")
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}

// ✅ 작동함 - 외부 호출
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Cacheable("userCache")
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
```

### Q2: Caffeine 통계가 보이지 않습니다

**해결**:
```yaml
eraf:
  cache:
    statistics-enabled: true
    caffeine:
      record-stats: true  # 중요!
```

### Q3: Redis 직렬화 오류 발생

**해결**: 엔티티에 직렬화 구현
```java
@Entity
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    // ...
}
```

또는 Jackson2JsonRedisSerializer 사용:
```java
@Bean
public RedisCacheConfiguration redisCacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer()
            )
        );
}
```

## 📈 성능 최적화 팁

### 1. 적절한 캐시 크기 설정

```yaml
eraf:
  cache:
    caches:
      # 자주 조회되는 핫 데이터 - 크게
      hotDataCache:
        max-size: 10000
      # 가끔 조회되는 데이터 - 작게
      coldDataCache:
        max-size: 100
```

### 2. TTL 최적화

```yaml
eraf:
  cache:
    caches:
      # 자주 변경되는 데이터 - 짧은 TTL
      dynamicDataCache:
        expire-after-write: 1m
      # 거의 변경 안되는 데이터 - 긴 TTL
      staticDataCache:
        expire-after-write: 24h
```

### 3. 캐시 워밍업

```java
@Component
public class CacheWarmer {

    @Autowired
    private UserService userService;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        // 애플리케이션 시작 시 주요 데이터 미리 캐싱
        List<Long> popularUserIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        for (Long userId : popularUserIds) {
            userService.findById(userId);
        }
        log.info("Cache warmed up");
    }
}
```

## 🔗 관련 클래스

- `ErafCacheProperties` - 캐시 설정 Properties
- `ErafCacheAutoConfiguration` - 자동 설정
- `CaffeineCacheManagerBuilder` - Caffeine 빌더
- `CacheStatistics` - 캐시 통계 정보
- `CacheStatisticsCollector` - 통계 수집기

## 📚 참고 자료

- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [Caffeine Cache](https://github.com/ben-manes/caffeine)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Cache Aside Pattern](https://docs.aws.amazon.com/whitepapers/latest/database-caching-strategies-using-redis/cache-aside.html)
