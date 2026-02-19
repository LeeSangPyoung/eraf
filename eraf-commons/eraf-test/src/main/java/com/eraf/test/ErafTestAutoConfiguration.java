package com.eraf.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * ERAF Test Auto-Configuration
 *
 * <p>테스트 유틸리티를 자동 구성합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>TestContainers (PostgreSQL, MySQL, Kafka, Redis, Elasticsearch)</li>
 *   <li>테스트 데이터 생성기 (DataFaker 기반)</li>
 *   <li>테스트 빌더 (Builder 패턴)</li>
 *   <li>Mock 헬퍼 (MockMvc, WebTestClient)</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // TestContainers
 * {@code @Testcontainers}
 * class MyTest {
 *     {@code @Container}
 *     static ErafPostgreSQLContainer postgres = ErafPostgreSQLContainer.create();
 *
 *     {@code @Container}
 *     static ErafRedisContainer redis = ErafRedisContainer.create();
 * }
 *
 * // 테스트 데이터 생성
 * TestDataGenerator generator = new TestDataGenerator();
 * String name = generator.name();
 * String email = generator.email();
 * LocalDate date = generator.pastDate();
 *
 * // 테스트 빌더
 * User user = TestBuilder.user()
 *     .name("홍길동")
 *     .email("hong@example.com")
 *     .build();
 * </pre>
 *
 * <p>Note: 테스트 유틸리티들은 직접 생성하거나 static 메서드로 사용합니다.</p>
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.testcontainers.containers.GenericContainer")
public class ErafTestAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ErafTestAutoConfiguration.class);

    public ErafTestAutoConfiguration() {
        log.info("ERAF Test module enabled - TestContainers, test data generator, test builders available");
        log.debug("Use ErafPostgreSQLContainer, ErafRedisContainer, TestDataGenerator for testing");
    }
}
