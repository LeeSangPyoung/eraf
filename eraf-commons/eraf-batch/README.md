# eraf-batch

Spring Batch 자동 설정 및 공통 유틸리티 모듈입니다.

## 주요 기능

- Spring Batch 자동 설정 (JobRepository, JobLauncher)
- 공통 ItemReader/ItemWriter 구현
- 배치 실행 모니터링
- 에러 핸들링 및 재시도

## 사용법

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-batch</artifactId>
</dependency>
```

## 설정

```yaml
eraf:
  batch:
    enabled: true
    chunk-size: 100
    max-retries: 3
```
