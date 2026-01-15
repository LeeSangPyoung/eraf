package com.eraf.starter.notification.push;

import com.eraf.starter.notification.ErafNotificationProperties;
import com.eraf.starter.notification.NotificationService.PushRequest;
import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Apple Push Notification Service (APNs) Push 발송 구현
 */
public class ApnsPushSender implements PushSender {

    private final ErafNotificationProperties properties;
    private final Executor executor;
    private ApnsClient apnsClient;
    private boolean initialized = false;

    public ApnsPushSender(ErafNotificationProperties properties, Executor executor) {
        this.properties = properties;
        this.executor = executor;
    }

    private void ensureInitialized() {
        if (!initialized) {
            try {
                ErafNotificationProperties.Push.Apns apnsProps = properties.getPush().getApns();

                ApnsClientBuilder builder = new ApnsClientBuilder();

                if (apnsProps.getCertificatePath() != null) {
                    builder.setApnsServer(apnsProps.isProduction()
                            ? ApnsClientBuilder.PRODUCTION_APNS_HOST
                            : ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                            .setClientCredentials(
                                    new File(apnsProps.getCertificatePath()),
                                    apnsProps.getCertificatePassword()
                            );
                }

                apnsClient = builder.build();
                initialized = true;
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize APNs client", e);
            }
        }
    }

    @Override
    public void send(PushRequest request) {
        ensureInitialized();

        try {
            ErafNotificationProperties.Push.Apns apnsProps = properties.getPush().getApns();
            String bundleId = apnsProps.getBundleId();

            ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder()
                    .setAlertTitle(request.getTitle())
                    .setAlertBody(request.getBody());

            if (request.getData() != null) {
                request.getData().forEach(payloadBuilder::addCustomProperty);
            }

            String payload = payloadBuilder.build();

            for (String token : request.getTokens()) {
                String sanitizedToken = TokenUtil.sanitizeTokenString(token);

                SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(
                        sanitizedToken,
                        bundleId,
                        payload
                );

                PushNotificationResponse<SimpleApnsPushNotification> response =
                        apnsClient.sendNotification(pushNotification).get();

                if (!response.isAccepted()) {
                    throw new RuntimeException("APNs rejected notification: " +
                            response.getRejectionReason().orElse("Unknown reason"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send APNs push notification", e);
        }
    }

    @Override
    public CompletableFuture<Void> sendAsync(PushRequest request) {
        return CompletableFuture.runAsync(() -> send(request), executor);
    }

    @Override
    public PushProvider getProvider() {
        return PushProvider.APNS;
    }
}
