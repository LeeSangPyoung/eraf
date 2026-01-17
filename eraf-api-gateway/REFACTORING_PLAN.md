# ERAF API Gateway - 빌드 타임 기능 선택 리팩토링 계획

## 목표
빌드 시 필요한 기능만 선택하여 단일 JAR로 생성하는 구조로 변경

## 현재 구조 vs 목표 구조

### 현재 (Runtime 기능 선택)
```
빌드: 모든 기능 포함된 Full JAR 생성
실행: YAML 설정으로 기능 on/off
문제점: 사용하지 않는 기능도 JAR에 포함되어 용량 증가
```

### 목표 (Build-time 기능 선택)
```
빌드: mvn package -P common,rate-limit,api-key
결과: 선택된 기능만 포함된 경량화 JAR
장점: 필요한 기능만 컴파일, 배포 용량 최소화
```

## 새로운 모듈 구조

```
eraf-api-gateway/
├── pom.xml (부모 POM)
│
├── eraf-gateway-common/                  # 필수 공통 기능
│   ├── 필터 체인 인프라
│   ├── 에러 핸들링 (GatewayException, ErrorResponse)
│   ├── 기본 설정 (Properties 베이스 클래스)
│   ├── 유틸리티 (PathMatcher, HttpUtils 래퍼)
│   └── Service/Repository 인터페이스 정의
│
├── eraf-gateway-feature-rate-limit/      # 기능 1: Rate Limiting
│   ├── RateLimitRule, RateLimitRecord 도메인
│   ├── RateLimitService 인터페이스 및 구현
│   ├── RateLimitFilter
│   └── RateLimitRepository 인터페이스
│
├── eraf-gateway-feature-api-key/         # 기능 2: API Key 인증
│   ├── ApiKey 도메인
│   ├── ApiKeyService 인터페이스 및 구현
│   ├── ApiKeyAuthFilter
│   └── ApiKeyRepository 인터페이스
│
├── eraf-gateway-feature-ip-restriction/  # 기능 3: IP 제한
│   ├── IpRestriction 도메인
│   ├── IpRestrictionService 인터페이스 및 구현
│   ├── IpRestrictionFilter
│   └── IpRestrictionRepository 인터페이스
│
├── eraf-gateway-feature-jwt/             # 기능 4: JWT 검증
│   ├── JwtValidationFilter
│   ├── JwtValidator
│   └── JWT 관련 설정
│
├── eraf-gateway-feature-circuit-breaker/ # 기능 5: Circuit Breaker
│   ├── CircuitBreaker 구현
│   ├── CircuitBreakerFilter
│   └── Circuit Breaker 설정
│
├── eraf-gateway-feature-analytics/       # 기능 6: Analytics
│   ├── ApiCall 도메인
│   ├── AnalyticsService
│   ├── AnalyticsFilter
│   └── Dashboard 생성
│
├── eraf-gateway-feature-cache/           # 기능 7: Response Cache
│   ├── CacheRule, CachedResponse 도메인
│   ├── ResponseCacheService
│   ├── ResponseCacheFilter
│   └── Cache 설정
│
├── eraf-gateway-feature-bot-detection/   # 기능 8: Bot Detection
│   ├── BotDetectionFilter
│   ├── BotDetector
│   └── Bot 설정
│
├── eraf-gateway-feature-transform/       # 기능 9: Request/Response Transform
│   ├── TransformRule 도메인
│   ├── TransformService
│   └── Transform 로직
│
├── eraf-gateway-store-memory/            # 스토리지: In-Memory
│   ├── *MemoryRepository 구현체들
│   └── 각 기능의 Repository 구현
│
├── eraf-gateway-store-jpa/               # 스토리지: JPA
│   ├── *JpaRepository 구현체들
│   ├── JPA Entity 매핑
│   └── 각 기능의 Repository 구현
│
├── eraf-gateway-builder/                 # 빌드 조합 모듈
│   ├── pom.xml (Maven Profile 정의)
│   └── Spring Boot AutoConfiguration
│
└── eraf-gateway-starter/                 # (기존 유지 또는 builder와 통합)
```

## Maven Profile 정의

### 루트 pom.xml에 Profile 정의

