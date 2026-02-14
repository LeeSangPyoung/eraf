
================================================================================
ERAF Commons 모듈 비교 분석 결과 (최종)
================================================================================

Excel 파일: /d/workspace_eraf/eraf2/eraf-commons/ERAF_Commons_개발목록 (5).xlsx
분석 기준: Excel P1-P5 우선순위 항목만 포함
분석 날짜: 2026-02-12

================================================================================
요약 통계
================================================================================
- Excel 모듈 수: 33개
- 현재 구현 모듈 수: 45개
- 일치하는 모듈: 32개
- 삭제 대상 (Excel에 없음): 13개
- 추가 필요 (Excel에만 있음): 1개

================================================================================
1. 삭제 대상 모듈 (13개) - Excel에 없음
================================================================================

### 1-1. 유지 권장 (Excel 기준과 다르지만 실제로 필요) - 11개

┌─────────────────────────┬────────────────────────────────────────┐
│ 모듈명                  │ 이유 및 권장사항                       │
└─────────────────────────┴────────────────────────────────────────┘

eraf-bom
  - 이유: BOM(Bill of Materials)은 Maven 의존성 버전 관리용
  - Excel 대응: Excel에 명시되지 않음 (인프라 모듈)
  - 권장: **유지 필수** (기술적으로 필요)

eraf-core-async
  - 이유: 비동기 처리 전담 모듈
  - Excel 대응: No.19 "비동기 처리" (eraf-core 내)
  - 권장: **유지** (세분화가 더 좋은 설계)

eraf-core-crypto
  - 이유: 암호화/보안 전담 모듈
  - Excel 대응: No.12 "암호화/보안" (eraf-core 내)
  - 권장: **유지** (보안 모듈 분리는 모범 사례)

eraf-core-exception
  - 이유: 예외 처리 전담 모듈
  - Excel 대응: No.1 "예외 처리" (eraf-core 내)
  - 권장: **유지** (핵심 기능 분리)

eraf-core-http
  - 이유: HTTP 통신 전담 모듈
  - Excel 대응: No.17 "HTTP 통신" (eraf-core 내)
  - 권장: **유지** (독립적 사용 가능)

eraf-core-i18n
  - 이유: 다국어 처리 전담 모듈
  - Excel 대응: No.10 "다국어 (i18n)" (eraf-core 내)
  - 권장: **유지** (독립적 기능)

eraf-core-resilience
  - 이유: 복원력 패턴(Circuit Breaker 등) 전담 모듈
  - Excel 대응: No.18 "복원력 패턴" (eraf-core 내)
  - 권장: **유지** (마이크로서비스에 필수)

eraf-core-system
  - 이유: 요청 컨텍스트, 시스템 유틸리티
  - Excel 대응: No.6 "요청 컨텍스트" (eraf-core 내)
  - 권장: **유지** (시스템 레벨 기능)

eraf-core-util
  - 이유: 유틸리티 클래스 모음
  - Excel 대응: No.4 "유틸리티" (eraf-core 내)
  - 권장: **유지** (재사용성 높음)

eraf-core-validation
  - 이유: 입력값 검증 전담 모듈
  - Excel 대응: No.3 "입력값 검증" (eraf-core 내)
  - 권장: **유지** (보안 및 데이터 품질)

eraf-test
  - 이유: 테스트 유틸리티
  - Excel 대응: No.83-84 "테스트 유틸리티" (eraf-test)
  - 권장: **유지** (Excel에도 있음, 일치함)
  - 참고: 실제로는 "일치" 카테고리로 이동해야 함


### 1-2. 삭제 또는 통합 검토 필요 - 2개

eraf-observability
  - 이유: Excel No.69-72에 "헬스체크", "메트릭 수집", "분산 추적", 
          "OpenTelemetry 연동"이 모두 eraf-actuator에 포함됨
  - 현재 상태: eraf-observability와 eraf-actuator 두 모듈이 공존
  - 권장: **삭제** 또는 eraf-actuator로 통합
  - 조치: 두 모듈의 코드를 비교하여 중복 확인 후 결정

eraf-outbox
  - 이유: Excel No.56 "Outbox 패턴"이 eraf-messaging-kafka의 
          하위 항목으로 포함됨
  - 현재 상태: 별도 모듈로 분리되어 있음
  - 권장: **삭제** 후 eraf-messaging-kafka로 통합
         또는 Excel에 별도 모듈로 추가
  - 조치: Outbox 패턴을 범용적으로 사용한다면 유지, 
         Kafka 전용이면 통합


### 1-3. 역할 불명확 - 1개

eraf-web
  - 이유: Excel "C. Web" 카테고리는 있지만 eraf-web 항목은 없음
  - Excel 대응: eraf-swagger만 존재 (No.37)
  - 현재 상태: 모듈은 존재함
  - 권장: eraf-web의 실제 기능 확인 후
    - 옵션 1: Spring Web 공통 기능이면 **유지** 및 Excel에 추가
    - 옵션 2: 불필요하면 **삭제**
  - 조치: eraf-web/src 내용 확인 필요


================================================================================
2. 추가 필요 모듈 (1개) - Excel에만 있음
================================================================================

