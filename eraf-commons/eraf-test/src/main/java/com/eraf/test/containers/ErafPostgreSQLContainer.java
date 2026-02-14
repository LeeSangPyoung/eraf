package com.eraf.test.containers;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * ERAF PostgreSQL TestContainer with sensible defaults
 *
 * <p>Usage:</p>
 * <pre>
 * {@code @Container}
 * static ErafPostgreSQLContainer postgres = new ErafPostgreSQLContainer();
 *
 * // Or with custom version
 * static ErafPostgreSQLContainer postgres = new ErafPostgreSQLContainer("15-alpine");
 * </pre>
 */
public class ErafPostgreSQLContainer extends PostgreSQLContainer<ErafPostgreSQLContainer> {

    private static final String DEFAULT_IMAGE = "postgres:15-alpine";
    private static final String DEFAULT_DATABASE = "test_db";
    private static final String DEFAULT_USERNAME = "test_user";
    private static final String DEFAULT_PASSWORD = "test_password";

    public ErafPostgreSQLContainer() {
        this(DEFAULT_IMAGE);
    }

    public ErafPostgreSQLContainer(String dockerImageName) {
        super(DockerImageName.parse(dockerImageName).asCompatibleSubstituteFor("postgres"));

        // Configure default settings
        withDatabaseName(DEFAULT_DATABASE);
        withUsername(DEFAULT_USERNAME);
        withPassword(DEFAULT_PASSWORD);

        // Performance optimization for tests
        withCommand(
            "postgres",
            "-c", "fsync=off",
            "-c", "synchronous_commit=off",
            "-c", "full_page_writes=off"
        );

        // Reusable container across tests (optional)
        withReuse(false);
    }

    /**
     * Create reusable container (faster for multiple test classes)
     */
    public static ErafPostgreSQLContainer createReusable() {
        ErafPostgreSQLContainer container = new ErafPostgreSQLContainer();
        container.withReuse(true);
        return container;
    }

    /**
     * Get JDBC URL for Spring Boot configuration
     */
    public String getJdbcUrlForSpring() {
        return getJdbcUrl();
    }

    /**
     * Get connection properties for Spring Boot
     */
    public String getSpringDatasourceUrl() {
        return String.format("spring.datasource.url=%s", getJdbcUrl());
    }

    public String getSpringDatasourceUsername() {
        return String.format("spring.datasource.username=%s", getUsername());
    }

    public String getSpringDatasourcePassword() {
        return String.format("spring.datasource.password=%s", getPassword());
    }
}
