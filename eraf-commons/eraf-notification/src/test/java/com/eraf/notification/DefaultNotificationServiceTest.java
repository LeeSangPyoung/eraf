package com.eraf.notification;

import com.eraf.notification.email.EmailSender;
import com.eraf.notification.impl.DefaultNotificationService;
import com.eraf.notification.push.PushSender;
import com.eraf.notification.sms.SmsSender;
import com.eraf.notification.template.NotificationTemplate;
import com.eraf.notification.template.NotificationTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultNotificationServiceTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private SmsSender smsSender;

    @Mock
    private PushSender fcmPushSender;

    @Mock
    private PushSender apnsPushSender;

    @Mock
    private NotificationTemplateService templateService;

    @Nested
    @DisplayName("이메일 발송")
    class EmailTests {

        @Test
        @DisplayName("일반 이메일 발송")
        void testSendEmail() {
            DefaultNotificationService service =
                    new DefaultNotificationService(emailSender, smsSender, fcmPushSender, apnsPushSender);

            NotificationService.EmailRequest request = NotificationService.EmailRequest.builder()
                    .to("test@example.com")
                    .subject("Test Subject")
                    .body("Test Body");

            service.sendEmail(request);

            verify(emailSender, times(1)).send(request);
        }

        @Test
        @DisplayName("이메일 미구성 시 예외")
        void testSendEmailNotConfigured() {
            DefaultNotificationService service =
                    new DefaultNotificationService(null, smsSender, null, null);

            NotificationService.EmailRequest request = NotificationService.EmailRequest.builder()
                    .to("test@example.com")
                    .subject("Test")
                    .body("Body");

            assertThrows(UnsupportedOperationException.class, () -> service.sendEmail(request));
        }

        @Test
        @DisplayName("템플릿을 사용한 이메일 발송")
        void testSendEmailWithTemplate() {
            DefaultNotificationService service =
                    new DefaultNotificationService(emailSender, smsSender, null, null, templateService);

            NotificationTemplateService.RenderedTemplate rendered =
                    new NotificationTemplateService.RenderedTemplate(
                            "주문 확인", "<h1>주문번호: ORD-001</h1>", true,
                            NotificationTemplate.TemplateType.EMAIL);
            when(templateService.render(eq("ORDER_CONFIRM"), any())).thenReturn(rendered);

            NotificationService.EmailRequest request = NotificationService.EmailRequest.builder()
                    .to("customer@example.com")
                    .template("ORDER_CONFIRM", Map.of("orderNumber", "ORD-001"));

            service.sendEmail(request);

            ArgumentCaptor<NotificationService.EmailRequest> captor =
                    ArgumentCaptor.forClass(NotificationService.EmailRequest.class);
            verify(emailSender).send(captor.capture());

            NotificationService.EmailRequest sent = captor.getValue();
            assertEquals("주문 확인", sent.getSubject());
            assertEquals("<h1>주문번호: ORD-001</h1>", sent.getBody());
            assertTrue(sent.isHtml());
        }

        @Test
        @DisplayName("템플릿 ID가 있어도 기존 subject는 유지")
        void testTemplateDoesNotOverrideExistingSubject() {
            DefaultNotificationService service =
                    new DefaultNotificationService(emailSender, null, null, null, templateService);

            NotificationTemplateService.RenderedTemplate rendered =
                    new NotificationTemplateService.RenderedTemplate(
                            "템플릿 제목", "템플릿 본문", false, NotificationTemplate.TemplateType.EMAIL);
            when(templateService.render(eq("TPL"), any())).thenReturn(rendered);

            NotificationService.EmailRequest request = NotificationService.EmailRequest.builder()
                    .to("user@example.com")
                    .subject("기존 제목")
                    .template("TPL", Map.of());

            service.sendEmail(request);

            ArgumentCaptor<NotificationService.EmailRequest> captor =
                    ArgumentCaptor.forClass(NotificationService.EmailRequest.class);
            verify(emailSender).send(captor.capture());

            assertEquals("기존 제목", captor.getValue().getSubject());
        }

        @Test
        @DisplayName("템플릿 서비스 없이 templateId 지정 시 무시")
        void testTemplateServiceNotAvailable() {
            DefaultNotificationService service =
                    new DefaultNotificationService(emailSender, null, null, null);

            NotificationService.EmailRequest request = NotificationService.EmailRequest.builder()
                    .to("user@example.com")
                    .subject("제목")
                    .body("본문")
                    .template("SOME_TPL", Map.of());

            service.sendEmail(request);

            verify(emailSender).send(request);
        }
    }

    @Nested
    @DisplayName("SMS 발송")
    class SmsTests {

        @Test
        @DisplayName("일반 SMS 발송")
        void testSendSms() {
            DefaultNotificationService service =
                    new DefaultNotificationService(null, smsSender, null, null);

            NotificationService.SmsRequest request = NotificationService.SmsRequest.builder()
                    .to("010-1234-5678")
                    .message("Test Message");

            service.sendSms(request);

            verify(smsSender, times(1)).send(request);
        }

        @Test
        @DisplayName("SMS 미구성 시 예외")
        void testSendSmsNotConfigured() {
            DefaultNotificationService service =
                    new DefaultNotificationService(emailSender, null, null, null);

            NotificationService.SmsRequest request = NotificationService.SmsRequest.builder()
                    .to("010-1234-5678")
                    .message("Test");

            assertThrows(UnsupportedOperationException.class, () -> service.sendSms(request));
        }

        @Test
        @DisplayName("템플릿을 사용한 SMS 발송")
        void testSendSmsWithTemplate() {
            DefaultNotificationService service =
                    new DefaultNotificationService(null, smsSender, null, null, templateService);

            NotificationTemplateService.RenderedTemplate rendered =
                    new NotificationTemplateService.RenderedTemplate(
                            null, "[ERAF] 인증번호: 123456", false,
                            NotificationTemplate.TemplateType.SMS);
            when(templateService.render(eq("AUTH_CODE"), any())).thenReturn(rendered);

            NotificationService.SmsRequest request = NotificationService.SmsRequest.builder()
                    .to("010-1234-5678")
                    .template("AUTH_CODE", Map.of("code", "123456"));

            service.sendSms(request);

            ArgumentCaptor<NotificationService.SmsRequest> captor =
                    ArgumentCaptor.forClass(NotificationService.SmsRequest.class);
            verify(smsSender).send(captor.capture());

            assertEquals("[ERAF] 인증번호: 123456", captor.getValue().getMessage());
        }
    }

    @Nested
    @DisplayName("푸시 알림 발송")
    class PushTests {

        @Test
        @DisplayName("FCM 푸시 발송")
        void testSendFcmPush() {
            DefaultNotificationService service =
                    new DefaultNotificationService(null, null, fcmPushSender, null);

            NotificationService.PushRequest request = NotificationService.PushRequest.builder()
                    .token("device-token")
                    .title("Title")
                    .body("Body")
                    .platform(NotificationService.PushRequest.PushPlatform.FCM);

            service.sendPush(request);

            verify(fcmPushSender, times(1)).send(request);
        }

        @Test
        @DisplayName("APNs 푸시 발송")
        void testSendApnsPush() {
            DefaultNotificationService service =
                    new DefaultNotificationService(null, null, null, apnsPushSender);

            NotificationService.PushRequest request = NotificationService.PushRequest.builder()
                    .token("device-token")
                    .title("Title")
                    .body("Body")
                    .platform(NotificationService.PushRequest.PushPlatform.APNS);

            service.sendPush(request);

            verify(apnsPushSender, times(1)).send(request);
        }

        @Test
        @DisplayName("ALL 플랫폼 - 양쪽 모두 발송")
        void testSendPushAll() {
            DefaultNotificationService service =
                    new DefaultNotificationService(null, null, fcmPushSender, apnsPushSender);

            NotificationService.PushRequest request = NotificationService.PushRequest.builder()
                    .token("device-token")
                    .title("Title")
                    .body("Body")
                    .platform(NotificationService.PushRequest.PushPlatform.ALL);

            service.sendPush(request);

            verify(fcmPushSender).send(request);
            verify(apnsPushSender).send(request);
        }
    }
}
