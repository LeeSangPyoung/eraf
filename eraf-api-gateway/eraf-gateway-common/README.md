# ERAF Gateway Common

API Gateway의 공통 인프라 및 베이스 클래스를 제공하는 모듈입니다.

## 포함 내용

### 1. 예외 처리 (exception)
- `GatewayException`: Gateway 기본 예외 클래스
- `GatewayErrorCode`: 공통 에러 코드 정의

### 2. 유틸리티 (util)
- `GatewayResponseUtils`: HTTP 응답 유틸리티

### 3. 필터 인프라 (filter)
- `GatewayFilter`: 필터 베이스 클래스 (경로 제외, 활성화 여부 체크)
- `FilterOrder`: 필터 실행 순서 상수 정의

### 4. 설정 (config)
- `GatewayProperties`: Gateway 공통 설정 클래스

### 5. Repository (repository)
- `GatewayRepository<T, ID>`: Repository 공통 인터페이스

## 의존성

이 모듈은 다음에만 의존합니다:
- `eraf-core`: ERAF 핵심 유틸리티
- `spring-web`: Spring Web 지원
- `jakarta.servlet-api`: Servlet API (provided)
- `slf4j-api`: 로깅
- `lombok`: 보일러플레이트 코드 제거 (provided)

JWT, JPA 등의 특정 기능 의존성은 **포함하지 않습니다**.

## 사용 방법

다른 Gateway 기능 모듈은 이 모듈을 의존성으로 추가해야 합니다.

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-common</artifactId>
</dependency>
```

## 빌드

```bash
mvn clean install
```

## 특징

- 경량: 핵심 인프라만 포함
- 독립적: 특정 기능에 의존하지 않음
- 확장 가능: 모든 기능 모듈의 베이스
