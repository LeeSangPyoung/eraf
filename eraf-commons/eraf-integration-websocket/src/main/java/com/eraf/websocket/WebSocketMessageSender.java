package com.eraf.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessageSender {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessageSender(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendToTopic(String topic, Object message) {
        messagingTemplate.convertAndSend("/topic/" + topic, message);
    }

    public void sendToUser(String username, String destination, Object message) {
        messagingTemplate.convertAndSendToUser(username, destination, message);
    }

    public void sendToQueue(String queue, Object message) {
        messagingTemplate.convertAndSend("/queue/" + queue, message);
    }
}
