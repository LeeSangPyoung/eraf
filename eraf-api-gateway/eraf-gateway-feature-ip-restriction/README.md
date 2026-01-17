# ERAF Gateway Feature - IP Restriction

API Gateway의 IP 제한 기능을 제공하는 모듈입니다.

## 기능

- **IP 블랙리스트**: 특정 IP 차단 (DENY)
- **IP 화이트리스트**: 특정 IP만 허용 (ALLOW)
- **CIDR 블록 지원**: 네트워크 범위 지정 (예: 192.168.1.0/24)
- **경로별 규칙**: PathMatcher를 사용한 경로 패턴 매칭
- **만료 시간 설정**: 일시적 차단/허용 규칙
- **활성화/비활성화**: 규칙별 활성화 상태 관리

## 포함 내용

### Domain
- `IpRestriction`: IP 제한 규칙 도메인 모델
- `RestrictionType`: ALLOW (화이트리스트), DENY (블랙리스트)

### Repository
- `IpRestrictionRepository`: IP 제한 규칙 저장소 인터페이스

### Service
- `IpRestrictionService`: IP 접근 체크 및 규칙 관리

### Filter
- `IpRestrictionFilter`: HTTP 요청 필터 (Order: HIGHEST + 20)

### Exception
- `IpBlockedException`: IP 차단 예외

