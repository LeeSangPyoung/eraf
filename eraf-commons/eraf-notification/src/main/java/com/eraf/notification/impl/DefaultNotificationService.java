package com.eraf.notification.impl;

import com.eraf.notification.NotificationService;
import com.eraf.notification.email.EmailSender;
import com.eraf.notification.push.PushSender;
import com.eraf.notification.sms.SmsSender;
import com.eraf.notification.template.NotificationTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * 기본 알림 서비스 구현
 *
 * <p>templateId가 제공되면 템플릿을 렌더링하여 제목/본문을 자동으로 설정합니다.</p>
 */
public class DefaultNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultNotificationService.class);

    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final PushSender fcmPushSender;
    private final PushSender apnsPushSender;
    private final NotificationTemplateService templateService;

    public DefaultNotificationService(EmailSender emailSender) {
        this(emailSender, null, null, null, null);
    }

    public DefaultNotificationService(EmailSender emailSender, SmsSender smsSender,
                                       PushSender fcmPushSender, PushSender apnsPushSender) {
        this(emailSender, smsSender, fcmPushSender, apnsPushSender, null);
    }

    public DefaultNotificationService(EmailSender emailSender, SmsSender smsSender,
                                       PushSender fcmPushSender, PushSender apnsPushSender,
                                       NotificationTemplateService templateService) {
        this.emailSender = emailSender;
        this.smsSender = smsSender;
        this.fcmPushSender = fcmPushSender;
        this.apnsPushSender = apnsPushSender;
        this.templateService = templateService;
    }

    @Override
    public void sendEmail(EmailRequest request) {
        if (emailSender != null) {
            resolveEmailTemplate(request);
            emailSender.send(request);
        } else {
            throw new UnsupportedOperationException("Email is not configured");
        }
    }

    @Override
    public CompletableFuture<Void> sendEmailAsync(EmailRequest request) {
        if (emailSender != null) {
            resolveEmailTemplate(request);
            return emailSender.sendAsync(request);
        }
        return CompletableFuture.failedFuture(new UnsupportedOperationException("Email is not configured"));
    }

    @Override
    public void sendSms(SmsRequest request) {
        if (smsSender != null) {
            resolveSmsTemplate(request);
            smsSender.send(request);
        } else {
            throw new UnsupportedOperationException("SMS is not configured");
        }
    }

    @Override
    public CompletableFuture<Void> sendSmsAsync(SmsRequest request) {
        if (smsSender != null) {
            resolveSmsTemplate(request);
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

    // ===== Template Resolution =====

    /**
     * 이메일 요청에 templateId가 있으면 렌더링하여 subject/body를 설정
     */
    private void resolveEmailTemplate(EmailRequest request) {
        if (request.getTemplateId() == null || request.getTemplateId().isEmpty()) {
            return;
        }
        if (templateService == null) {
            log.warn("[NotificationService] Template requested but NotificationTemplateService is not available. templateId={}", request.getTemplateId());
            return;
        }

        NotificationTemplateService.RenderedTemplate rendered =
                templateService.render(request.getTemplateId(), request.getTemplateVariables());

        if (rendered.getSubject() != null && request.getSubject() == null) {
            request.subject(rendered.getSubject());
        }
        if (rendered.getBody() != null) {
            request.body(rendered.getBody());
        }
        if (rendered.isHtml()) {
            request.html(true);
        }
    }

    /**
     * SMS 요청에 templateId가 있으면 렌더링하여 message를 설정
     */
    private void resolveSmsTemplate(SmsRequest request) {
        if (request.getTemplateId() == null || request.getTemplateId().isEmpty()) {
            return;
        }
        if (templateService == null) {
            log.warn("[NotificationService] Template requested but NotificationTemplateService is not available. templateId={}", request.getTemplateId());
            return;
        }

        NotificationTemplateService.RenderedTemplate rendered =
                templateService.render(request.getTemplateId(), request.getTemplateVariables());

        if (rendered.getBody() != null) {
            request.message(rendered.getBody());
        }
    }
}
