# ERAF Gateway Store - Memory

In-Memory 기반 저장소 구현체입니다.

## 특징

- 별도의 외부 저장소 없이 동작
- 애플리케이션 재시작 시 데이터 초기화
- 개발/테스트 환경에 적합
- 단일 인스턴스 환경에 적합

## 포함 Repository

- `InMemoryApiKeyRepository` - API Key 저장
- `InMemoryRateLimitRuleRepository` - Rate Limit 규칙 저장
- `InMemoryRateLimitRecordRepository` - Rate Limit 기록 저장
- `InMemoryIpRestrictionRepository` - IP 제한 규칙 저장
- `InMemoryAnalyticsRepository` - 분석 데이터 저장
- `InMemoryResponseCacheRepository` - 응답 캐시 저장
- `InMemoryOAuth2TokenRepository` - OAuth2 토큰 저장
- `InMemoryOAuth2ClientRepository` - OAuth2 클라이언트 저장

## 설정

```yaml
eraf:
  gateway:
    store-type: memory  # 기본값
```

## 사용 시 주의사항

- 클러스터 환경에서는 Redis나 JPA 저장소 사용 권장
- 대용량 데이터 저장 시 메모리 사용량 고려 필요
