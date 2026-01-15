package com.eraf.starter.notification;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 통합 알림 서비스 인터페이스
 */
public interface NotificationService {

    /**
     * 이메일 발송
     */
    void sendEmail(EmailRequest request);

    /**
     * 이메일 비동기 발송
     */
    CompletableFuture<Void> sendEmailAsync(EmailRequest request);

    /**
     * SMS 발송
     */
    void sendSms(SmsRequest request);

    /**
     * SMS 비동기 발송
     */
    CompletableFuture<Void> sendSmsAsync(SmsRequest request);

    /**
     * 푸시 알림 발송
     */
    void sendPush(PushRequest request);

    /**
     * 푸시 알림 비동기 발송
     */
    CompletableFuture<Void> sendPushAsync(PushRequest request);

    /**
     * 이메일 요청
     */
    class EmailRequest {
        private List<String> to;
        private List<String> cc;
        private List<String> bcc;
        private String subject;
        private String body;
        private boolean html;
        private List<Attachment> attachments;
        private String templateId;
        private Map<String, Object> templateVariables;

        public static EmailRequest builder() {
            return new EmailRequest();
        }

        public EmailRequest to(String... emails) {
            this.to = List.of(emails);
            return this;
        }

        public EmailRequest to(List<String> emails) {
            this.to = emails;
            return this;
        }

        public EmailRequest cc(String... emails) {
            this.cc = List.of(emails);
            return this;
        }

        public EmailRequest bcc(String... emails) {
            this.bcc = List.of(emails);
            return this;
        }

        public EmailRequest subject(String subject) {
            this.subject = subject;
            return this;
        }

        public EmailRequest body(String body) {
            this.body = body;
            return this;
        }

        public EmailRequest html(boolean html) {
            this.html = html;
            return this;
        }

        public EmailRequest attachments(List<Attachment> attachments) {
            this.attachments = attachments;
            return this;
        }

        public EmailRequest template(String templateId, Map<String, Object> variables) {
            this.templateId = templateId;
            this.templateVariables = variables;
            return this;
        }

        // Getters
        public List<String> getTo() { return to; }
        public List<String> getCc() { return cc; }
        public List<String> getBcc() { return bcc; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
        public boolean isHtml() { return html; }
        public List<Attachment> getAttachments() { return attachments; }
        public String getTemplateId() { return templateId; }
        public Map<String, Object> getTemplateVariables() { return templateVariables; }
    }

    /**
     * SMS 요청
     */
    class SmsRequest {
        private String to;
        private String message;
        private String templateId;
        private Map<String, Object> templateVariables;

        public static SmsRequest builder() {
            return new SmsRequest();
        }

        public SmsRequest to(String phoneNumber) {
            this.to = phoneNumber;
            return this;
        }

        public SmsRequest message(String message) {
            this.message = message;
            return this;
        }

        public SmsRequest template(String templateId, Map<String, Object> variables) {
            this.templateId = templateId;
            this.templateVariables = variables;
            return this;
        }

        // Getters
        public String getTo() { return to; }
        public String getMessage() { return message; }
        public String getTemplateId() { return templateId; }
        public Map<String, Object> getTemplateVariables() { return templateVariables; }
    }

    /**
     * 푸시 요청
     */
    class PushRequest {
        private List<String> tokens;
        private String topic;
        private String title;
        private String body;
        private String imageUrl;
        private Map<String, String> data;
        private PushPlatform platform;

        public enum PushPlatform {
            FCM, APNS, ALL
        }

        public static PushRequest builder() {
            return new PushRequest();
        }

        public PushRequest tokens(List<String> tokens) {
            this.tokens = tokens;
            return this;
        }

        public PushRequest token(String token) {
            this.tokens = List.of(token);
            return this;
        }

        public PushRequest topic(String topic) {
            this.topic = topic;
            return this;
        }

        public PushRequest title(String title) {
            this.title = title;
            return this;
        }

        public PushRequest body(String body) {
            this.body = body;
            return this;
        }

        public PushRequest imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public PushRequest data(Map<String, String> data) {
            this.data = data;
            return this;
        }

        public PushRequest platform(PushPlatform platform) {
            this.platform = platform;
            return this;
        }

        // Getters
        public List<String> getTokens() { return tokens; }
        public String getTopic() { return topic; }
        public String getTitle() { return title; }
        public String getBody() { return body; }
        public String getImageUrl() { return imageUrl; }
        public Map<String, String> getData() { return data; }
        public PushPlatform getPlatform() { return platform != null ? platform : PushPlatform.ALL; }
    }

    /**
     * 첨부 파일
     */
    class Attachment {
        private String filename;
        private byte[] content;
        private String contentType;

        public Attachment(String filename, byte[] content, String contentType) {
            this.filename = filename;
            this.content = content;
            this.contentType = contentType;
        }

        public String getFilename() { return filename; }
        public byte[] getContent() { return content; }
        public String getContentType() { return contentType; }
    }
}
