package com.eraf.actuator.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;

/**
 * AWS S3 Health Indicator
 * <p>
 * S3 엔드포인트 연결 가능 여부를 확인합니다.
 * 기본적으로 S3 서비스 엔드포인트에 대한 연결 확인을 수행합니다.
 */
public class S3HealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(S3HealthIndicator.class);

    private final String endpoint;
    private final String bucketName;
    private final int timeoutMs;

    public S3HealthIndicator(String endpoint, String bucketName) {
        this(endpoint, bucketName, 5000);
    }

    public S3HealthIndicator(String endpoint, String bucketName, int timeoutMs) {
        this.endpoint = endpoint;
        this.bucketName = bucketName;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public Health health() {
        Instant start = Instant.now();
        try {
            String checkUrl = endpoint.endsWith("/")
                    ? endpoint + bucketName
                    : endpoint + "/" + bucketName;

            HttpURLConnection connection = (HttpURLConnection) URI.create(checkUrl).toURL().openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(timeoutMs);
            connection.setReadTimeout(timeoutMs);

            int responseCode = connection.getResponseCode();
            long responseTime = Duration.between(start, Instant.now()).toMillis();
            connection.disconnect();

            // S3 HEAD bucket: 200=exists, 403=exists but no permission, 404=not found
            if (responseCode == 200 || responseCode == 403) {
                return Health.up()
                        .withDetail("endpoint", endpoint)
                        .withDetail("bucket", bucketName)
                        .withDetail("responseCode", responseCode)
                        .withDetail("responseTime", responseTime + "ms")
                        .build();
            } else {
                return Health.down()
                        .withDetail("endpoint", endpoint)
                        .withDetail("bucket", bucketName)
                        .withDetail("responseCode", responseCode)
                        .withDetail("responseTime", responseTime + "ms")
                        .build();
            }

        } catch (Exception e) {
            long responseTime = Duration.between(start, Instant.now()).toMillis();
            log.error("S3 health check failed: {}", e.getMessage());

            return Health.down()
                    .withDetail("endpoint", endpoint)
                    .withDetail("bucket", bucketName)
                    .withDetail("error", e.getMessage())
                    .withDetail("responseTime", responseTime + "ms")
                    .build();
        }
    }
}
