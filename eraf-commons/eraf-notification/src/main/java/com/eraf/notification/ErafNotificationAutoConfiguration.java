package com.eraf.notification;

import com.eraf.notification.email.EmailSender;
import com.eraf.notification.email.SmtpEmailSender;
import com.eraf.notification.history.NotificationHistoryRepository;
import com.eraf.notification.history.NotificationRetentionPolicy;
import com.eraf.notification.impl.DefaultNotificationService;
import com.eraf.notification.push.ApnsPushSender;
import com.eraf.notification.push.FcmPushSender;
import com.eraf.notification.push.PushSender;
import com.eraf.notification.sms.*;
import com.eraf.notification.template.NotificationTemplateEngine;
import com.eraf.notification.template.NotificationTemplateRepository;
import com.eraf.notification.template.NotificationTemplateService;
import com.eraf.notification.webhook.SlackWebhookSender;
import com.eraf.notification.webhook.TeamsWebhookSender;
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
    public Executor notificationExecutor(ErafNotificationProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getExecutor().getCorePoolSize());
        executor.setMaxPoolSize(properties.getExecutor().getMaxPoolSize());
        executor.setQueueCapacity(properties.getExecutor().getQueueCapacity());
        executor.setThreadNamePrefix("notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * SMTP 이메일 설정
     */
    @Configuration
    @ConditionalOnClass(JavaMailSender.class)
    @ConditionalOnBean(JavaMailSender.class)
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

    /**
     * Slack Webhook 설정
     */
    @Configuration
    @ConditionalOnProperty(name = "eraf.notification.webhook.slack.enabled", havingValue = "true")
    public static class SlackWebhookConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public SlackWebhookSender slackWebhookSender(ErafNotificationProperties properties) {
            String webhookUrl = properties.getWebhook().getSlack().getWebhookUrl();
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                throw new IllegalArgumentException("Slack webhook URL is required");
            }
            return new SlackWebhookSender(webhookUrl);
        }
    }

    /**
     * Microsoft Teams Webhook 설정
     */
    @Configuration
    @ConditionalOnProperty(name = "eraf.notification.webhook.teams.enabled", havingValue = "true")
    public static class TeamsWebhookConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public TeamsWebhookSender teamsWebhookSender(ErafNotificationProperties properties) {
            String webhookUrl = properties.getWebhook().getTeams().getWebhookUrl();
            if (webhookUrl == null || webhookUrl.isEmpty()) {
                throw new IllegalArgumentException("Teams webhook URL is required");
            }
            return new TeamsWebhookSender(webhookUrl);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public NotificationService notificationService(
            @org.springframework.beans.factory.annotation.Autowired(required = false) EmailSender emailSender,
            @org.springframework.beans.factory.annotation.Autowired(required = false) SmsSender smsSender,
            @org.springframework.beans.factory.annotation.Autowired(required = false) PushSender fcmPushSender,
            @org.springframework.beans.factory.annotation.Autowired(required = false) PushSender apnsPushSender,
            @org.springframework.beans.factory.annotation.Autowired(required = false) NotificationTemplateService templateService) {
        return new DefaultNotificationService(emailSender, smsSender, fcmPushSender, apnsPushSender, templateService);
    }

    /**
     * 알림 템플릿 엔진
     */
    @Bean
    @ConditionalOnMissingBean
    public NotificationTemplateEngine notificationTemplateEngine() {
        return new NotificationTemplateEngine();
    }

    /**
     * 알림 템플릿 서비스
     */
    @Configuration
    @ConditionalOnBean(NotificationTemplateRepository.class)
    public static class TemplateConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public NotificationTemplateService notificationTemplateService(
                NotificationTemplateRepository templateRepository,
                NotificationTemplateEngine templateEngine) {
            return new NotificationTemplateService(templateRepository, templateEngine);
        }
    }

    /**
     * 알림 이력 보존 정책
     */
    @Configuration
    @ConditionalOnBean(NotificationHistoryRepository.class)
    @ConditionalOnProperty(name = "eraf.notification.retention.enabled", havingValue = "true", matchIfMissing = true)
    public static class RetentionConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public NotificationRetentionPolicy notificationRetentionPolicy(
                NotificationHistoryRepository repository,
                ErafNotificationProperties properties) {
            return new NotificationRetentionPolicy(
                    repository,
                    properties.getRetention().getRetentionDays());
        }
    }
}
