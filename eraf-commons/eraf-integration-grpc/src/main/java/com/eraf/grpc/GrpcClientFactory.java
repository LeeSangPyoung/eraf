package com.eraf.grpc;

import com.eraf.grpc.config.ErafGrpcProperties;
import com.eraf.grpc.interceptor.ClientMetadataInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * gRPC 클라이언트 팩토리
 *
 * gRPC 채널 관리 및 메타데이터 전파를 포함한 클라이언트 생성을 지원합니다.
 * 애플리케이션 종료 시 {@link DisposableBean}을 통해 모든 채널을 안전하게 종료합니다.
 */
public class GrpcClientFactory implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(GrpcClientFactory.class);

    private final ErafGrpcProperties properties;
    private final Map<String, ManagedChannel> channels = new ConcurrentHashMap<>();

    public GrpcClientFactory(ErafGrpcProperties properties) {
        this.properties = properties;
    }

    /**
     * 지정 호스트:포트에 대한 gRPC 채널 생성 또는 재사용
     */
    public ManagedChannel getChannel(String target) {
        return channels.computeIfAbsent(target, this::createChannel);
    }

    /**
     * gRPC 채널 생성
     * TLS 설정이 활성화된 경우 TLS 채널을, 아닌 경우 plaintext 채널을 생성합니다.
     */
    private ManagedChannel createChannel(String target) {
        log.info("Creating gRPC channel to: {}", target);

        ErafGrpcProperties.Tls tls = properties.getTls();
        if (tls != null && tls.isEnabled()) {
            return createTlsChannel(target, tls);
        }

        return ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .intercept(new ClientMetadataInterceptor())
                .maxInboundMessageSize(properties.getServer().getMaxMessageSize())
                .build();
    }

    /**
     * TLS가 적용된 gRPC 채널 생성
     */
    private ManagedChannel createTlsChannel(String target, ErafGrpcProperties.Tls tls) {
        try {
            SslContextBuilder sslBuilder = GrpcSslContexts.forClient();

            if (tls.getTrustCertPath() != null && !tls.getTrustCertPath().isEmpty()) {
                sslBuilder.trustManager(new File(tls.getTrustCertPath()));
            }

            if (tls.getCertChainPath() != null && !tls.getCertChainPath().isEmpty()
                    && tls.getPrivateKeyPath() != null && !tls.getPrivateKeyPath().isEmpty()) {
                sslBuilder.keyManager(
                        new File(tls.getCertChainPath()),
                        new File(tls.getPrivateKeyPath()));
            }

            SslContext sslContext = sslBuilder.build();

            log.info("Creating TLS-enabled gRPC channel to: {}", target);

            return NettyChannelBuilder.forTarget(target)
                    .sslContext(sslContext)
                    .intercept(new ClientMetadataInterceptor())
                    .maxInboundMessageSize(properties.getServer().getMaxMessageSize())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create TLS gRPC channel to: " + target, e);
        }
    }

    /**
     * 특정 채널 종료
     */
    public void closeChannel(String target) {
        ManagedChannel channel = channels.remove(target);
        if (channel != null) {
            channel.shutdown();
            try {
                channel.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 모든 채널 종료 (애플리케이션 종료 시)
     */
    public void closeAll() {
        channels.forEach((target, channel) -> {
            log.info("Closing gRPC channel to: {}", target);
            channel.shutdown();
            try {
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("gRPC channel to {} did not terminate in time, forcing shutdown", target);
                    channel.shutdownNow();
                }
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
        channels.clear();
    }

    @Override
    public void destroy() {
        log.info("Destroying GrpcClientFactory, closing {} channels", channels.size());
        closeAll();
    }
}
