package com.eraf.actuator.health;

import org.bson.Document;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoDB 상태 확인 Health Indicator
 */
public class MongoHealthIndicator implements HealthIndicator {

    private final MongoTemplate mongoTemplate;

    public MongoHealthIndicator(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Health health() {
        try {
            Document result = mongoTemplate.executeCommand(new Document("ping", 1));
            if (result.containsKey("ok") && result.getDouble("ok") == 1.0) {
                return Health.up()
                        .withDetail("mongodb", "connected")
                        .withDetail("database", mongoTemplate.getDb().getName())
                        .build();
            } else {
                return Health.down()
                        .withDetail("mongodb", "unexpected ping response")
                        .withDetail("response", result.toJson())
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("mongodb", "disconnected")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
