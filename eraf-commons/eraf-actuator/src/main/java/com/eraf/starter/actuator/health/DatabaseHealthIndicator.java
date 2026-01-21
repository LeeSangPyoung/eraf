package com.eraf.starter.actuator.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Database 상태 확인 Health Indicator
 */
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final String validationQuery;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this(dataSource, "SELECT 1");
    }

    public DatabaseHealthIndicator(DataSource dataSource, String validationQuery) {
        this.dataSource = dataSource;
        this.validationQuery = validationQuery;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            // Validation query 실행
            connection.prepareStatement(validationQuery).execute();

            return Health.up()
                    .withDetail("database", metaData.getDatabaseProductName())
                    .withDetail("version", metaData.getDatabaseProductVersion())
                    .withDetail("url", metaData.getURL())
                    .withDetail("validationQuery", validationQuery)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "disconnected")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
