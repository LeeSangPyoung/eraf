# ERAF Gateway Builder - 빌드 예제 모음

실제 사용 시나리오별 빌드 명령어 모음입니다.

## 📋 기본 빌드 명령어

### 모든 기능 포함 (기본)
```bash
cd eraf-api-gateway/eraf-gateway-builder
mvn clean package
```

**결과**: `target/eraf-gateway-1.0.0-SNAPSHOT.jar` (모든 기능 포함)

### 최소 구성
```bash
mvn clean package -P minimal
```

**결과**: Common + Memory Store만 포함된 경량 JAR

---

## 🎯 시나리오별 빌드

### 시나리오 1: 공개 REST API Gateway

**요구사항**:
- Rate Limiting으로 남용 방지
- Bot Detection으로 악의적 봇 차단
- Response Cache로 성능 향상
- 인증 불필요 (공개 API)

```bash
mvn clean package -P minimal,rate-limit,bot-detection,cache,store-memory
```

**실행**:
```bash
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar
```

**설정** (application.yml):
```yaml
eraf:
  gateway:
    rate-limit:
      enabled: true
      default-limit-per-second: 100
    bot-detection:
      enabled: true
      block-bots: true
    cache:
      enabled: true
      default-ttl-seconds: 300
```

---

### 시나리오 2: 내부 마이크로서비스 Gateway

**요구사항**:
- API Key로 서비스 간 인증
- Circuit Breaker로 장애 격리
- Analytics로 모니터링
- 높은 가용성

```bash
mvn clean package -P minimal,api-key,circuit-breaker,analytics,store-memory
```

**실행**:
```bash
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar \
  --eraf.gateway.api-key.enabled=true \
  --eraf.gateway.circuit-breaker.enabled=true \
  --eraf.gateway.analytics.enabled=true
```

---

### 시나리오 3: 외부 파트너 API Gateway

**요구사항**:
- JWT 기반 사용자 인증
- IP 화이트리스트로 접근 제어
- Rate Limiting으로 공정한 사용
- API Key로 파트너 식별

```bash
mvn clean package -P security,store-jpa
```

**실행**:
```bash
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:mysql://localhost:3306/gateway \
  --spring.datasource.username=root \
  --spring.datasource.password=password
```

**설정** (application.yml):
```yaml
eraf:
  gateway:
    jwt:
      enabled: true
      secret-key: ${JWT_SECRET:your-256-bit-secret-key}
    ip-restriction:
      enabled: true
      support-cidr: true
    rate-limit:
      enabled: true
      default-limit-per-second: 50
    api-key:
      enabled: true
```

---

### 시나리오 4: High-Performance Gateway

**요구사항**:
- 최고 성능 필요
- Response Cache 적극 활용
- Circuit Breaker로 빠른 실패
- 최소한의 보안 (Rate Limit만)

```bash
mvn clean package -P performance,store-memory
```

**설정** (application.yml):
```yaml
eraf:
  gateway:
    rate-limit:
      enabled: true
      default-limit-per-second: 1000  # 높은 처리량
    circuit-breaker:
      enabled: true
      default-failure-threshold: 3    # 빠른 차단
      default-open-timeout-ms: 30000  # 빠른 복구
    cache:
      enabled: true
      default-ttl-seconds: 600        # 긴 캐시
      max-cache-size: 10000
```

---

### 시나리오 5: 개발/테스트 환경

**요구사항**:
- 빠른 재시작
- 모든 기능 활성화 (테스트용)
- In-Memory 저장소
- 상세한 로깅

```bash
mvn clean package -P full,store-memory
```

**실행**:
```bash
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar \
  --logging.level.com.eraf.gateway=DEBUG \
  --eraf.gateway.rate-limit.default-limit-per-second=10000
```

---

### 시나리오 6: 프로덕션 환경 (Full Stack)

**요구사항**:
- 모든 기능 활성화
- JPA로 영속성 보장
- 높은 안정성
- 상세한 Analytics

```bash
mvn clean package -P full,store-jpa
```

**실행**:
```bash
java -jar target/eraf-gateway-1.0.0-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:postgresql://db-prod:5432/gateway \
  --spring.datasource.username=${DB_USER} \
  --spring.datasource.password=${DB_PASSWORD} \
  --spring.jpa.hibernate.ddl-auto=none
```

