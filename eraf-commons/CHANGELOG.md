# Changelog

All notable changes to ERAF Commons will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0-SNAPSHOT] - 2026-02-19

### Added
- **47 modules** organized into 11 categories
- **Core (10 modules)**: eraf-core, eraf-core-crypto, eraf-core-util, eraf-core-exception, eraf-core-validation, eraf-core-resilience, eraf-core-async, eraf-core-i18n, eraf-core-http, eraf-core-system
- **Web (5 modules)**: eraf-web, eraf-security, eraf-session, eraf-swagger, eraf-gateway
- **Data (6 modules)**: eraf-data-jpa, eraf-data-mybatis, eraf-data-redis, eraf-data-elasticsearch, eraf-data-cache, eraf-data-mongo
- **Messaging (2 modules)**: eraf-messaging-kafka, eraf-messaging-rabbitmq
- **Integration (6 modules)**: eraf-integration-ftp, eraf-integration-tcp, eraf-integration-s3, eraf-integration-http, eraf-integration-grpc, eraf-integration-websocket
- **Processing (7 modules)**: eraf-batch, eraf-scheduler, eraf-statemachine, eraf-saga, eraf-outbox, eraf-workflow, eraf-report
- **Observability (3 modules)**: eraf-actuator, eraf-observability, eraf-notification
- **Document & Media (4 modules)**: eraf-excel, eraf-pdf, eraf-barcode, eraf-image
- **Other (3 modules)**: eraf-config, eraf-feature-flag, eraf-test
- **BOM (1 module)**: eraf-bom (46 modules registered)
- Enterprise patterns: Saga, StateMachine, Workflow, Outbox, Feature Flag, Distributed Lock
- Security: JWT, API Key, OAuth2, RBAC, Bot Detection, IP Access Control
- Observability: Actuator health indicators, OpenTelemetry tracing, metrics collection
- CI pipeline with GitHub Actions
- Code quality tools: JaCoCo (70%), Checkstyle, SpotBugs

### Fixed
- Spring Cloud version conflict (2024.0.0 -> 2023.0.3 unified)
- WorkflowContext defensive copy bug with immutable maps
- AutoConfiguration loading order between Web and JPA modules

### Security
- Added HSTS, CSP, X-Content-Type-Options, Referrer-Policy, Permissions-Policy headers
- All SecurityFilterChain configurations include comprehensive security headers
- Kafka/RabbitMQ DLQ properly wired for failed message handling
