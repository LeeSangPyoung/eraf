package com.eraf.starter.tcp;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * TCP 클라이언트 인터페이스
 */
public interface TcpClient {

    /**
     * 서버에 연결
     */
    CompletableFuture<Void> connect();

    /**
     * 연결 해제
     */
    void disconnect();

    /**
     * 연결 상태 확인
     */
    boolean isConnected();

    /**
     * 메시지 전송
     */
    CompletableFuture<Void> send(byte[] data);

    /**
     * 메시지 전송 (문자열)
     */
    CompletableFuture<Void> send(String message);

    /**
     * 동기 전송 후 응답 수신
     */
    byte[] sendAndReceive(byte[] data, long timeoutMillis);

    /**
     * 메시지 수신 리스너 등록
     */
    void onMessage(Consumer<byte[]> listener);

    /**
     * 연결 상태 변경 리스너 등록
     */
    void onConnectionStateChange(Consumer<ConnectionState> listener);

    /**
     * 연결 상태
     */
    enum ConnectionState {
        CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED, RECONNECTING
    }
}
