package com.eraf.starter.notification.sms;

import com.eraf.starter.notification.NotificationService.SmsRequest;

import java.util.concurrent.CompletableFuture;

/**
 * SMS 발송 인터페이스
 */
public interface SmsSender {

    /**
     * SMS 발송
     */
    void send(SmsRequest request);

    /**
     * SMS 비동기 발송
     */
    CompletableFuture<Void> sendAsync(SmsRequest request);

    /**
     * 프로바이더 타입
     */
    SmsProvider getProvider();

    enum SmsProvider {
        TWILIO, NAVER, NHN, AWS_SNS, CUSTOM
    }
}
