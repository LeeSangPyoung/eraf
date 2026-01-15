package com.eraf.starter.notification.sms;

import com.eraf.starter.notification.ErafNotificationProperties;
import com.eraf.starter.notification.NotificationService.SmsRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Naver Cloud SMS 발송 구현
 */
public class NaverSmsSender implements SmsSender {

    private static final String API_URL = "https://sens.apigw.ntruss.com/sms/v2/services/%s/messages";

    private final ErafNotificationProperties properties;
    private final Executor executor;
    private final HttpClient httpClient;

    public NaverSmsSender(ErafNotificationProperties properties, Executor executor) {
        this.properties = properties;
        this.executor = executor;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void send(SmsRequest request) {
        try {
            ErafNotificationProperties.Sms.Naver naverProps = properties.getSms().getNaver();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String signature = makeSignature(timestamp, naverProps);

            String url = String.format(API_URL, naverProps.getServiceId());

            String jsonBody = String.format("""
                {
                    "type": "SMS",
                    "from": "%s",
                    "content": "%s",
                    "messages": [
                        {"to": "%s"}
                    ]
                }
                """,
                    properties.getSms().getFrom(),
                    escapeJson(request.getMessage()),
                    request.getTo()
            );

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("x-ncp-apigw-timestamp", timestamp)
                    .header("x-ncp-iam-access-key", naverProps.getAccessKey())
                    .header("x-ncp-apigw-signature-v2", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 202) {
                throw new RuntimeException("Failed to send SMS via Naver: " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send SMS via Naver", e);
        }
    }

    @Override
    public CompletableFuture<Void> sendAsync(SmsRequest request) {
        return CompletableFuture.runAsync(() -> send(request), executor);
    }

    @Override
    public SmsProvider getProvider() {
        return SmsProvider.NAVER;
    }

    private String makeSignature(String timestamp, ErafNotificationProperties.Sms.Naver naverProps) throws Exception {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/" + naverProps.getServiceId() + "/messages";

        String message = method + space + url + newLine + timestamp + newLine + naverProps.getAccessKey();

        SecretKeySpec signingKey = new SecretKeySpec(naverProps.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(rawHmac);
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
