# ERAF Integration - TCP

Netty 기반 TCP 클라이언트를 지원하는 모듈입니다.

## 기능

- **비동기 TCP 클라이언트**: Netty 기반 고성능 네트워크 통신
- **자동 재연결**: 연결 실패 시 자동 재시도
- **이벤트 기반**: 메시지 수신 및 연결 상태 리스너
- **동기/비동기 전송**: 요청-응답 및 Fire-and-Forget 모두 지원
- **하트비트**: Keep-alive 메커니즘

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-integration-tcp</artifactId>
</dependency>
```

## 설정

### application.yml

```yaml
eraf:
  tcp:
    host: localhost
    port: 8080
    connect-timeout: 5000
    read-timeout: 30000
    auto-reconnect: true
    reconnect-delay: 1000
    heartbeat-interval: 30000
```

## 사용법

### 1. 기본 사용

```java
import com.eraf.tcp.TcpClient;
import com.eraf.tcp.impl.NettyTcpClient;
import org.springframework.stereotype.Service;

@Service
public class TcpService {

    private final TcpClient tcpClient;

    public TcpService(ErafTcpProperties properties) {
        this.tcpClient = new NettyTcpClient(properties);
    }

    public void initialize() {
        tcpClient.connect()
            .thenRun(() -> System.out.println("Connected to TCP server"))
            .exceptionally(ex -> {
                System.err.println("Connection failed: " + ex.getMessage());
                return null;
            });
    }

    public void sendMessage(String message) {
        tcpClient.send(message)
            .thenRun(() -> System.out.println("Message sent"))
            .exceptionally(ex -> {
                System.err.println("Send failed: " + ex.getMessage());
                return null;
            });
    }

    public void shutdown() {
        tcpClient.disconnect();
    }
}
```

### 2. 메시지 수신 리스너

```java
@PostConstruct
public void setupListeners() {
    // 메시지 수신 리스너
    tcpClient.onMessage(data -> {
        String message = new String(data, StandardCharsets.UTF_8);
        System.out.println("Received: " + message);
        handleIncomingMessage(message);
    });

    // 연결 상태 변경 리스너
    tcpClient.onConnectionStateChange(state -> {
        System.out.println("Connection state: " + state);
        if (state == ConnectionState.CONNECTED) {
            onConnected();
        } else if (state == ConnectionState.DISCONNECTED) {
            onDisconnected();
        }
    });
}
```

### 3. 동기 전송 (요청-응답)

```java
public String sendAndReceive(String request) {
    byte[] requestData = request.getBytes(StandardCharsets.UTF_8);
    byte[] responseData = tcpClient.sendAndReceive(requestData, 5000);
    return new String(responseData, StandardCharsets.UTF_8);
}

public String queryData(String query) {
    String request = "QUERY:" + query;
    String response = sendAndReceive(request);
    return parseResponse(response);
}
```

### 4. 바이너리 데이터 전송

```java
public void sendBinary(byte[] data) {
    tcpClient.send(data)
        .thenRun(() -> log.info("Binary data sent"))
        .exceptionally(ex -> {
            log.error("Failed to send binary data", ex);
            return null;
        });
}

public byte[] sendBinaryAndReceive(byte[] request) {
    return tcpClient.sendAndReceive(request, 10000);
}
```

## 프로토콜 구현

### 1. 길이 헤더 프로토콜

```java
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class LengthHeaderProtocol {

    public byte[] encode(String message) {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + messageBytes.length);
        buffer.putInt(messageBytes.length);
        buffer.put(messageBytes);
        return buffer.array();
    }

    public String decode(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int length = buffer.getInt();
        byte[] messageBytes = new byte[length];
        buffer.get(messageBytes);
        return new String(messageBytes, StandardCharsets.UTF_8);
    }
}
```

### 2. 구분자 기반 프로토콜

```java
public class DelimiterProtocol {

    private static final String DELIMITER = "\r\n";

    public byte[] encode(String message) {
        return (message + DELIMITER).getBytes(StandardCharsets.UTF_8);
    }

    public String decode(byte[] data) {
        String message = new String(data, StandardCharsets.UTF_8);
        return message.replace(DELIMITER, "");
    }
}
```

### 3. JSON 프로토콜

```java
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonProtocol {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] encode(Object message) throws Exception {
        return objectMapper.writeValueAsBytes(message);
    }

    public <T> T decode(byte[] data, Class<T> clazz) throws Exception {
        return objectMapper.readValue(data, clazz);
    }
}
```

## 고급 기능

### 1. 커스텀 핸들러

```java
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class CustomMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        byte[] data = new byte[msg.readableBytes()];
        msg.readBytes(data);

        // 커스텀 메시지 처리
        handleMessage(data);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Handler exception", cause);
        ctx.close();
    }
}
```

### 2. SSL/TLS 지원

```yaml
eraf:
  tcp:
    ssl:
      enabled: true
      cert-path: /path/to/cert.pem
      key-path: /path/to/key.pem
      trust-cert-path: /path/to/ca.pem
