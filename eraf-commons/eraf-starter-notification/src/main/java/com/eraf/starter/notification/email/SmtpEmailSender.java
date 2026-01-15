package com.eraf.starter.notification.email;

import com.eraf.starter.notification.ErafNotificationProperties;
import com.eraf.starter.notification.NotificationService.Attachment;
import com.eraf.starter.notification.NotificationService.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * SMTP 이메일 발송 구현
 */
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;
    private final ErafNotificationProperties properties;
    private final Executor executor;

    public SmtpEmailSender(JavaMailSender mailSender, ErafNotificationProperties properties, Executor executor) {
        this.mailSender = mailSender;
        this.properties = properties;
        this.executor = executor;
    }

    @Override
    public void send(EmailRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 발신자
            if (properties.getEmail().getFromName() != null) {
                helper.setFrom(properties.getEmail().getFrom(), properties.getEmail().getFromName());
            } else {
                helper.setFrom(properties.getEmail().getFrom());
            }

            // 수신자
            helper.setTo(request.getTo().toArray(new String[0]));

            // 참조
            if (request.getCc() != null && !request.getCc().isEmpty()) {
                helper.setCc(request.getCc().toArray(new String[0]));
            }

            // 숨은 참조
            if (request.getBcc() != null && !request.getBcc().isEmpty()) {
                helper.setBcc(request.getBcc().toArray(new String[0]));
            }

            // 제목
            helper.setSubject(request.getSubject());

            // 본문
            helper.setText(request.getBody(), request.isHtml());

            // 첨부 파일
            if (request.getAttachments() != null) {
                for (Attachment attachment : request.getAttachments()) {
                    helper.addAttachment(
                            attachment.getFilename(),
                            new ByteArrayResource(attachment.getContent()),
                            attachment.getContentType()
                    );
                }
            }

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public CompletableFuture<Void> sendAsync(EmailRequest request) {
        return CompletableFuture.runAsync(() -> send(request), executor);
    }
}
