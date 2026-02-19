package com.eraf.websocket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketMessageSender 단위 테스트")
class WebSocketMessageSenderTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketMessageSender sender;

    @Test
    @DisplayName("sendToTopic - /topic/ 접두사를 붙여 convertAndSend를 호출한다")
    void sendToTopic_callsConvertAndSendWithTopicPrefix() {
        // given
        String topic = "notifications";
        String message = "hello";

        // when
        sender.sendToTopic(topic, message);

        // then
        verify(messagingTemplate).convertAndSend("/topic/notifications", (Object) message);
    }

    @Test
    @DisplayName("sendToUser - 사용자에게 메시지를 전송한다")
    void sendToUser_callsConvertAndSendToUser() {
        // given
        String username = "user123";
        String destination = "/queue/messages";
        String message = "private message";

        // when
        sender.sendToUser(username, destination, message);

        // then
        verify(messagingTemplate).convertAndSendToUser("user123", "/queue/messages", (Object) message);
    }

    @Test
    @DisplayName("sendToQueue - /queue/ 접두사를 붙여 convertAndSend를 호출한다")
    void sendToQueue_callsConvertAndSendWithQueuePrefix() {
        // given
        String queue = "tasks";
        Map<String, String> message = Map.of("task", "build");

        // when
        sender.sendToQueue(queue, message);

        // then
        verify(messagingTemplate).convertAndSend("/queue/tasks", (Object) message);
    }
}