```

```java
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public TcpClient createSecureClient(ErafTcpProperties properties) {
    SslContext sslContext = SslContextBuilder.forClient()
        .trustManager(new File(properties.getSsl().getTrustCertPath()))
        .build();

    return new NettyTcpClient(properties, sslContext);
}
```

### 3. 연결 풀

```java
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TcpConnectionPool {

    private final Map<String, TcpClient> connections = new ConcurrentHashMap<>();

    public TcpClient getConnection(String host, int port) {
        String key = host + ":" + port;
        return connections.computeIfAbsent(key, k -> {
            TcpClient client = createClient(host, port);
            client.connect();
            return client;
        });
    }

    public void releaseConnection(String host, int port) {
        String key = host + ":" + port;
        TcpClient client = connections.remove(key);
        if (client != null) {
            client.disconnect();
        }
    }
}
```

### 4. 하트비트

```java
public class HeartbeatSender {

    private final TcpClient tcpClient;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void startHeartbeat() {
        scheduler.scheduleAtFixedRate(() -> {
            if (tcpClient.isConnected()) {
                tcpClient.send("PING".getBytes());
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public void stopHeartbeat() {
        scheduler.shutdown();
    }
}
```

## 에러 처리

### 1. 재연결 로직

```java
public class ResilientTcpClient {

    private final TcpClient tcpClient;
    private final int maxRetries = 5;

    public void connectWithRetry() {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                tcpClient.connect().get(5, TimeUnit.SECONDS);
                log.info("Connected successfully");
                return;
            } catch (Exception e) {
                attempts++;
                log.warn("Connection attempt {} failed", attempts);
                if (attempts < maxRetries) {
                    sleep(1000 * attempts);  // Exponential backoff
                }
            }
        }
        throw new RuntimeException("Failed to connect after " + maxRetries + " attempts");
    }
}
```

### 2. 타임아웃 처리

```java
import java.util.concurrent.TimeoutException;

public String sendWithTimeout(String message, long timeout) {
    try {
        byte[] request = message.getBytes();
        byte[] response = tcpClient.sendAndReceive(request, timeout);
        return new String(response);
    } catch (TimeoutException e) {
        log.error("Request timed out after {}ms", timeout);
        throw new BusinessException("TCP request timeout");
    }
}
```

## 실전 예제

### 1. ERP 시스템 연동

```java
@Service
public class ErpIntegrationService {

    private final TcpClient erpClient;

    public OrderResponse createOrder(OrderRequest request) {
        // 1. 요청 메시지 생성
        String message = buildErpMessage("CREATE_ORDER", request);

        // 2. 전송 및 응답 수신
        String response = sendAndReceive(message);

        // 3. 응답 파싱
        return parseOrderResponse(response);
    }

    private String buildErpMessage(String command, Object payload) {
        // STX(0x02) + LENGTH + COMMAND + PAYLOAD + ETX(0x03) + CHECKSUM
        String json = objectMapper.writeValueAsString(payload);
        return String.format("%c%04d%s%s%c%02x",
            0x02, json.length(), command, json, 0x03, calculateChecksum(json));
    }
}
```

### 2. IoT 디바이스 통신

```java
@Service
public class IoTDeviceService {

    private final TcpClient deviceClient;

    @PostConstruct
    public void initialize() {
        deviceClient.onMessage(data -> {
            DeviceMessage message = parseDeviceMessage(data);
            handleDeviceData(message);
        });
    }

    public void sendCommand(String deviceId, String command) {
        String message = String.format("%s:%s", deviceId, command);
        deviceClient.send(message.getBytes());
    }

    private void handleDeviceData(DeviceMessage message) {
        switch (message.getType()) {
            case "TELEMETRY":
                saveTelemetryData(message);
                break;
            case "ALERT":
                processAlert(message);
                break;
        }
    }
}
```

## 모범 사례

1. **연결 관리**: 애플리케이션 시작 시 연결, 종료 시 disconnect
2. **에러 처리**: 연결 실패 및 타임아웃에 대한 적절한 처리
3. **재연결**: auto-reconnect 활성화 및 지수 백오프 적용
4. **하트비트**: 장시간 유지 연결 시 주기적 ping 전송
5. **프로토콜**: 명확한 메시지 구분자 또는 길이 헤더 사용
6. **스레드 안전**: 여러 스레드에서 안전한 사용 보장
7. **리소스 정리**: 반드시 disconnect로 리소스 해제

## 참고

- [Netty Documentation](https://netty.io/wiki/index.html)
- [Netty User Guide](https://netty.io/wiki/user-guide.html)
- [TCP/IP Protocol](https://en.wikipedia.org/wiki/Transmission_Control_Protocol)
