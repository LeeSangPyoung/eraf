# ERAF Gateway Store - JPA

JPA/Hibernate 기반 영구 저장소 구현체입니다.

## 특징

- 데이터베이스 기반 영구 저장
- 애플리케이션 재시작 후에도 데이터 유지
- 다양한 RDBMS 지원 (MySQL, PostgreSQL, Oracle, H2 등)
- 클러스터 환경 지원

## 지원 Entity

- `ApiKeyEntity` - API Key 정보
- `RateLimitRuleEntity` - Rate Limit 규칙
- `IpRestrictionRuleEntity` - IP 제한 규칙
- `AnalyticsEventEntity` - 분석 이벤트

## 설정

```yaml
eraf:
  gateway:
    store-type: jpa

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gateway
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-gateway-store-jpa</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- 데이터베이스 드라이버 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>
```

## 스키마

테이블은 JPA `ddl-auto: update` 설정으로 자동 생성됩니다.

수동 생성이 필요한 경우 `schema.sql`을 참조하세요.
