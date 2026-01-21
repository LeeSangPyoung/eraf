package com.eraf.starter.notification.sms;

import com.eraf.starter.notification.ErafNotificationProperties;
import com.eraf.starter.notification.NotificationService.SmsRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Custom SMS API 발송 구현
 * 외부 SMS API와 연동할 때 사용
 */
public class CustomSmsSender implements SmsSender {

    private final ErafNotificationProperties properties;
    private final Executor executor;
    private final HttpClient httpClient;

    public CustomSmsSender(ErafNotificationProperties properties, Executor executor) {
        this.properties = properties;
        this.executor = executor;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void send(SmsRequest request) {
        try {
            ErafNotificationProperties.Sms smsProps = properties.getSms();
            ErafNotificationProperties.Sms.Custom customProps = smsProps.getCustom();

            String jsonBody = String.format("""
                {
                    "from": "%s",
                    "to": "%s",
                    "message": "%s"
                }
                """,
                    smsProps.getFrom(),
                    request.getTo(),
                    escapeJson(request.getMessage())
            );

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(customProps.getApiUrl()))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            // API Key 헤더 추가
            if (customProps.getApiKey() != null) {
                requestBuilder.header(customProps.getApiKeyHeader(), customProps.getApiKey());
            }

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Failed to send SMS via Custom API: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS via Custom API", e);
        }
    }

    @Override
    public CompletableFuture<Void> sendAsync(SmsRequest request) {
        return CompletableFuture.runAsync(() -> send(request), executor);
    }

    @Override
    public SmsProvider getProvider() {
        return SmsProvider.CUSTOM;
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
