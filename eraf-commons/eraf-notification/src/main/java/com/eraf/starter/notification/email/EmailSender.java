package com.eraf.starter.notification.email;

import com.eraf.starter.notification.NotificationService.EmailRequest;

import java.util.concurrent.CompletableFuture;

/**
 * 이메일 발송 인터페이스
 */
public interface EmailSender {

    /**
     * 이메일 발송
     */
    void send(EmailRequest request);

    /**
     * 이메일 비동기 발송
     */
    CompletableFuture<Void> sendAsync(EmailRequest request);
}
