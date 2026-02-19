package com.eraf.grpc;

import com.eraf.grpc.config.ErafGrpcProperties;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("GrpcClientFactory 테스트")
class GrpcClientFactoryTest {

    private ErafGrpcProperties properties;
    private GrpcClientFactory factory;

    @BeforeEach
    void setUp() {
        properties = new ErafGrpcProperties();
        factory = new GrpcClientFactory(properties);
    }

    @Test
    @DisplayName("팩토리 생성")
    void create_ShouldSucceed() {
        assertThat(factory).isNotNull();
    }

    @Test
    @DisplayName("closeChannel - 존재하지 않는 타겟은 무시")
    void closeChannel_NonExistentTarget_ShouldNotFail() {
        factory.closeChannel("non-existent:9090");
        // no exception
    }

    @Test
    @DisplayName("closeChannel - 존재하는 채널 종료")
    @SuppressWarnings("unchecked")
    void closeChannel_ExistingChannel_ShouldShutdown() throws Exception {
        ManagedChannel mockChannel = mock(ManagedChannel.class);
        when(mockChannel.shutdown()).thenReturn(mockChannel);
        when(mockChannel.awaitTermination(5, TimeUnit.SECONDS)).thenReturn(true);

        // 리플렉션으로 channels 맵에 직접 추가
        Field channelsField = GrpcClientFactory.class.getDeclaredField("channels");
        channelsField.setAccessible(true);
        Map<String, ManagedChannel> channels = (Map<String, ManagedChannel>) channelsField.get(factory);
        channels.put("test:9090", mockChannel);

        factory.closeChannel("test:9090");

        verify(mockChannel).shutdown();
        verify(mockChannel).awaitTermination(5, TimeUnit.SECONDS);
        assertThat(channels).doesNotContainKey("test:9090");
    }

    @Test
    @DisplayName("closeChannel - 타임아웃 시 강제 종료")
    @SuppressWarnings("unchecked")
    void closeChannel_WhenTimeout_ShouldForceShutdown() throws Exception {
        ManagedChannel mockChannel = mock(ManagedChannel.class);
        when(mockChannel.shutdown()).thenReturn(mockChannel);
        when(mockChannel.awaitTermination(5, TimeUnit.SECONDS))
                .thenThrow(new InterruptedException());
        when(mockChannel.shutdownNow()).thenReturn(mockChannel);

        Field channelsField = GrpcClientFactory.class.getDeclaredField("channels");
        channelsField.setAccessible(true);
        Map<String, ManagedChannel> channels = (Map<String, ManagedChannel>) channelsField.get(factory);
        channels.put("timeout:9090", mockChannel);

        factory.closeChannel("timeout:9090");

        verify(mockChannel).shutdownNow();
    }

    @Test
    @DisplayName("closeAll - 모든 채널 종료")
    @SuppressWarnings("unchecked")
    void closeAll_ShouldShutdownAllChannels() throws Exception {
        ManagedChannel ch1 = mock(ManagedChannel.class);
        ManagedChannel ch2 = mock(ManagedChannel.class);
        when(ch1.shutdown()).thenReturn(ch1);
        when(ch2.shutdown()).thenReturn(ch2);
        when(ch1.awaitTermination(5, TimeUnit.SECONDS)).thenReturn(true);
        when(ch2.awaitTermination(5, TimeUnit.SECONDS)).thenReturn(true);

        Field channelsField = GrpcClientFactory.class.getDeclaredField("channels");
        channelsField.setAccessible(true);
        Map<String, ManagedChannel> channels = (Map<String, ManagedChannel>) channelsField.get(factory);
        channels.put("host1:9090", ch1);
        channels.put("host2:9090", ch2);

        factory.closeAll();

        verify(ch1).shutdown();
        verify(ch2).shutdown();
        assertThat(channels).isEmpty();
    }

    @Test
    @DisplayName("destroy는 closeAll 호출")
    @SuppressWarnings("unchecked")
    void destroy_ShouldCallCloseAll() throws Exception {
        ManagedChannel ch = mock(ManagedChannel.class);
        when(ch.shutdown()).thenReturn(ch);
        when(ch.awaitTermination(5, TimeUnit.SECONDS)).thenReturn(true);

        Field channelsField = GrpcClientFactory.class.getDeclaredField("channels");
        channelsField.setAccessible(true);
        Map<String, ManagedChannel> channels = (Map<String, ManagedChannel>) channelsField.get(factory);
        channels.put("destroy-test:9090", ch);

        factory.destroy();

        verify(ch).shutdown();
        assertThat(channels).isEmpty();
    }

    @Test
    @DisplayName("빈 팩토리 destroy 안전")
    void destroy_EmptyFactory_ShouldNotFail() {
        factory.destroy();
        // no exception
    }

    @Test
    @DisplayName("TLS 비활성화 시 properties 기본값 확인")
    void tlsDisabled_ByDefault() {
        assertThat(properties.getTls().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("서버 기본 포트 확인")
    void serverDefaults() {
        assertThat(properties.getServer().getPort()).isEqualTo(9090);
        assertThat(properties.getServer().getMaxMessageSize()).isEqualTo(4 * 1024 * 1024);
    }

    @Test
    @DisplayName("클라이언트 기본 타임아웃 확인")
    void clientDefaults() {
        assertThat(properties.getClient().getDefaultTimeout()).isEqualTo(30000);
    }
}