**설정** (application-prod.yml):
```yaml
eraf:
  gateway:
    rate-limit:
      enabled: true
      default-limit-per-second: 100
    api-key:
      enabled: true
    ip-restriction:
      enabled: true
    jwt:
      enabled: true
      secret-key: ${JWT_SECRET}
    circuit-breaker:
      enabled: true
    analytics:
      enabled: true
      retention-days: 90
    cache:
      enabled: true
    bot-detection:
      enabled: true
      block-bots: true

spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

---

## 🔧 고급 빌드 옵션

### 특정 기능만 선택

```bash
# JWT + API Key만
mvn clean package -P minimal,jwt,api-key,store-memory

# Rate Limit + Analytics만
mvn clean package -P minimal,rate-limit,analytics,store-memory

# Cache + Circuit Breaker만
mvn clean package -P minimal,cache,circuit-breaker,store-memory
```

### Docker 이미지 빌드와 함께

```bash
# 1. JAR 빌드
mvn clean package -P security,store-jpa

# 2. Docker 이미지 생성
docker build -t eraf-gateway:security .

# 3. 실행
docker run -p 8080:8080 \
  -e JWT_SECRET=${JWT_SECRET} \
  -e DB_URL=jdbc:postgresql://db:5432/gateway \
  eraf-gateway:security
```

---

## 📊 빌드 결과 확인

### JAR 크기 확인
```bash
ls -lh target/eraf-gateway-*.jar
```

### 포함된 클래스 확인
```bash
jar tf target/eraf-gateway-1.0.0-SNAPSHOT.jar | grep "eraf/gateway/feature"
```

### 의존성 트리 확인
```bash
mvn dependency:tree -P minimal,rate-limit,api-key
```

---

## 🎨 커스텀 조합

### A사 요구사항: JWT + Rate Limit + Cache
```bash
mvn clean package -P minimal,jwt,rate-limit,cache,store-jpa
```

### B사 요구사항: API Key + IP Restriction + Bot Detection
```bash
mvn clean package -P minimal,api-key,ip-restriction,bot-detection,store-memory
```

### C사 요구사항: 모든 보안 기능 + Analytics
```bash
mvn clean package -P security,analytics,store-jpa
```

---

## 🚀 CI/CD 파이프라인 예제

### Jenkins Pipeline

```groovy
pipeline {
    agent any

    parameters {
        choice(name: 'BUILD_PROFILE',
               choices: ['full', 'minimal', 'security', 'performance'],
               description: 'Gateway Build Profile')
        choice(name: 'STORAGE',
               choices: ['store-memory', 'store-jpa'],
               description: 'Storage Implementation')
    }

    stages {
        stage('Build') {
            steps {
                sh "mvn clean package -P ${params.BUILD_PROFILE},${params.STORAGE}"
            }
        }

        stage('Test') {
            steps {
                sh "java -jar target/eraf-gateway-*.jar --spring.profiles.active=test &"
                sh "sleep 10"
                sh "curl http://localhost:8080/health"
            }
        }

        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar'
            }
        }
    }
}
```

### GitHub Actions

```yaml
name: Build Gateway

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        profile: [minimal, security, performance, full]

    steps:
      - uses: actions/checkout@v2

      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build with Maven
        run: |
          cd eraf-api-gateway/eraf-gateway-builder
          mvn clean package -P ${{ matrix.profile }},store-memory

      - name: Upload JAR
        uses: actions/upload-artifact@v2
        with:
          name: gateway-${{ matrix.profile }}
          path: target/eraf-gateway-*.jar
```

---

## 💡 팁 & 트릭

### 빌드 시간 단축
```bash
# 테스트 스킵
mvn clean package -P minimal,rate-limit -DskipTests

# 병렬 빌드
mvn clean package -P full -T 4
```

### 특정 버전으로 빌드
```bash
mvn clean package -P security -Drevision=1.2.3
```

### 로컬 리포지토리 사용
```bash
mvn clean package -P full -o  # Offline mode
```

---

## 🐛 트러블슈팅

### Profile이 적용되지 않을 때
```bash
# 활성화된 Profile 확인
mvn help:active-profiles -P minimal,rate-limit
```

### 의존성 충돌
```bash
# 의존성 분석
mvn dependency:analyze -P full
mvn dependency:tree -P full > deps.txt
```

### 빌드 실패 시
```bash
# 상세 로그
mvn clean package -P minimal,rate-limit -X

# 특정 모듈만 빌드
cd ../eraf-gateway-feature-rate-limit
mvn clean install
cd ../eraf-gateway-builder
mvn clean package -P minimal,rate-limit
```