eraf-gateway
  - Excel: No.38 "API Gateway" [P4 우선순위]
  - 카테고리: C. Web
  - 현재 상태: 미구현
  - 권장: **P4 우선순위이므로 현재는 추가하지 않음**
  - 참고: eraf-openapi-gateway 프로젝트가 별도로 존재함
          (d:/workspace_eraf/eraf2/eraf-openapi-gateway/)
  - 조치: eraf-commons의 eraf-gateway는 Gateway 클라이언트 라이브러리로
         필요 시 추가 (현재는 불필요)


================================================================================
3. 일치하는 모듈 (32개) - 정상
================================================================================

다음 모듈들은 Excel과 완벽히 일치하며 정상적으로 구현됨:

P1 우선순위 (8개):
  - eraf-core (27개 항목)
  - eraf-data-jpa (11개 항목)
  - eraf-security (8개 항목)
  - eraf-actuator (4개 항목 - 헬스체크만 P1)
  - eraf-swagger (1개 항목)
  - eraf-excel (1개 항목)
  - (eraf-test도 P2-P3로 포함되어 실제로는 일치)

P2 우선순위 (5개):
  - eraf-notification (5개 항목 중 이메일만 P2)
  - eraf-scheduler (1개 항목)
  - eraf-data-cache (1개 항목)
  - eraf-pdf (1개 항목)

P3 우선순위 (12개):
  - eraf-data-jpa (멀티테넌시 등 P3 항목 포함)
  - eraf-security (API Key, IP 접근제어 등 P3 항목 포함)
  - eraf-data-redis (1개 항목)
  - eraf-data-mybatis (1개 항목)
  - eraf-messaging-kafka (2개 항목 중 1개 P3)
  - eraf-messaging-rabbitmq (1개 항목)
  - eraf-integration-http (1개 항목)
  - eraf-integration-s3 (1개 항목)
  - eraf-batch (1개 항목)
  - eraf-session (1개 항목)
  - eraf-notification (SMS, Slack/Teams 등 P3 항목 포함)
  - eraf-image (1개 항목)

P4 우선순위 (10개):
  - eraf-data-jpa (멀티 데이터소스 P4)
  - eraf-data-elasticsearch (1개 항목)
  - eraf-messaging-kafka (Outbox 패턴 P4)
  - eraf-integration-ftp (1개 항목)
  - eraf-integration-websocket (1개 항목)
  - eraf-statemachine (1개 항목)
  - eraf-saga (1개 항목)
  - eraf-notification (푸시 알림 P4)
  - eraf-barcode (1개 항목)
  - eraf-config (1개 항목)

P5 우선순위 (5개):
  - eraf-core (데이터 보존 정책 P5)
  - eraf-data-mongo (1개 항목)
  - eraf-integration-tcp (1개 항목)
  - eraf-integration-grpc (1개 항목)
  - eraf-workflow (1개 항목)
  - eraf-report (1개 항목)


================================================================================
4. 최종 권장 조치사항
================================================================================

### 즉시 조치 (필수)

1. eraf-test를 "일치" 카테고리로 재분류
   - 실수: eraf-test는 Excel No.83-84와 일치함

2. eraf-web의 역할 확인
   total 0
drwxr-xr-x 1 leesp 197121 0  2월  5 09:25 .
drwxr-xr-x 1 leesp 197121 0  2월 12 13:20 ..
drwxr-xr-x 1 leesp 197121 0  2월  4 23:47 main
drwxr-xr-x 1 leesp 197121 0  2월  5 09:25 test
   - 필요하면 유지, 불필요하면 삭제


### 중기 조치 (검토 후 결정)

3. eraf-observability vs eraf-actuator 중복 확인
   - 두 모듈의 기능 비교
   - 중복되면 eraf-actuator로 통합

4. eraf-outbox 위치 결정
   - 범용 패턴이면 유지
   - Kafka 전용이면 eraf-messaging-kafka로 통합


### 장기 조치 (현재는 불필요)

5. eraf-gateway 추가 여부 결정
   - P4 우선순위이므로 현재는 미구현 유지
   - 필요 시 eraf-openapi-gateway와 연계


### Excel 업데이트 권장

6. Excel에 다음 내용 반영 권장:
   - eraf-core의 세분화된 하위 모듈 명시 (또는 통합된 것으로 수용)
   - eraf-bom 추가 (인프라 모듈로)
   - eraf-observability 추가 또는 eraf-actuator로 통합 명시
   - eraf-outbox 추가 또는 eraf-messaging-kafka에 통합 명시
   - eraf-web 추가 또는 삭제 결정


================================================================================
5. 결론
================================================================================

현재 구현된 45개 모듈 중:
- **유지 필요: 43개** (eraf-observability, eraf-web 검토 제외)
- **삭제 검토: 2개** (eraf-observability, eraf-outbox - 중복 가능성)
- **추가 불필요: 1개** (eraf-gateway - P4 우선순위)

Excel 기준이 "큰 모듈" 단위이고, 실제 구현은 "세분화된 모듈"로 되어 있어
일부 불일치가 있지만, 이는 **더 나은 모듈화 설계**로 볼 수 있습니다.

Excel을 "기능 요구사항 목록"으로, pom.xml을 "실제 모듈 구조"로 이해하면
현재 구조가 합리적입니다.

권장: Excel을 업데이트하여 실제 모듈 구조와 동기화