```xml
<profiles>
    <!-- 1. Minimal: 공통 기능만 -->
    <profile>
        <id>minimal</id>
        <modules>
            <module>eraf-gateway-common</module>
            <module>eraf-gateway-store-memory</module>
            <module>eraf-gateway-builder</module>
        </modules>
    </profile>

    <!-- 2. Rate Limit 추가 -->
    <profile>
        <id>rate-limit</id>
        <modules>
            <module>eraf-gateway-feature-rate-limit</module>
        </modules>
    </profile>

    <!-- 3. API Key 추가 -->
    <profile>
        <id>api-key</id>
        <modules>
            <module>eraf-gateway-feature-api-key</module>
        </modules>
    </profile>

    <!-- 4. IP Restriction 추가 -->
    <profile>
        <id>ip-restriction</id>
        <modules>
            <module>eraf-gateway-feature-ip-restriction</module>
        </modules>
    </profile>

    <!-- 5. JWT 추가 -->
    <profile>
        <id>jwt</id>
        <modules>
            <module>eraf-gateway-feature-jwt</module>
        </modules>
    </profile>

    <!-- 6. Circuit Breaker 추가 -->
    <profile>
        <id>circuit-breaker</id>
        <modules>
            <module>eraf-gateway-feature-circuit-breaker</module>
        </modules>
    </profile>

    <!-- 7. Analytics 추가 -->
    <profile>
        <id>analytics</id>
        <modules>
            <module>eraf-gateway-feature-analytics</module>
        </modules>
    </profile>

    <!-- 8. Cache 추가 -->
    <profile>
        <id>cache</id>
        <modules>
            <module>eraf-gateway-feature-cache</module>
        </modules>
    </profile>

    <!-- 9. Bot Detection 추가 -->
    <profile>
        <id>bot-detection</id>
        <modules>
            <module>eraf-gateway-feature-bot-detection</module>
        </modules>
    </profile>

    <!-- 10. Transform 추가 -->
    <profile>
        <id>transform</id>
        <modules>
            <module>eraf-gateway-feature-transform</module>
        </modules>
    </profile>

    <!-- 11. JPA Store 추가 -->
    <profile>
        <id>store-jpa</id>
        <modules>
            <module>eraf-gateway-store-jpa</module>
        </modules>
    </profile>

    <!-- 12. Full: 모든 기능 -->
    <profile>
        <id>full</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <modules>
            <module>eraf-gateway-common</module>
            <module>eraf-gateway-feature-rate-limit</module>
            <module>eraf-gateway-feature-api-key</module>
            <module>eraf-gateway-feature-ip-restriction</module>
            <module>eraf-gateway-feature-jwt</module>
            <module>eraf-gateway-feature-circuit-breaker</module>
            <module>eraf-gateway-feature-analytics</module>
            <module>eraf-gateway-feature-cache</module>
            <module>eraf-gateway-feature-bot-detection</module>
            <module>eraf-gateway-feature-transform</module>
            <module>eraf-gateway-store-memory</module>
            <module>eraf-gateway-store-jpa</module>
            <module>eraf-gateway-builder</module>
        </modules>
    </profile>
</profiles>
```

### eraf-gateway-builder/pom.xml (조합 모듈)

```xml
<project>
    <artifactId>eraf-gateway-builder</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <!-- 공통 모듈은 항상 포함 -->
        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-gateway-common</artifactId>
        </dependency>

        <!-- Profile에 따라 선택적으로 포함 -->
        <dependency>
            <groupId>com.eraf</groupId>
            <artifactId>eraf-gateway-feature-rate-limit</artifactId>
            <optional>true</optional>
        </dependency>
        <!-- ... 기타 기능들도 optional로 -->
    </dependencies>

    <profiles>
        <profile>
            <id>rate-limit</id>
            <dependencies>
                <dependency>
                    <groupId>com.eraf</groupId>
                    <artifactId>eraf-gateway-feature-rate-limit</artifactId>
                    <optional>false</optional>
                </dependency>
            </dependencies>
        </profile>
        <!-- ... 기타 프로파일 -->
    </profiles>

    <build>
        <plugins>
            <!-- Spring Boot Maven Plugin으로 실행 가능한 JAR 생성 -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <classifier>exec</classifier>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 빌드 명령어 예시

```bash
# 1. 최소 구성 (common + memory store만)
mvn clean package -P minimal

# 2. Rate Limit + API Key만 포함
mvn clean package -P minimal,rate-limit,api-key

# 3. 보안 기능만 (API Key + JWT + IP Restriction)
mvn clean package -P minimal,api-key,jwt,ip-restriction

# 4. 성능 최적화 기능 (Rate Limit + Circuit Breaker + Cache)
mvn clean package -P minimal,rate-limit,circuit-breaker,cache

