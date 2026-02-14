# ERAF WebSocket

Spring WebSocket + STOMP 기반의 실시간 양방향 통신 모듈입니다.

## 개요

WebSocket을 사용하여 클라이언트와 서버 간 실시간 양방향 통신을 구현합니다.

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-websocket</artifactId>
</dependency>
```

## 설정

### application.yml

```yaml
spring:
  websocket:
    allowed-origins:
      - "http://localhost:3000"
      - "https://app.example.com"
    heartbeat:
      incoming: 10000  # 클라이언트 heartbeat (ms)
      outgoing: 10000  # 서버 heartbeat (ms)
```

## 아키텍처

```
Client                  Server
  |                       |
  |-- CONNECT ----------->|
  |<-- CONNECTED ---------|
  |                       |
  |-- SUBSCRIBE /topic -->|
  |                       |
  |-- SEND /app/chat --->|
  |                       |
  |<-- MESSAGE /topic ----|
  |                       |
  |-- DISCONNECT -------->|
```

## 사용법

### 1. 서버 구성

#### Message Controller

```java
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 클라이언트 → 서버: /app/chat.send
     */
    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * 특정 사용자에게 메시지 전송
     */
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(
            @Payload ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        String recipient = message.getRecipient();
        messagingTemplate.convertAndSendToUser(
            recipient,
            "/queue/private",
            message
        );
    }

    /**
     * 사용자 입장 처리
     */
    @MessageMapping("/chat.join")
    @SendTo("/topic/public")
    public ChatMessage addUser(
            @Payload ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        // WebSocket 세션에 사용자 저장
        headerAccessor.getSessionAttributes().put("username", message.getSender());

        message.setType(MessageType.JOIN);
        return message;
    }
}
```

#### Event Listener

```java
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("New WebSocket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            log.info("User disconnected: {}", username);

            ChatMessage message = new ChatMessage();
            message.setType(MessageType.LEAVE);
            message.setSender(username);

            messagingTemplate.convertAndSend("/topic/public", message);
        }
    }
}
```

### 2. 클라이언트 구성 (JavaScript)

#### SockJS + STOMP 연결

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class WebSocketService {
  constructor() {
    this.stompClient = null;
  }

  connect(onMessageReceived) {
    const socket = new SockJS('http://localhost:8080/ws');
    this.stompClient = Stomp.over(socket);

    this.stompClient.connect({}, (frame) => {
      console.log('Connected:', frame);

      // Public 채널 구독
      this.stompClient.subscribe('/topic/public', (message) => {
        const payload = JSON.parse(message.body);
        onMessageReceived(payload);
      });

      // Private 채널 구독
      this.stompClient.subscribe('/user/queue/private', (message) => {
        const payload = JSON.parse(message.body);
        onMessageReceived(payload);
      });
    }, (error) => {
      console.error('Connection error:', error);
    });
  }

  sendMessage(message) {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send('/app/chat.send', {}, JSON.stringify(message));
    }
  }

  sendPrivateMessage(message) {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.send('/app/chat.private', {}, JSON.stringify(message));
    }
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect();
    }
  }
}

export default new WebSocketService();
```

#### React 사용 예시

```jsx
import React, { useEffect, useState } from 'react';
import webSocketService from './WebSocketService';

function ChatRoom() {
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');

  useEffect(() => {
    // WebSocket 연결
    webSocketService.connect((message) => {
      setMessages(prev => [...prev, message]);
    });

    // 컴포넌트 언마운트 시 연결 해제
    return () => {
      webSocketService.disconnect();
    };
  }, []);

  const sendMessage = () => {
    const message = {
      sender: 'user123',
      content: inputMessage,
      type: 'CHAT'
    };

    webSocketService.sendMessage(message);
    setInputMessage('');
  };

  return (
    <div>
      <div className="messages">
        {messages.map((msg, index) => (
          <div key={index}>{msg.sender}: {msg.content}</div>
        ))}
      </div>
      <input
        value={inputMessage}
        onChange={(e) => setInputMessage(e.target.value)}
        onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
      />
      <button onClick={sendMessage}>Send</button>
    </div>
  );
}
```

### 3. 실시간 알림 시스템

```java
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 모든 사용자에게 알림
     */
    public void broadcastNotification(String message) {
        messagingTemplate.convertAndSend("/topic/notifications",
            new Notification(message, LocalDateTime.now()));
    }

    /**
     * 특정 사용자에게 알림
     */
    public void sendNotificationToUser(String username, String message) {
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/notifications",
            new Notification(message, LocalDateTime.now())
        );
    }

    /**
     * 특정 그룹에게 알림
     */
    public void sendNotificationToGroup(String groupId, String message) {
        messagingTemplate.convertAndSend("/topic/group." + groupId,
            new Notification(message, LocalDateTime.now()));
    }
}
```

### 4. 실시간 대시보드 업데이트

```java
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 5000) // 5초마다
    public void pushDashboardUpdate() {
        DashboardData data = new DashboardData();
        data.setActiveUsers(userService.getActiveCount());
        data.setCpuUsage(systemMonitor.getCpuUsage());
        data.setMemoryUsage(systemMonitor.getMemoryUsage());
        data.setRequestsPerSecond(metricsService.getRPS());

        messagingTemplate.convertAndSend("/topic/dashboard", data);
    }

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 실시간 주문 알림
        messagingTemplate.convertAndSend("/topic/orders",
            new OrderNotification(event.getOrder()));
    }
}
```

## 주요 목적지 패턴

| 목적지 | 설명 | 예시 |
|--------|------|------|
| `/topic/*` | Pub/Sub 방식 (1:N) | `/topic/public`, `/topic/chat` |
| `/queue/*` | Point-to-Point (1:1) | `/queue/private` |
| `/user/*` | 특정 사용자 | `/user/queue/notifications` |
| `/app/*` | 클라이언트 → 서버 | `/app/chat.send` |

## 보안

```java
@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {

        messages
            .nullDestMatcher().authenticated()
            .simpSubscribeDestMatchers("/user/queue/**").authenticated()
            .simpDestMatchers("/app/**").authenticated()
            .simpSubscribeDestMatchers("/topic/public").permitAll()
            .anyMessage().denyAll();

        return messages.build();
    }
}
```

## 참고

- [Spring WebSocket Documentation](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [STOMP Protocol](https://stomp.github.io/)
- [SockJS](https://github.com/sockjs/sockjs-client)
