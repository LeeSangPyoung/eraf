package com.eraf.notification.history;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

/**
 * 알림 이력 엔티티
 */
@Entity
@Table(name = "notification_history", indexes = {
        @Index(name = "idx_notification_type", columnList = "notification_type"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_sent_at", columnList = "sent_at"),
        @Index(name = "idx_recipient", columnList = "recipient")
})
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 알림 타입 (EMAIL, SMS, PUSH, SLACK, TEAMS)
     */
    @Column(name = "notification_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    /**
     * 수신자
     */
    @Column(name = "recipient", nullable = false, length = 500)
    private String recipient;

    /**
     * 제목
     */
    @Column(name = "subject", length = 500)
    private String subject;

    /**
     * 메시지 내용
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * 전송 상태 (SUCCESS, FAILED, PENDING)
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    /**
     * 에러 메시지
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 전송 시도 시간
     */
    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    /**
     * 전송 완료 시간
     */
    @Column(name = "completed_at")
    private Instant completedAt;

    /**
     * 응답 시간 (밀리초)
     */
    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    /**
     * 추가 메타데이터 (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * 재시도 횟수
     */
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /**
     * 발신자 (이메일인 경우)
     */
    @Column(name = "sender", length = 200)
    private String sender;

    /**
     * 우선순위
     */
    @Column(name = "priority", length = 20)
    @Enumerated(EnumType.STRING)
    private NotificationPriority priority;

    public enum NotificationType {
        EMAIL, SMS, PUSH_FCM, PUSH_APNS, SLACK, TEAMS, WEBHOOK
    }

    public enum NotificationStatus {
        PENDING,    // 대기 중
        SUCCESS,    // 성공
        FAILED,     // 실패
        RETRY       // 재시도 중
    }

    public enum NotificationPriority {
        LOW, NORMAL, HIGH, URGENT
    }

    // Constructors

    public NotificationHistory() {
        this.sentAt = Instant.now();
        this.status = NotificationStatus.PENDING;
        this.priority = NotificationPriority.NORMAL;
    }

    public NotificationHistory(NotificationType type, String recipient,
                                String subject, String message) {
        this();
        this.notificationType = type;
        this.recipient = recipient;
        this.subject = subject;
        this.message = message;
    }

    // Builder pattern

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final NotificationHistory history = new NotificationHistory();

        public Builder type(NotificationType type) {
            history.notificationType = type;
            return this;
        }

        public Builder recipient(String recipient) {
            history.recipient = recipient;
            return this;
        }

        public Builder subject(String subject) {
            history.subject = subject;
            return this;
        }

        public Builder message(String message) {
            history.message = message;
            return this;
        }

        public Builder status(NotificationStatus status) {
            history.status = status;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            history.errorMessage = errorMessage;
            return this;
        }

        public Builder sender(String sender) {
            history.sender = sender;
            return this;
        }

        public Builder priority(NotificationPriority priority) {
            history.priority = priority;
            return this;
        }

        public Builder metadata(String metadata) {
            history.metadata = metadata;
            return this;
        }

        public NotificationHistory build() {
            return history;
        }
    }

    // Business methods

    public void markSuccess(long responseTimeMs) {
        this.status = NotificationStatus.SUCCESS;
        this.completedAt = Instant.now();
        this.responseTimeMs = responseTimeMs;
    }

    public void markFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.completedAt = Instant.now();
        this.errorMessage = errorMessage;
    }

    public void incrementRetry() {
        this.retryCount++;
        this.status = NotificationStatus.RETRY;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }
}
