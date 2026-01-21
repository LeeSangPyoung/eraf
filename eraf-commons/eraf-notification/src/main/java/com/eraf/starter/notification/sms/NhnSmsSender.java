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
 * NHN Cloud SMS 발송 구현
 */
public class NhnSmsSender implements SmsSender {

    private static final String API_URL = "https://api-sms.cloud.toast.com/sms/v3.0/appKeys/%s/sender/sms";

    private final ErafNotificationProperties properties;
    private final Executor executor;
    private final HttpClient httpClient;

    public NhnSmsSender(ErafNotificationProperties properties, Executor executor) {
        this.properties = properties;
        this.executor = executor;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void send(SmsRequest request) {
        try {
            ErafNotificationProperties.Sms.Nhn nhnProps = properties.getSms().getNhn();
            String url = String.format(API_URL, nhnProps.getAppKey());

            String jsonBody = String.format("""
                {
                    "body": "%s",
                    "sendNo": "%s",
                    "recipientList": [
                        {"recipientNo": "%s"}
                    ]
                }
                """,
                    escapeJson(request.getMessage()),
                    properties.getSms().getFrom(),
                    request.getTo()
            );

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("X-Secret-Key", nhnProps.getSecretKey())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to send SMS via NHN: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS via NHN", e);
        }
    }

    @Override
    public CompletableFuture<Void> sendAsync(SmsRequest request) {
        return CompletableFuture.runAsync(() -> send(request), executor);
    }

    @Override
    public SmsProvider getProvider() {
        return SmsProvider.NHN;
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
