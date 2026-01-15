package com.eraf.starter.notification.impl;

import com.eraf.starter.notification.NotificationService;
import com.eraf.starter.notification.email.EmailSender;
import com.eraf.starter.notification.push.PushSender;
import com.eraf.starter.notification.sms.SmsSender;

import java.util.concurrent.CompletableFuture;

/**
 * 기본 알림 서비스 구현
 */
public class DefaultNotificationService implements NotificationService {

    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final PushSender fcmPushSender;
    private final PushSender apnsPushSender;

    public DefaultNotificationService(EmailSender emailSender) {
        this(emailSender, null, null, null);
    }

    public DefaultNotificationService(EmailSender emailSender, SmsSender smsSender,
                                       PushSender fcmPushSender, PushSender apnsPushSender) {
        this.emailSender = emailSender;
        this.smsSender = smsSender;
        this.fcmPushSender = fcmPushSender;
        this.apnsPushSender = apnsPushSender;
    }

    @Override
    public void sendEmail(EmailRequest request) {
        if (emailSender != null) {
            emailSender.send(request);
        } else {
            throw new UnsupportedOperationException("Email is not configured");
        }
    }

    @Override
    public CompletableFuture<Void> sendEmailAsync(EmailRequest request) {
        if (emailSender != null) {
            return emailSender.sendAsync(request);
        }
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Email is not configured"));
    }

    @Override
    public void sendSms(SmsRequest request) {
        if (smsSender != null) {
            smsSender.send(request);
        } else {
            throw new UnsupportedOperationException("SMS is not configured");
        }
    }

    @Override
    public CompletableFuture<Void> sendSmsAsync(SmsRequest request) {
        if (smsSender != null) {
            return smsSender.sendAsync(request);
        }
        return CompletableFuture.failedFuture(new UnsupportedOperationException("SMS is not configured"));
    }

    @Override
    public void sendPush(PushRequest request) {
        PushRequest.PushPlatform platform = request.getPlatform();

        if (platform == PushRequest.PushPlatform.FCM || platform == PushRequest.PushPlatform.ALL) {
            if (fcmPushSender != null) {
                fcmPushSender.send(request);
            } else if (platform == PushRequest.PushPlatform.FCM) {
                throw new UnsupportedOperationException("FCM Push is not configured");
            }
        }

        if (platform == PushRequest.PushPlatform.APNS || platform == PushRequest.PushPlatform.ALL) {
            if (apnsPushSender != null) {
                apnsPushSender.send(request);
            } else if (platform == PushRequest.PushPlatform.APNS) {
                throw new UnsupportedOperationException("APNs Push is not configured");
            }
        }
    }

    @Override
    public CompletableFuture<Void> sendPushAsync(PushRequest request) {
        PushRequest.PushPlatform platform = request.getPlatform();

        CompletableFuture<Void> fcmFuture = CompletableFuture.completedFuture(null);
        CompletableFuture<Void> apnsFuture = CompletableFuture.completedFuture(null);

        if (platform == PushRequest.PushPlatform.FCM || platform == PushRequest.PushPlatform.ALL) {
            if (fcmPushSender != null) {
                fcmFuture = fcmPushSender.sendAsync(request);
            } else if (platform == PushRequest.PushPlatform.FCM) {
                return CompletableFuture.failedFuture(new UnsupportedOperationException("FCM Push is not configured"));
            }
        }

        if (platform == PushRequest.PushPlatform.APNS || platform == PushRequest.PushPlatform.ALL) {
            if (apnsPushSender != null) {
                apnsFuture = apnsPushSender.sendAsync(request);
            } else if (platform == PushRequest.PushPlatform.APNS) {
                return CompletableFuture.failedFuture(new UnsupportedOperationException("APNs Push is not configured"));
            }
        }

        return CompletableFuture.allOf(fcmFuture, apnsFuture);
    }
}
