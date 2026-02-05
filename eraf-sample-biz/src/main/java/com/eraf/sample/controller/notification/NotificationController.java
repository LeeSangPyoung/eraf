package com.eraf.sample.controller.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 알림/메시징 컨트롤러
 * #50 이메일 발송, #51 SMS 발송, #52 푸시 알림, #53 알림 이력 조회
 * #54 Kafka 메시지 발행, #55 Kafka 메시지 조회
 */
@Controller
@RequestMapping("/notification")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // In-memory stores for demo (Mock implementation)
    private final List<NotificationRecord> notificationHistory = new CopyOnWriteArrayList<>();
    private final List<KafkaMessageRecord> kafkaMessages = new CopyOnWriteArrayList<>();
    private final Map<String, List<KafkaMessageRecord>> topicSubscribers = new ConcurrentHashMap<>();

    @GetMapping
    public String notificationPage() {
        return "notification/notification";
    }

    // ============ #50 이메일 발송 ============

    @PostMapping("/email/send")
    @ResponseBody
    public Map<String, Object> sendEmail(@RequestBody EmailRequest request) {
        log.info("[Mock Email] Sending email to: {}", request.getTo());
        log.info("[Mock Email] Subject: {}", request.getSubject());
        log.info("[Mock Email] Body: {}", request.getBody());
        log.info("[Mock Email] HTML: {}", request.isHtml());

        String notificationId = UUID.randomUUID().toString().substring(0, 8);

        // Store in history
        NotificationRecord record = new NotificationRecord();
        record.setId(notificationId);
        record.setType("EMAIL");
        record.setRecipient(String.join(", ", request.getTo()));
        record.setTitle(request.getSubject());
        record.setContent(request.getBody());
        record.setStatus("SUCCESS");
        record.setSentAt(LocalDateTime.now());
        notificationHistory.add(0, record);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("notificationId", notificationId);
        result.put("type", "EMAIL");
        result.put("to", request.getTo());
        result.put("subject", request.getSubject());
        result.put("sentAt", LocalDateTime.now().format(FORMATTER));
        result.put("message", "Email sent successfully (Mock - check console log)");

        return result;
    }

    // ============ #51 SMS 발송 ============

    @PostMapping("/sms/send")
    @ResponseBody
    public Map<String, Object> sendSms(@RequestBody SmsRequest request) {
        log.info("[Mock SMS] Sending SMS to: {}", request.getTo());
        log.info("[Mock SMS] Message: {}", request.getMessage());

        String notificationId = UUID.randomUUID().toString().substring(0, 8);

        // Store in history
        NotificationRecord record = new NotificationRecord();
        record.setId(notificationId);
        record.setType("SMS");
        record.setRecipient(request.getTo());
        record.setTitle("SMS");
        record.setContent(request.getMessage());
        record.setStatus("SUCCESS");
        record.setSentAt(LocalDateTime.now());
        notificationHistory.add(0, record);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("notificationId", notificationId);
        result.put("type", "SMS");
        result.put("to", request.getTo());
        result.put("message", request.getMessage());
        result.put("sentAt", LocalDateTime.now().format(FORMATTER));
        result.put("provider", "MOCK");
        result.put("note", "SMS sent successfully (Mock - check console log)");

        return result;
    }

    // ============ #52 푸시 알림 발송 ============

    @PostMapping("/push/send")
    @ResponseBody
    public Map<String, Object> sendPush(@RequestBody PushRequest request) {
        log.info("[Mock Push] Sending push notification");
        log.info("[Mock Push] Title: {}", request.getTitle());
        log.info("[Mock Push] Body: {}", request.getBody());
        log.info("[Mock Push] Tokens: {}", request.getTokens());
        log.info("[Mock Push] Platform: {}", request.getPlatform());

        String notificationId = UUID.randomUUID().toString().substring(0, 8);

        // Store in history
        NotificationRecord record = new NotificationRecord();
        record.setId(notificationId);
        record.setType("PUSH_" + request.getPlatform());
        record.setRecipient(String.join(", ", request.getTokens()));
        record.setTitle(request.getTitle());
        record.setContent(request.getBody());
        record.setStatus("SUCCESS");
        record.setSentAt(LocalDateTime.now());
        notificationHistory.add(0, record);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("notificationId", notificationId);
        result.put("type", "PUSH");
        result.put("platform", request.getPlatform());
        result.put("title", request.getTitle());
        result.put("body", request.getBody());
        result.put("tokens", request.getTokens());
        result.put("sentAt", LocalDateTime.now().format(FORMATTER));
        result.put("note", "Push notification sent successfully (Mock - check console log)");

        return result;
    }

    // ============ #53 알림 이력 조회 ============

    @GetMapping("/history")
    @ResponseBody
    public Map<String, Object> getNotificationHistory(
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(defaultValue = "20") int limit) {

        List<NotificationRecord> filtered = notificationHistory.stream()
                .filter(r -> "ALL".equals(type) || r.getType().startsWith(type))
                .limit(limit)
                .toList();

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("total", (long) notificationHistory.size());
        stats.put("email", notificationHistory.stream().filter(r -> "EMAIL".equals(r.getType())).count());
        stats.put("sms", notificationHistory.stream().filter(r -> "SMS".equals(r.getType())).count());
        stats.put("push", notificationHistory.stream().filter(r -> r.getType().startsWith("PUSH")).count());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filter", type);
        result.put("stats", stats);
        result.put("records", filtered);

        return result;
    }

    @DeleteMapping("/history/clear")
    @ResponseBody
    public Map<String, Object> clearHistory() {
        int count = notificationHistory.size();
        notificationHistory.clear();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("clearedCount", count);
        result.put("message", "Notification history cleared");

        return result;
    }

    // ============ #54 Kafka 메시지 발행 ============

    @PostMapping("/kafka/publish")
    @ResponseBody
    public Map<String, Object> publishKafkaMessage(@RequestBody KafkaPublishRequest request) {
        log.info("[Mock Kafka] Publishing message to topic: {}", request.getTopic());
        log.info("[Mock Kafka] Event Type: {}", request.getEventType());
        log.info("[Mock Kafka] Key: {}", request.getKey());
        log.info("[Mock Kafka] Payload: {}", request.getPayload());

        String eventId = UUID.randomUUID().toString();
        LocalDateTime timestamp = LocalDateTime.now();

        // Store in kafka messages
        KafkaMessageRecord record = new KafkaMessageRecord();
        record.setEventId(eventId);
        record.setTopic(request.getTopic());
        record.setKey(request.getKey());
        record.setEventType(request.getEventType());
        record.setPayload(request.getPayload());
        record.setTimestamp(timestamp);
        record.setSource("eraf-sample-biz");
        record.setDirection("OUTBOUND");
        kafkaMessages.add(0, record);

        // Simulate message delivery to subscribers
        if (topicSubscribers.containsKey(request.getTopic())) {
            KafkaMessageRecord inbound = new KafkaMessageRecord();
            inbound.setEventId(eventId);
            inbound.setTopic(request.getTopic());
            inbound.setKey(request.getKey());
            inbound.setEventType(request.getEventType());
            inbound.setPayload(request.getPayload());
            inbound.setTimestamp(timestamp);
            inbound.setSource("eraf-sample-biz");
            inbound.setDirection("INBOUND");
            topicSubscribers.get(request.getTopic()).add(0, inbound);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("eventId", eventId);
        result.put("topic", request.getTopic());
        result.put("key", request.getKey());
        result.put("eventType", request.getEventType());
        result.put("timestamp", timestamp.format(FORMATTER));
        result.put("note", "Message published (Mock - in-memory storage)");

        return result;
    }

    // ============ #55 Kafka 메시지 조회 ============

    @GetMapping("/kafka/messages")
    @ResponseBody
    public Map<String, Object> getKafkaMessages(
            @RequestParam(required = false) String topic,
            @RequestParam(defaultValue = "ALL") String direction,
            @RequestParam(defaultValue = "50") int limit) {

        List<KafkaMessageRecord> filtered = kafkaMessages.stream()
                .filter(m -> topic == null || topic.isEmpty() || topic.equals(m.getTopic()))
                .filter(m -> "ALL".equals(direction) || direction.equals(m.getDirection()))
                .limit(limit)
                .toList();

        // Get unique topics
        Set<String> topics = new LinkedHashSet<>();
        kafkaMessages.forEach(m -> topics.add(m.getTopic()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalMessages", kafkaMessages.size());
        result.put("topics", topics);
        result.put("filter", Map.of("topic", topic != null ? topic : "ALL", "direction", direction));
        result.put("messages", filtered);

        return result;
    }

    @PostMapping("/kafka/subscribe")
    @ResponseBody
    public Map<String, Object> subscribeTopic(@RequestBody Map<String, String> request) {
        String topic = request.get("topic");

        log.info("[Mock Kafka] Subscribing to topic: {}", topic);

        topicSubscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("topic", topic);
        result.put("subscribed", true);
        result.put("activeSubscriptions", topicSubscribers.keySet());

        return result;
    }

    @GetMapping("/kafka/subscriptions")
    @ResponseBody
    public Map<String, Object> getSubscriptions() {
        Map<String, Integer> subscriptionStats = new LinkedHashMap<>();
        topicSubscribers.forEach((topic, messages) -> subscriptionStats.put(topic, messages.size()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("subscriptions", subscriptionStats);
        result.put("totalTopics", topicSubscribers.size());

        return result;
    }

    @GetMapping("/kafka/received")
    @ResponseBody
    public Map<String, Object> getReceivedMessages(
            @RequestParam String topic,
            @RequestParam(defaultValue = "20") int limit) {

        List<KafkaMessageRecord> messages = topicSubscribers.getOrDefault(topic, List.of())
                .stream()
                .limit(limit)
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("topic", topic);
        result.put("messageCount", messages.size());
        result.put("messages", messages);

        return result;
    }

    @DeleteMapping("/kafka/clear")
    @ResponseBody
    public Map<String, Object> clearKafkaMessages() {
        int count = kafkaMessages.size();
        kafkaMessages.clear();
        topicSubscribers.clear();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("clearedMessages", count);
        result.put("message", "Kafka messages and subscriptions cleared");

        return result;
    }

    // ============ Request/Response DTOs ============

    public static class EmailRequest {
        private List<String> to;
        private List<String> cc;
        private List<String> bcc;
        private String subject;
        private String body;
        private boolean html;

        public List<String> getTo() { return to; }
        public void setTo(List<String> to) { this.to = to; }
        public List<String> getCc() { return cc; }
        public void setCc(List<String> cc) { this.cc = cc; }
        public List<String> getBcc() { return bcc; }
        public void setBcc(List<String> bcc) { this.bcc = bcc; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        public boolean isHtml() { return html; }
        public void setHtml(boolean html) { this.html = html; }
    }

    public static class SmsRequest {
        private String to;
        private String message;

        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class PushRequest {
        private List<String> tokens;
        private String title;
        private String body;
        private String platform = "FCM";
        private Map<String, String> data;

        public List<String> getTokens() { return tokens; }
        public void setTokens(List<String> tokens) { this.tokens = tokens; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }
        public Map<String, String> getData() { return data; }
        public void setData(Map<String, String> data) { this.data = data; }
    }

    public static class KafkaPublishRequest {
        private String topic;
        private String key;
        private String eventType;
        private Object payload;

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public Object getPayload() { return payload; }
        public void setPayload(Object payload) { this.payload = payload; }
    }

    public static class NotificationRecord {
        private String id;
        private String type;
        private String recipient;
        private String title;
        private String content;
        private String status;
        private LocalDateTime sentAt;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public LocalDateTime getSentAt() { return sentAt; }
        public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
        public String getSentAtFormatted() { return sentAt != null ? sentAt.format(FORMATTER) : null; }
    }

    public static class KafkaMessageRecord {
        private String eventId;
        private String topic;
        private String key;
        private String eventType;
        private Object payload;
        private LocalDateTime timestamp;
        private String source;
        private String direction;

        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        public Object getPayload() { return payload; }
        public void setPayload(Object payload) { this.payload = payload; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }
        public String getTimestampFormatted() { return timestamp != null ? timestamp.format(FORMATTER) : null; }
    }
}
