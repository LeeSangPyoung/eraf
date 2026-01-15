package com.eraf.starter.notification.sms;

import com.eraf.starter.notification.ErafNotificationProperties;
import com.eraf.starter.notification.NotificationService.SmsRequest;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Twilio SMS 발송 구현
 */
public class TwilioSmsSender implements SmsSender {

    private final ErafNotificationProperties properties;
    private final Executor executor;
    private boolean initialized = false;

    public TwilioSmsSender(ErafNotificationProperties properties, Executor executor) {
        this.properties = properties;
        this.executor = executor;
    }

    private void ensureInitialized() {
        if (!initialized) {
            ErafNotificationProperties.Sms.Twilio twilioProps = properties.getSms().getTwilio();
            Twilio.init(twilioProps.getAccountSid(), twilioProps.getAuthToken());
            initialized = true;
        }
    }

    @Override
    public void send(SmsRequest request) {
        ensureInitialized();

        String from = properties.getSms().getFrom();
        String to = request.getTo();
        String message = request.getMessage();

        Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(from),
                message
        ).create();
    }

    @Override
    public CompletableFuture<Void> sendAsync(SmsRequest request) {
        return CompletableFuture.runAsync(() -> send(request), executor);
    }

    @Override
    public SmsProvider getProvider() {
        return SmsProvider.TWILIO;
    }
}
