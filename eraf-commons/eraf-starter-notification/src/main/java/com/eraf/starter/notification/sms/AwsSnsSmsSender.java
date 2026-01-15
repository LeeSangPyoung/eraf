package com.eraf.starter.notification.sms;

import com.eraf.starter.notification.ErafNotificationProperties;
import com.eraf.starter.notification.NotificationService.SmsRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * AWS SNS SMS 발송 구현
 */
public class AwsSnsSmsSender implements SmsSender {

    private final SnsClient snsClient;
    private final ErafNotificationProperties properties;
    private final Executor executor;

    public AwsSnsSmsSender(SnsClient snsClient, ErafNotificationProperties properties, Executor executor) {
        this.snsClient = snsClient;
        this.properties = properties;
        this.executor = executor;
    }

    @Override
    public void send(SmsRequest request) {
        PublishRequest publishRequest = PublishRequest.builder()
                .phoneNumber(request.getTo())
                .message(request.getMessage())
                .build();

        snsClient.publish(publishRequest);
    }

    @Override
    public CompletableFuture<Void> sendAsync(SmsRequest request) {
        return CompletableFuture.runAsync(() -> send(request), executor);
    }

    @Override
    public SmsProvider getProvider() {
        return SmsProvider.AWS_SNS;
    }
}
