# ERAF Gateway Feature - Circuit Breaker

API Gateway의 Circuit Breaker 기능을 제공하는 모듈입니다.

## 기능

- **자동 장애 감지**: 연속된 실패 요청을 감지하여 자동으로 차단
- **3단계 상태 관리**: CLOSED (정상) -> OPEN (차단) -> HALF_OPEN (시험)
- **자동 복구**: 일정 시간 후 HALF_OPEN 상태로 전환하여 복구 시도
- **경로별 Circuit Breaker**: 엔드포인트별로 독립적인 Circuit Breaker 적용
- **상태 헤더**: X-Circuit-Breaker 헤더로 Circuit Breaker 이름 제공

## 포함 내용

### Core
- `CircuitBreaker`: Circuit Breaker 핵심 구현
- `CircuitBreakerRegistry`: Circuit Breaker 레지스트리

### Filter
- `CircuitBreakerFilter`: HTTP 요청 필터 (Order: HIGHEST + 40)

### Exception
- `CircuitBreakerException`: Circuit Breaker OPEN 상태 예외

### Configuration
- `CircuitBreakerProperties`: 설정 클래스
- `CircuitBreakerAutoConfiguration`: Spring Boot 자동 설정

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-feature-circuit-breaker</artifactId>
</dependency>
```

## 설정 예시

```yaml
eraf:
  gateway:
    circuit-breaker:
      enabled: true
      default-failure-threshold: 5
      default-success-threshold: 2
      default-open-timeout-ms: 60000
      failure-status-threshold: 500
      exclude-patterns:
        - /actuator/**
        - /health/**
```

## Circuit Breaker 상태

### CLOSED (정상)
- 모든 요청 허용
- 실패 카운트 증가
- 실패 임계값 도달 시 OPEN으로 전환

### OPEN (차단)
- 모든 요청 거부 (503 Service Unavailable)
- 타임아웃 후 HALF_OPEN으로 전환

### HALF_OPEN (시험)
- 제한된 요청 허용
- 성공 시 성공 카운트 증가
- 성공 임계값 도달 시 CLOSED로 전환
- 실패 시 즉시 OPEN으로 전환

## 응답 헤더

### Circuit Breaker OPEN (503)
```
X-Circuit-Breaker: api-users
```

## Circuit Breaker 이름 생성 규칙

경로 기반으로 Circuit Breaker 이름을 자동 생성합니다:

- `/api/users/123` -> `api-users`
- `/products/details` -> `products-details`
- `/health` -> `health`

## 실패 판단 기준

- HTTP 상태 코드 500 이상 (기본값)
- 필터 체인에서 발생한 예외
- `failure-status-threshold` 설정으로 기준 변경 가능

## 사용 방법

1. 모듈 의존성 추가
2. 설정 파일에 circuit-breaker 설정 추가
3. 필요시 excludePatterns로 특정 경로 제외

## 수동 제어

CircuitBreakerRegistry를 주입받아 수동으로 제어할 수 있습니다:

```java
@Autowired
private CircuitBreakerRegistry registry;

// Circuit Breaker 강제 OPEN
registry.get("api-users").ifPresent(CircuitBreaker::trip);

// Circuit Breaker 강제 CLOSED (리셋)
registry.get("api-users").ifPresent(CircuitBreaker::reset);

// 모든 Circuit Breaker 리셋
registry.resetAll();

// 상태 조회
registry.get("api-users").ifPresent(cb -> {
    CircuitBreaker.CircuitBreakerStatus status = cb.getStatus();
    System.out.println("State: " + status.getState());
    System.out.println("Failure Count: " + status.getFailureCount());
});
```

## 설정 파라미터

| 파라미터 | 기본값 | 설명 |
|---------|-------|------|
| enabled | true | Circuit Breaker 기능 활성화 |
| default-failure-threshold | 5 | CLOSED -> OPEN 전환 실패 횟수 |
| default-success-threshold | 2 | HALF_OPEN -> CLOSED 전환 성공 횟수 |
| default-open-timeout-ms | 60000 | OPEN -> HALF_OPEN 전환 대기 시간 (ms) |
| failure-status-threshold | 500 | 실패로 간주할 HTTP 상태 코드 시작 값 |
| exclude-patterns | [] | Circuit Breaker를 적용하지 않을 경로 패턴 |

## 모니터링

Circuit Breaker 상태를 모니터링하려면:

```java
@RestController
@RequestMapping("/admin/circuit-breakers")
public class CircuitBreakerController {

    @Autowired
    private CircuitBreakerRegistry registry;

    @GetMapping
    public List<CircuitBreaker.CircuitBreakerStatus> getAll() {
        return registry.getAll().stream()
                .map(CircuitBreaker::getStatus)
                .collect(Collectors.toList());
    }
}
```

## 빌드

```bash
mvn clean install
```