### Configuration
- `IpRestrictionProperties`: 설정 클래스
- `IpRestrictionAutoConfiguration`: Spring Boot 자동 설정

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-feature-ip-restriction</artifactId>
</dependency>
```

**주의**: Repository 구현체는 별도로 제공해야 합니다.
- `eraf-gateway-store-memory` 또는
- `eraf-gateway-store-jpa`

## 설정 예시

```yaml
eraf:
  gateway:
    ip-restriction:
      enabled: true
      support-cidr: true
      auto-cleanup-expired: true
      cleanup-interval-minutes: 60
      exclude-patterns:
        - /actuator/**
        - /health/**
        - /public/**
      default-blacklist:
        - 10.0.0.1
        - 192.168.1.100
      default-whitelist:
        - 172.16.0.0/16
```

## IP 제한 타입

### DENY (블랙리스트)
특정 IP 주소를 차단합니다. 해당 IP에서의 모든 요청이 거부됩니다.

```java
// 단일 IP 차단
ipRestrictionService.blockIp("10.0.0.1", "Suspicious activity");

// CIDR 블록 차단
IpRestriction restriction = IpRestriction.builder()
    .ipAddress("192.168.1.0/24")
    .type(IpRestriction.RestrictionType.DENY)
    .description("Block entire subnet")
    .enabled(true)
    .build();
ipRestrictionService.createRestriction(restriction);
```

### ALLOW (화이트리스트)
특정 IP 주소만 허용합니다. 화이트리스트가 설정된 경로는 해당 IP만 접근 가능합니다.

```java
// 특정 IP만 관리자 경로 접근 허용
ipRestrictionService.allowIp(
    "192.168.1.100",
    "/api/admin/**",
    "Admin access from office"
);

// CIDR 블록 허용
IpRestriction restriction = IpRestriction.builder()
    .ipAddress("10.0.0.0/8")
    .type(IpRestriction.RestrictionType.ALLOW)
    .pathPattern("/api/internal/**")
    .description("Internal network access")
    .enabled(true)
    .build();
ipRestrictionService.createRestriction(restriction);
```

## IP 체크 로직

1. **DENY 리스트 확인**: 먼저 블랙리스트를 확인하고, 매칭되면 즉시 차단
2. **ALLOW 리스트 확인**: 해당 경로에 화이트리스트가 있으면, 화이트리스트에 포함된 IP만 허용
3. **ALLOW 리스트 없음**: 화이트리스트가 없으면 모든 IP 허용

## CIDR 블록 매칭

IP 주소에 `/`가 포함되면 CIDR 블록으로 인식합니다.

```java
// 예시
192.168.1.0/24     // 192.168.1.0 ~ 192.168.1.255
10.0.0.0/8         // 10.0.0.0 ~ 10.255.255.255
172.16.0.0/12      // 172.16.0.0 ~ 172.31.255.255
```

## 차단 응답 (403 Forbidden)

```json
{
  "code": "IP_BLOCKED",
  "message": "해당 IP는 차단되었습니다",
  "status": 403
}
```

## 사용 예시

### 1. 악의적인 IP 즉시 차단

```java
@Autowired
private IpRestrictionService ipRestrictionService;

// 공격 감지 시 IP 차단
public void blockMaliciousIp(String ip) {
    ipRestrictionService.blockIp(ip, "DDoS attack detected");
}
```

### 2. 관리자 페이지 IP 제한

```java
// 사무실 IP에서만 관리자 접근 허용
IpRestriction restriction = IpRestriction.builder()
    .ipAddress("203.0.113.0/24")
    .type(IpRestriction.RestrictionType.ALLOW)
    .pathPattern("/api/admin/**")
    .description("Office network only")
    .enabled(true)
    .build();
ipRestrictionService.createRestriction(restriction);
```

### 3. 일시적 차단 (만료 시간 설정)

```java
// 1시간 동안만 차단
IpRestriction tempBlock = IpRestriction.builder()
    .ipAddress("192.168.1.50")
    .type(IpRestriction.RestrictionType.DENY)
    .description("Temporary block for rate limit violation")
    .enabled(true)
    .expiresAt(LocalDateTime.now().plusHours(1))
    .build();
ipRestrictionService.createRestriction(tempBlock);
```

### 4. IP 접근 확인 (프로그래밍 방식)

```java
// 예외 발생 없이 체크
boolean allowed = ipRestrictionService.isIpAllowed(clientIp, requestPath);

if (!allowed) {
    log.warn("IP access denied: {}", clientIp);
}
```

## 규칙 관리

### 규칙 조회

```java
// 모든 규칙 조회
List<IpRestriction> allRules = ipRestrictionService.getAllRestrictions();

// 특정 규칙 조회
Optional<IpRestriction> rule = ipRestrictionService.getRestriction(ruleId);
```

### 규칙 수정

```java
IpRestriction restriction = ipRestrictionService.getRestriction(ruleId).orElseThrow();

// 규칙 비활성화
restriction = IpRestriction.builder()
    .id(restriction.getId())
    .ipAddress(restriction.getIpAddress())
    .type(restriction.getType())
    .pathPattern(restriction.getPathPattern())
    .description(restriction.getDescription())
    .enabled(false)  // 비활성화
    .build();

ipRestrictionService.updateRestriction(restriction);
```

### 규칙 삭제

```java
ipRestrictionService.deleteRestriction(ruleId);
```

## 필터 순서

IP Restriction 필터는 다른 필터들보다 먼저 실행됩니다:

```
1. Bot Detection (HIGHEST + 5)
2. Rate Limit (HIGHEST + 10)
3. IP Restriction (HIGHEST + 20)  <- 이 모듈
4. API Key Auth (HIGHEST + 30)
5. JWT Validation (HIGHEST + 35)
...
```

## 사용 방법

1. 모듈 의존성 추가
2. Repository 구현체 선택 (Memory 또는 JPA)
3. 설정 파일에 ip-restriction 설정 추가
4. IpRestrictionService를 통해 규칙 생성/관리
5. 필터가 자동으로 IP 체크 수행

## 빌드

```bash
mvn clean install
```

## 주의사항

- DENY 규칙이 ALLOW 규칙보다 우선 적용됩니다
- 화이트리스트를 설정하면 해당 경로는 화이트리스트에 있는 IP만 접근 가능
- CIDR 블록을 사용할 때는 네트워크 범위를 정확히 계산하세요
- X-Forwarded-For 헤더를 통해 실제 클라이언트 IP를 추출합니다 (프록시 환경 대응)
- Rate Limit과 함께 사용하여 보안을 강화할 수 있습니다
