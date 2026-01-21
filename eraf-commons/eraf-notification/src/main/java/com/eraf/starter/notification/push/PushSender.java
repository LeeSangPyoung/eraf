package com.eraf.starter.notification.push;

import com.eraf.starter.notification.NotificationService.PushRequest;

import java.util.concurrent.CompletableFuture;

/**
 * Push 알림 발송 인터페이스
 */
public interface PushSender {

    /**
     * Push 알림 발송
     */
    void send(PushRequest request);

    /**
     * Push 알림 비동기 발송
     */
    CompletableFuture<Void> sendAsync(PushRequest request);

    /**
     * 프로바이더 타입
     */
    PushProvider getProvider();

    enum PushProvider {
        FCM, APNS
    }
}
