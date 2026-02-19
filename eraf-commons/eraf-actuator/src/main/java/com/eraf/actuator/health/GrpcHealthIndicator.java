package com.eraf.actuator.health;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.List;

/**
 * gRPC 채널 상태 확인 Health Indicator
 */
public class GrpcHealthIndicator implements HealthIndicator {

    private final List<ManagedChannel> channels;

    public GrpcHealthIndicator(List<ManagedChannel> channels) {
        this.channels = channels;
    }

    @Override
    public Health health() {
        if (channels == null || channels.isEmpty()) {
            return Health.up()
                    .withDetail("grpc", "no channels configured")
                    .build();
        }

        int totalChannels = channels.size();
        int readyChannels = 0;

        for (ManagedChannel channel : channels) {
            ConnectivityState state = channel.getState(false);
            if (state == ConnectivityState.READY || state == ConnectivityState.IDLE) {
                readyChannels++;
            }
        }

        if (readyChannels == totalChannels) {
            return Health.up()
                    .withDetail("grpc", "all channels available")
                    .withDetail("totalChannels", totalChannels)
                    .withDetail("readyChannels", readyChannels)
                    .build();
        } else if (readyChannels > 0) {
            return Health.up()
                    .withDetail("grpc", "partial channels available")
                    .withDetail("totalChannels", totalChannels)
                    .withDetail("readyChannels", readyChannels)
                    .build();
        } else {
            return Health.down()
                    .withDetail("grpc", "no channels available")
                    .withDetail("totalChannels", totalChannels)
                    .withDetail("readyChannels", 0)
                    .build();
        }
    }
}
