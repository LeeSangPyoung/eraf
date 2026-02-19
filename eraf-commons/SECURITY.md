# Security Policy

## Supported Versions

| Version | Supported |
|---------|-----------|
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

보안 취약점을 발견하신 경우 다음 절차를 따라주세요.

### 보고 방법

1. **공개 이슈로 보고하지 마세요.** 보안 취약점은 비공개로 처리해야 합니다.
2. 이메일로 보고: security@eraf.com
3. 다음 정보를 포함해 주세요:
   - 취약점 유형 (예: XSS, SQL Injection, CSRF 등)
   - 영향받는 모듈 및 버전
   - 재현 절차
   - 예상 영향도

### 응답 시간

- **확인**: 2 영업일 이내
- **초기 평가**: 5 영업일 이내
- **수정 릴리스**: 심각도에 따라 1-30일

### 심각도 기준

| 등급 | 설명 | 대응 시간 |
|------|------|-----------|
| Critical | 원격 코드 실행, 인증 우회 | 24시간 |
| High | 데이터 유출, 권한 상승 | 7일 |
| Medium | XSS, CSRF 등 | 14일 |
| Low | 정보 노출, 설정 미흡 | 30일 |

## Security Features

ERAF Commons에서 제공하는 보안 기능:

### 인증 (Authentication)
- JWT 토큰 기반 인증 (`eraf-security`)
- API Key 인증 (`eraf-security`)
- OAuth2 연동 (`eraf-security`)

### 인가 (Authorization)
- RBAC (Role-Based Access Control)
- Method-level Security (`@PreAuthorize`, `@HasPermission`)
- URL 패턴 기반 접근 제어

### 보안 헤더
- HTTP Strict Transport Security (HSTS)
- Content-Security-Policy (CSP)
- X-Content-Type-Options: nosniff
- Referrer-Policy: strict-origin-when-cross-origin
- X-Frame-Options: DENY
- Permissions-Policy

### 기타 보안
- 봇 탐지 (User-Agent 기반)
- IP 접근 제어 (화이트리스트/블랙리스트)
- 보안 감사 로깅
- CORS 설정
- CSRF 보호

## Security Best Practices

ERAF Commons 사용 시 권장사항:

1. **JWT 시크릿 키**: 환경변수 또는 Vault로 관리 (소스코드에 포함하지 않음)
2. **CORS**: 프로덕션에서 `allowedOrigins`에 와일드카드(`*`) 사용 금지
3. **CSRF**: REST API 외 웹 애플리케이션에서는 CSRF 보호 활성화
4. **로깅**: 민감한 정보(패스워드, 토큰)가 로그에 포함되지 않도록 주의
5. **의존성**: `mvn versions:display-dependency-updates`로 정기적 업데이트
