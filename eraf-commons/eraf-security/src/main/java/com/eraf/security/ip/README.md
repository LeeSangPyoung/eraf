# IP 접근 제어 (IP Access Control)

IP 주소 기반으로 요청을 필터링하는 보안 기능입니다.

## 주요 기능

- **화이트리스트/블랙리스트**: IP 주소 또는 CIDR 범위 지원
- **기본 정책**: ALLOW(기본허용) 또는 DENY(기본차단)
- **URL 패턴 필터링**: 특정 경로에만 IP 제어 적용
- **프록시 환경 지원**: X-Forwarded-For, X-Real-IP 헤더 처리
- **감사 로그**: IP 차단 시 자동 로그 기록

## 사용 예시

### 1. 기본 설정 (블랙리스트 방식)

```yaml
eraf:
  security:
    ip-access-control:
      enabled: true
      default-policy: ALLOW  # 기본 허용
      blacklist:
        - "192.168.1.100"      # 특정 IP 차단
        - "10.0.0.0/8"          # CIDR 범위 차단
        - "172.16.0.0/12"
```

### 2. 화이트리스트 방식 (더 엄격)

```yaml
eraf:
  security:
    ip-access-control:
      enabled: true
      default-policy: DENY    # 기본 차단
      whitelist:
        - "203.0.113.0/24"    # 허용할 IP 범위
        - "198.51.100.50"     # 특정 IP 허용
```

### 3. 특정 경로에만 적용

```yaml
eraf:
  security:
    ip-access-control:
      enabled: true
      default-policy: DENY
      whitelist:
        - "10.0.0.0/8"
      include-patterns:
        - "/admin/**"          # Admin 경로만 IP 제어
        - "/api/sensitive/**"
      exclude-patterns:
        - "/actuator/health"   # Health check는 제외
        - "/public/**"
```

### 4. 프록시 환경 설정

```yaml
eraf:
  security:
    ip-access-control:
      enabled: true
      use-forwarded-for: true   # X-Forwarded-For 사용
      use-real-ip: true         # X-Real-IP 사용
      whitelist:
        - "10.0.0.0/8"
```

### 5. 커스텀 차단 메시지

```yaml
eraf:
  security:
    ip-access-control:
      enabled: true
      denied-message: "귀하의 IP 주소에서 접근이 제한되었습니다."
      audit-on-denied: true     # 차단 시 감사 로그 기록
```

## 동작 방식

### 1. IP 추출 우선순위

1. **X-Forwarded-For 헤더** (use-forwarded-for: true)
   - 프록시/로드밸런서 환경에서 실제 클라이언트 IP 추출
   - 예: `X-Forwarded-For: 203.0.113.195, 10.0.0.1`
   - 첫 번째 IP(203.0.113.195)를 클라이언트 IP로 사용

2. **X-Real-IP 헤더** (use-real-ip: true)
   - Nginx 등에서 설정한 실제 IP
   - 예: `X-Real-IP: 203.0.113.195`

3. **Remote Address**
   - 직접 연결된 클라이언트 IP
   - `request.getRemoteAddr()`

### 2. IP 검증 순서

```
요청 → IP 추출 → 블랙리스트 확인(차단) → 화이트리스트 확인(허용) → 기본 정책 적용
```

1. **블랙리스트 확인** (최우선)
   - 블랙리스트에 있으면 무조건 차단 (403 Forbidden)

2. **화이트리스트 확인**
   - 화이트리스트에 있으면 허용

3. **기본 정책 적용**
   - `ALLOW`: 화이트리스트에 없어도 허용 (블랙리스트만 차단)
   - `DENY`: 화이트리스트에 없으면 차단 (화이트리스트만 허용)

### 3. URL 패턴 매칭

- **include-patterns**: 지정된 경로에만 IP 제어 적용
  - 비어있으면 모든 요청에 적용
  - Ant-style 패턴 지원: `/admin/**`, `/api/*/sensitive`

- **exclude-patterns**: IP 제어에서 제외할 경로
  - Health check, 공개 API 등
  - include-patterns보다 우선 적용

## CIDR 표기법

### 지원 형식

```yaml
whitelist:
  - "192.168.1.100"           # 단일 IP
  - "10.0.0.0/8"              # Class A (10.0.0.0 ~ 10.255.255.255)
  - "172.16.0.0/12"           # Private network
  - "192.168.1.0/24"          # Class C (192.168.1.0 ~ 192.168.1.255)
  - "203.0.113.0/28"          # 16개 IP
```

### CIDR 계산 예시

| CIDR          | IP 범위                     | 개수     |
|---------------|-----------------------------|----------|
| /32           | 1개 IP                      | 1        |
| /24           | 192.168.1.0 ~ 192.168.1.255 | 256      |
| /16           | 172.16.0.0 ~ 172.16.255.255 | 65,536   |
| /8            | 10.0.0.0 ~ 10.255.255.255   | 16,777,216|

## 차단 시 응답

### HTTP 403 Forbidden

