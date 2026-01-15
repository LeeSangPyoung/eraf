package com.eraf.starter.notification.push;

import com.eraf.starter.notification.ErafNotificationProperties;
import com.eraf.starter.notification.NotificationService.PushRequest;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Firebase Cloud Messaging (FCM) Push 발송 구현
 */
public class FcmPushSender implements PushSender {

    private final ErafNotificationProperties properties;
    private final Executor executor;
    private boolean initialized = false;

    public FcmPushSender(ErafNotificationProperties properties, Executor executor) {
        this.properties = properties;
        this.executor = executor;
    }

    private void ensureInitialized() {
        if (!initialized) {
            try {
                String credentialsPath = properties.getPush().getFcm().getCredentialsPath();
                FileInputStream serviceAccount = new FileInputStream(credentialsPath);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }
                initialized = true;
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize FCM", e);
            }
        }
    }

    @Override
    public void send(PushRequest request) {
        ensureInitialized();

        try {
            if (request.getTopic() != null) {
                // 토픽으로 발송
                Message message = buildMessage(request)
                        .setTopic(request.getTopic())
                        .build();
                FirebaseMessaging.getInstance().send(message);
            } else if (request.getTokens() != null && !request.getTokens().isEmpty()) {
                // 토큰으로 발송
                if (request.getTokens().size() == 1) {
                    Message message = buildMessage(request)
                            .setToken(request.getTokens().get(0))
                            .build();
                    FirebaseMessaging.getInstance().send(message);
                } else {
                    // 다중 토큰 발송
                    MulticastMessage message = buildMulticastMessage(request, request.getTokens());
                    FirebaseMessaging.getInstance().sendEachForMulticast(message);
                }
            }
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException("Failed to send FCM push notification", e);
        }
    }

    @Override
    public CompletableFuture<Void> sendAsync(PushRequest request) {
        return CompletableFuture.runAsync(() -> send(request), executor);
    }

    @Override
    public PushProvider getProvider() {
        return PushProvider.FCM;
    }

    private Message.Builder buildMessage(PushRequest request) {
        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody());

        if (request.getImageUrl() != null) {
            notificationBuilder.setImage(request.getImageUrl());
        }

        Message.Builder messageBuilder = Message.builder()
                .setNotification(notificationBuilder.build());

        if (request.getData() != null) {
            messageBuilder.putAllData(request.getData());
        }

        return messageBuilder;
    }

    private MulticastMessage buildMulticastMessage(PushRequest request, List<String> tokens) {
        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody());

        if (request.getImageUrl() != null) {
            notificationBuilder.setImage(request.getImageUrl());
        }

        MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(notificationBuilder.build());

        if (request.getData() != null) {
            messageBuilder.putAllData(request.getData());
        }

        return messageBuilder.build();
    }
}
