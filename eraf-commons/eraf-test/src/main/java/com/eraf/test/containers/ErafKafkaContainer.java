package com.eraf.test.containers;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * ERAF Kafka TestContainer with sensible defaults
 *
 * <p>Usage:</p>
 * <pre>
 * {@code @Container}
 * static ErafKafkaContainer kafka = new ErafKafkaContainer();
 * </pre>
 */
public class ErafKafkaContainer extends KafkaContainer {

    private static final String DEFAULT_IMAGE = "confluentinc/cp-kafka:7.5.3";

    public ErafKafkaContainer() {
        this(DEFAULT_IMAGE);
    }

    public ErafKafkaContainer(String dockerImageName) {
        super(DockerImageName.parse(dockerImageName));
    }

    /**
     * Create reusable container (faster for multiple test classes)
     */
    public static ErafKafkaContainer createReusable() {
        ErafKafkaContainer container = new ErafKafkaContainer();
        container.withReuse(true);
        return container;
    }

    /**
     * Get bootstrap servers for Spring Boot configuration
     */
    public String getBootstrapServersForSpring() {
        return getBootstrapServers();
    }

    /**
     * Get Spring Boot property string
     */
    public String getSpringKafkaBootstrapServers() {
        return String.format("spring.kafka.bootstrap-servers=%s", getBootstrapServers());
    }
}
