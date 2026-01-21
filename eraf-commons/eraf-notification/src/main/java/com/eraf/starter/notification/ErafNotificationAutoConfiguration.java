package com.eraf.starter.notification;

import com.eraf.starter.notification.email.EmailSender;
import com.eraf.starter.notification.email.SmtpEmailSender;
import com.eraf.starter.notification.impl.DefaultNotificationService;
import com.eraf.starter.notification.push.ApnsPushSender;
import com.eraf.starter.notification.push.FcmPushSender;
import com.eraf.starter.notification.push.PushSender;
import com.eraf.starter.notification.sms.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.twilio.Twilio;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import software.amazon.awssdk.services.sns.SnsClient;

import java.util.concurrent.Executor;

/**
 * ERAF 알림 Auto Configuration
 */
@AutoConfiguration
@EnableConfigurationProperties(ErafNotificationProperties.class)
public class ErafNotificationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notification-");
        executor.initialize();
        return executor;
    }

    /**
     * SMTP 이메일 설정
     */
    @Configuration
    @ConditionalOnClass(JavaMailSender.class)
    @ConditionalOnProperty(name = "eraf.notification.email.enabled", havingValue = "true", matchIfMissing = true)
    public static class SmtpEmailConfiguration {

        @Bean
        @ConditionalOnMissingBean(EmailSender.class)
        public EmailSender smtpEmailSender(JavaMailSender mailSender,
                                            ErafNotificationProperties properties,
                                            Executor notificationExecutor) {
            return new SmtpEmailSender(mailSender, properties, notificationExecutor);
        }
    }

    /**
     * Twilio SMS 설정
     */
    @Configuration
    @ConditionalOnClass(Twilio.class)
    @ConditionalOnProperty(name = "eraf.notification.sms.provider", havingValue = "TWILIO")
    public static class TwilioSmsConfiguration {

        @Bean
        @ConditionalOnMissingBean(SmsSender.class)
        public SmsSender twilioSmsSender(ErafNotificationProperties properties,
                                          Executor notificationExecutor) {
            return new TwilioSmsSender(properties, notificationExecutor);
        }
    }

    /**
     * Naver SMS 설정
     */
    @Configuration
    @ConditionalOnProperty(name = "eraf.notification.sms.provider", havingValue = "NAVER")
    public static class NaverSmsConfiguration {

        @Bean
        @ConditionalOnMissingBean(SmsSender.class)
        public SmsSender naverSmsSender(ErafNotificationProperties properties,
                                         Executor notificationExecutor) {
            return new NaverSmsSender(properties, notificationExecutor);
        }
    }

    /**
     * NHN SMS 설정
     */
    @Configuration
    @ConditionalOnProperty(name = "eraf.notification.sms.provider", havingValue = "NHN")
    public static class NhnSmsConfiguration {

        @Bean
        @ConditionalOnMissingBean(SmsSender.class)
        public SmsSender nhnSmsSender(ErafNotificationProperties properties,
                                       Executor notificationExecutor) {
            return new NhnSmsSender(properties, notificationExecutor);
        }
    }

    /**
     * AWS SNS SMS 설정
     */
    @Configuration
    @ConditionalOnClass(SnsClient.class)
    @ConditionalOnProperty(name = "eraf.notification.sms.provider", havingValue = "AWS_SNS")
    public static class AwsSnsSmsConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public SnsClient snsClient() {
            return SnsClient.create();
        }

        @Bean
        @ConditionalOnMissingBean(SmsSender.class)
        public SmsSender awsSnsSmsSender(SnsClient snsClient,
                                          ErafNotificationProperties properties,
                                          Executor notificationExecutor) {
            return new AwsSnsSmsSender(snsClient, properties, notificationExecutor);
        }
    }

    /**
     * Custom SMS 설정
     */
    @Configuration
    @ConditionalOnProperty(name = "eraf.notification.sms.provider", havingValue = "CUSTOM")
    public static class CustomSmsConfiguration {

        @Bean
        @ConditionalOnMissingBean(SmsSender.class)
        public SmsSender customSmsSender(ErafNotificationProperties properties,
                                          Executor notificationExecutor) {
            return new CustomSmsSender(properties, notificationExecutor);
        }
    }

    /**
     * FCM Push 설정
     */
    @Configuration
    @ConditionalOnClass(FirebaseMessaging.class)
    @ConditionalOnProperty(name = "eraf.notification.push.fcm.enabled", havingValue = "true")
    public static class FcmPushConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "fcmPushSender")
        public PushSender fcmPushSender(ErafNotificationProperties properties,
                                         Executor notificationExecutor) {
            return new FcmPushSender(properties, notificationExecutor);
        }
    }

    /**
     * APNs Push 설정
     */
    @Configuration
    @ConditionalOnClass(name = "com.eatthepath.pushy.apns.ApnsClient")
    @ConditionalOnProperty(name = "eraf.notification.push.apns.enabled", havingValue = "true")
    public static class ApnsPushConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "apnsPushSender")
        public PushSender apnsPushSender(ErafNotificationProperties properties,
                                          Executor notificationExecutor) {
            return new ApnsPushSender(properties, notificationExecutor);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public NotificationService notificationService(
            EmailSender emailSender,
            @org.springframework.beans.factory.annotation.Autowired(required = false) SmsSender smsSender,
            @org.springframework.beans.factory.annotation.Autowired(required = false) PushSender fcmPushSender,
            @org.springframework.beans.factory.annotation.Autowired(required = false) PushSender apnsPushSender) {
        return new DefaultNotificationService(emailSender, smsSender, fcmPushSender, apnsPushSender);
    }
}
