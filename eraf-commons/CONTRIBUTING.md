# Contributing to ERAF Commons

ERAF Commons 프로젝트에 기여해 주셔서 감사합니다.

## 개발 환경

- **Java**: 21 (LTS)
- **Spring Boot**: 3.2.11
- **Maven**: 3.8+
- **IDE**: IntelliJ IDEA 권장

## 빌드

```bash
# 전체 빌드
mvn clean install

# 테스트 제외 빌드
mvn clean install -DskipTests

# 특정 모듈만 빌드
mvn clean install -pl eraf-core -am
```

## 코드 품질 도구

```bash
# Checkstyle 검사
mvn checkstyle:check

# SpotBugs 정적 분석
mvn compile spotbugs:check

# JaCoCo 커버리지 리포트
mvn test jacoco:report

# 의존성 버전 확인
mvn versions:display-dependency-updates

# Enforcer 검증 (버전 충돌, Maven/Java 버전)
mvn enforcer:enforce
```

## 브랜치 전략

| 브랜치 | 용도 |
|--------|------|
| `main` | 프로덕션 릴리스 |
| `develop` | 개발 통합 |
| `feature/*` | 기능 개발 |
| `bugfix/*` | 버그 수정 |
| `release/*` | 릴리스 준비 |

## 커밋 메시지 규칙

```
<type>(<scope>): <subject>

<body>
```

**Type**: feat, fix, refactor, test, docs, chore, perf
**Scope**: 모듈명 (예: core, security, kafka)

예시:
```
feat(workflow): add JPA persistence for workflow instances
fix(outbox): handle concurrent message processing
test(report): add ReportData builder tests
```

## 모듈 추가 가이드

1. `eraf-<module-name>` 디렉토리 생성
2. `pom.xml`에 부모 POM 설정 (`com.eraf:eraf-commons`)
3. 루트 `pom.xml`의 `<modules>` 블록에 모듈 추가
4. `eraf-bom/pom.xml`에 의존성 등록
5. AutoConfiguration 클래스 생성 및 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 등록
6. `README.md` 작성
7. 단위 테스트 작성 (JaCoCo 70% 이상)

## 테스트

- **JUnit 5** + **Mockito** + **AssertJ** 사용
- `@DisplayName`에 한글 설명 추가
- 모든 public 메서드에 대한 테스트 작성
- 최소 커버리지: 70% (JaCoCo)

## 코드 리뷰 체크리스트

- [ ] 기존 패턴/컨벤션을 따르는가?
- [ ] AutoConfiguration이 `@ConditionalOnMissingBean`으로 보호되는가?
- [ ] 설정 프리픽스가 `eraf.*`를 따르는가?
- [ ] META-INF에 AutoConfiguration이 등록되었는가?
- [ ] BOM에 모듈이 등록되었는가?
- [ ] 테스트 커버리지가 70% 이상인가?
- [ ] 불필요한 의존성이 추가되지 않았는가?
