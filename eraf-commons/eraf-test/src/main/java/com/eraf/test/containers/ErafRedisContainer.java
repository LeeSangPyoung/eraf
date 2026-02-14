package com.eraf.test.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * ERAF Redis TestContainer with sensible defaults
 *
 * <p>Usage:</p>
 * <pre>
 * {@code @Container}
 * static ErafRedisContainer redis = new ErafRedisContainer();
 * </pre>
 */
public class ErafRedisContainer extends GenericContainer<ErafRedisContainer> {

    private static final String DEFAULT_IMAGE = "redis:7-alpine";
    private static final int DEFAULT_PORT = 6379;

    public ErafRedisContainer() {
        this(DEFAULT_IMAGE);
    }

    public ErafRedisContainer(String dockerImageName) {
        super(DockerImageName.parse(dockerImageName));

        withExposedPorts(DEFAULT_PORT);
        withReuse(false);
    }

    /**
     * Create reusable container
     */
    public static ErafRedisContainer createReusable() {
        ErafRedisContainer container = new ErafRedisContainer();
        container.withReuse(true);
        return container;
    }

    /**
     * Get Redis host
     */
    public String getRedisHost() {
        return getHost();
    }

    /**
     * Get Redis port
     */
    public Integer getRedisPort() {
        return getMappedPort(DEFAULT_PORT);
    }

    /**
     * Get Redis connection string
     */
    public String getRedisUrl() {
        return String.format("redis://%s:%d", getHost(), getMappedPort(DEFAULT_PORT));
    }

    /**
     * Get Spring Boot property for Redis host
     */
    public String getSpringRedisHost() {
        return String.format("spring.data.redis.host=%s", getHost());
    }

    /**
     * Get Spring Boot property for Redis port
     */
    public String getSpringRedisPort() {
        return String.format("spring.data.redis.port=%d", getMappedPort(DEFAULT_PORT));
    }
}