```json
{
  "error": "Forbidden",
  "message": "Access denied from your IP address",
  "path": "/admin/users"
}
```

### 헤더

```
HTTP/1.1 403 Forbidden
Content-Type: application/json;charset=UTF-8
```

### 로그 (audit-on-denied: true)

```json
{
  "timestamp": "2025-01-15T10:30:00Z",
  "eventType": "ACCESS_DENIED",
  "ipAddress": "203.0.113.50",
  "requestUri": "/admin/users",
  "method": "GET",
  "success": false,
  "details": {
    "reason": "IP access control"
  }
}
```

## 실전 시나리오

### 시나리오 1: Admin 페이지 보호

**요구사항**: Admin 페이지는 사내 IP에서만 접근

```yaml
eraf:
  security:
    ip-access-control:
      enabled: true
      default-policy: DENY
      whitelist:
        - "10.0.0.0/8"          # 사내 네트워크
        - "203.0.113.100"       # VPN IP
      include-patterns:
        - "/admin/**"
      exclude-patterns:
        - "/admin/login"        # 로그인 페이지는 제외
```

### 시나리오 2: 악성 IP 차단

**요구사항**: 특정 IP 범위에서 공격 감지

```yaml
eraf:
  security:
    ip-access-control:
      enabled: true
      default-policy: ALLOW
      blacklist:
        - "45.142.120.0/22"     # 악성 IP 범위
        - "185.220.100.0/22"
        - "192.0.2.50"          # 특정 공격자 IP
      audit-on-denied: true     # 차단 로그 기록
```

### 시나리오 3: API 엔드포인트 보호 (화이트리스트)

**요구사항**: Webhook API는 특정 서비스에서만 호출 가능

```yaml
eraf:
  security:
    ip-access-control:
      enabled: true
      default-policy: DENY
      whitelist:
        - "192.0.2.0/24"        # 파트너 A
        - "198.51.100.0/24"     # 파트너 B
      include-patterns:
        - "/api/webhooks/**"
```

### 시나리오 4: 프록시 환경 (AWS ALB/NLB)

**요구사항**: AWS ALB 뒤에서 실제 클라이언트 IP 확인

```yaml
eraf:
  security:
    ip-access-control:
      enabled: true
      use-forwarded-for: true   # X-Forwarded-For 헤더 사용
      use-real-ip: false        # ALB는 X-Real-IP 미사용
      default-policy: DENY
      whitelist:
        - "203.0.113.0/24"
```

## 주의사항

### 1. 프록시 환경 설정

- **프록시/로드밸런서 사용 시**: `use-forwarded-for: true` 필수
- **신뢰할 수 있는 프록시만 사용**: X-Forwarded-For는 조작 가능
- **AWS ALB/NLB**: `use-forwarded-for: true`
- **Nginx**: `use-real-ip: true` 또는 `use-forwarded-for: true`

### 2. 기본 정책 선택

- **ALLOW (블랙리스트)**: 일반적인 보안, 유연성 높음
- **DENY (화이트리스트)**: 높은 보안 요구, 관리 부담 증가

### 3. Health Check 제외

```yaml
exclude-patterns:
  - "/actuator/health"
  - "/actuator/info"
  - "/health"
```

### 4. 성능 고려

- IP 검증은 모든 요청마다 실행
- CIDR 매칭은 정확한 IP 매칭보다 느림
- 화이트리스트/블랙리스트가 너무 많으면 성능 저하 가능

## 프로그래밍 방식 사용

### 커스텀 IP Validator

```java
@Component
public class CustomIpValidator {

    private final IpValidator ipValidator;

    public CustomIpValidator(IpAccessControlProperties properties) {
        this.ipValidator = new IpValidator(
            properties.getWhitelist(),
            properties.getBlacklist()
        );
    }

    public boolean checkIp(String ip) {
        return ipValidator.isAllowed(ip, AccessPolicy.ALLOW);
    }
}
```

### 커스텀 감사 리스너

```java
@Component
public class IpAccessAuditListener {

    @Autowired
    public void registerListener(SecurityAuditLogger auditLogger) {
        auditLogger.addListener(event -> {
            if (event.getEventType() == EventType.ACCESS_DENIED) {
                // 커스텀 처리: 알림, DB 저장 등
                notifyAdmin(event);
            }
        });
    }
}
```

## 트러블슈팅

### 문제: 올바른 IP가 차단됨

**원인**: 프록시 설정 오류

**해결**:
```yaml
use-forwarded-for: true  # 프록시 환경이면 true
use-real-ip: true        # Nginx 등 사용 시
```

### 문제: 로그에 프록시 IP만 보임

**원인**: X-Forwarded-For 헤더 미사용

**해결**: 프록시/로드밸런서에서 X-Forwarded-For 헤더 전달 설정

**Nginx 예시**:
```nginx
proxy_set_header X-Real-IP $remote_addr;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
```

### 문제: Health check가 실패함

**원인**: Health check 경로에 IP 제어 적용

**해결**:
```yaml
exclude-patterns:
  - "/actuator/health"
```
