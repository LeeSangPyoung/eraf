# ERAF Gateway Feature - Cache

API Gateway의 Response Caching 기능을 제공하는 모듈입니다.

## 기능

- **응답 캐싱**: GET 요청에 대한 응답을 메모리에 캐싱
- **TTL 기반 만료**: 시간 기반 캐시 만료 관리
- **경로별 규칙**: PathMatcher를 사용한 경로 패턴 매칭
- **유연한 캐시 키**: 쿼리 파라미터 및 헤더 기반 캐시 구분
- **캐시 헤더**: X-Cache, X-Cache-TTL 헤더로 캐시 상태 제공
- **Set-Cookie 제외**: 보안을 위해 쿠키는 캐시하지 않음

## 포함 내용

### Domain
- `CachedResponse`: 캐시된 응답 데이터 (상태 코드, 헤더, 바디, TTL 등)
- `CacheRule`: 캐시 규칙 정의 (경로 패턴, 메소드, TTL 등)

### Repository
- `ResponseCacheRepository`: 캐시 저장소 인터페이스

### Filter
- `ResponseCacheFilter`: HTTP 응답 캐싱 필터 (Order: HIGHEST + 50)

### Configuration
- `CacheProperties`: 설정 클래스
- `CacheAutoConfiguration`: Spring Boot 자동 설정

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-feature-cache</artifactId>
</dependency>
```

**주의**: Repository 구현체는 별도로 제공해야 합니다.
- `eraf-gateway-store-memory` 또는
- `eraf-gateway-store-jpa`

## 설정 예시

```yaml
eraf:
  gateway:
    cache:
      enabled: true
      default-ttl-seconds: 300
      vary-by-query-params: true
      vary-by-headers: false
      vary-headers:
        - Accept-Language
        - Accept-Encoding
      exclude-patterns:
        - /actuator/**
        - /health/**
      cleanup-interval-seconds: 60
      max-cache-size: 1000
```

## 응답 헤더

### 캐시 HIT
```
X-Cache: HIT
X-Cache-TTL: 285
```

- `X-Cache`: HIT (캐시에서 제공) 또는 MISS (원본에서 제공)
- `X-Cache-TTL`: 캐시 만료까지 남은 시간 (초)

## 캐시 키 생성 규칙

캐시 키는 다음 요소로 구성됩니다:

1. **기본**: `HTTP_METHOD:REQUEST_URI`
2. **쿼리 파라미터**: `varyByQueryParams=true`인 경우 쿼리 문자열 포함
3. **헤더**: `varyByHeaders=true`이고 `varyHeaders`가 설정된 경우 해당 헤더 값 포함

예시:
```
GET:/api/users
GET:/api/users?page=1&size=10
GET:/api/users|Accept-Language=ko
```

## 캐시 규칙

### 기본 규칙
- 경로 패턴: `/**` (모든 경로)
- HTTP 메소드: `GET`만 캐싱
- TTL: 설정된 `default-ttl-seconds` 값 사용
- 성공 응답만 캐싱: 2xx 상태 코드만 캐시

### 커스텀 규칙 추가
추후 `CacheRule`을 동적으로 생성하여 경로별, 메소드별 세밀한 캐시 정책 적용 가능

## 캐시 대상

### 캐시됨
- GET 요청의 2xx 응답
- 응답 헤더 (Set-Cookie 제외)
- 응답 바디

### 캐시 안됨
- POST, PUT, DELETE 등 GET 이외의 요청
- 4xx, 5xx 오류 응답
- Set-Cookie 헤더

## Repository 구현 예시

```java
@Component
public class InMemoryResponseCacheRepository implements ResponseCacheRepository {

    private final Map<String, CachedResponse> cache = new ConcurrentHashMap<>();

    @Override
    public Optional<CachedResponse> get(String key) {
        CachedResponse response = cache.get(key);
        if (response != null && !response.isExpired()) {
            return Optional.of(response);
        }
        cache.remove(key);
        return Optional.empty();
    }

    @Override
    public void put(String key, CachedResponse response) {
        cache.put(key, response);
    }

    @Override
    public void evict(String key) {
        cache.remove(key);
    }

    @Override
    public void evictByPattern(String pattern) {
        cache.keySet().stream()
            .filter(key -> PathMatcher.matches(key, pattern))
            .forEach(cache::remove);
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public void cleanupExpired() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
```

## 사용 방법

1. 모듈 의존성 추가
2. Repository 구현체 선택 또는 구현
3. 설정 파일에 cache 설정 추가
4. 필요시 CacheRule 동적 생성/수정

## 빌드

```bash
mvn clean install
```

## 성능 고려사항

- 메모리 사용량: `max-cache-size` 설정으로 제한
- 정리 주기: `cleanup-interval-seconds`로 만료된 캐시 자동 정리
- 캐시 키 복잡도: `varyByHeaders`가 많을수록 캐시 효율 저하
- 응답 크기: 큰 응답은 메모리 압박 가능

## 주의사항

- 동적 콘텐츠나 사용자별 데이터는 캐싱하지 않도록 제외 패턴 설정 필요
- Set-Cookie 헤더는 자동으로 제외되지만, 민감한 정보가 포함된 헤더는 추가 검토 필요
- 분산 환경에서는 Redis 등 외부 캐시 저장소 사용 권장