# 5. 전체 기능 (기본값)
mvn clean package
# 또는
mvn clean package -P full

# 6. JPA Store 사용
mvn clean package -P minimal,rate-limit,store-jpa
```

## Spring Boot AutoConfiguration 전략

### eraf-gateway-builder의 AutoConfiguration

```java
// ErafGatewayAutoConfiguration.java
@Configuration
@ConditionalOnProperty(prefix = "eraf.gateway", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ErafGatewayAutoConfiguration {

    // Common 기능은 항상 로드
    @Bean
    public FilterChainManager filterChainManager() {
        return new FilterChainManager();
    }

    // 각 기능 모듈이 classpath에 있을 때만 자동 설정
    @Configuration
    @ConditionalOnClass(name = "com.eraf.gateway.ratelimit.RateLimitFilter")
    @Import(RateLimitAutoConfiguration.class)
    static class RateLimitConfiguration {}

    @Configuration
    @ConditionalOnClass(name = "com.eraf.gateway.apikey.ApiKeyAuthFilter")
    @Import(ApiKeyAutoConfiguration.class)
    static class ApiKeyConfiguration {}

    // ... 기타 기능들
}
```

### 각 기능 모듈의 AutoConfiguration

```java
// eraf-gateway-feature-rate-limit/RateLimitAutoConfiguration.java
@Configuration
@ConditionalOnClass(RateLimitFilter.class)
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RateLimitService rateLimitService(RateLimitRepository repository) {
        return new RateLimitServiceImpl(repository);
    }

    @Bean
    public RateLimitFilter rateLimitFilter(RateLimitService service) {
        return new RateLimitFilter(service);
    }
}
```

## 마이그레이션 단계

### Phase 1: Common 모듈 분리
1. eraf-gateway-common 모듈 생성
2. 공통 인프라 코드 이동
   - FilterChain 관련
   - Exception, ErrorResponse
   - Properties 베이스 클래스
   - 공통 유틸리티

### Phase 2: 기능별 모듈 분리 (우선순위별)
1. **Rate Limit** (가장 중요한 기능)
   - eraf-gateway-feature-rate-limit 생성
   - 도메인, 서비스, 필터, 리포지토리 이동
   - AutoConfiguration 작성

2. **API Key**
   - eraf-gateway-feature-api-key 생성
   - 관련 코드 이동

3. **IP Restriction**
   - eraf-gateway-feature-ip-restriction 생성

4. 나머지 기능들 순차적으로 분리

### Phase 3: Builder 모듈 생성
1. eraf-gateway-builder 생성
2. Maven Profile 구성
3. 통합 테스트

### Phase 4: 테스트 및 검증
1. 각 Profile 조합별 빌드 테스트
2. 실행 가능한 JAR 검증
3. 기능 동작 확인

## 기대 효과

### 1. JAR 크기 최적화
```
Full JAR:     ~15MB (모든 기능)
Minimal JAR:  ~3MB  (공통 기능만)
Custom JAR:   ~5-10MB (선택된 기능만)
```

### 2. 의존성 최적화
- JWT 미사용 시 JJWT 라이브러리 제외
- JPA 미사용 시 Hibernate 제외
- 불필요한 전이 의존성 제거

### 3. 보안 향상
- 사용하지 않는 코드가 JAR에 포함되지 않음
- 공격 표면(Attack Surface) 최소화

### 4. 배포 유연성
- 마이크로서비스별 필요 기능만 선택
- 환경별 최적화된 JAR 배포

## 호환성 유지

### 기존 사용자를 위한 하위 호환성
1. `full` 프로파일을 기본값으로 설정
2. 기존 YAML 설정 그대로 사용 가능
3. 단계적 마이그레이션 지원

### 마이그레이션 가이드 제공
```markdown
# 기존 사용자
mvn clean package  # 기존처럼 전체 빌드

# 새로운 방식
mvn clean package -P minimal,rate-limit,api-key  # 필요한 기능만 선택
```

## 다음 단계

1. ✅ 설계 검토 및 승인
2. Phase 1 시작: Common 모듈 분리
3. Phase 2: Rate Limit 기능 분리 (파일럿)
4. 테스트 후 나머지 기능 분리
5. 문서화 및 예제 작성

---

## 참고사항

- 각 기능 모듈은 독립적으로 테스트 가능
- Common 모듈에 대한 의존성만 가짐
- Feature 간 의존성은 최소화 (Cross-cutting concerns는 Common에)
- Store 모듈은 모든 Feature의 Repository 구현체 제공
