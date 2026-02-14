# ERAF Integration - gRPC

gRPC 기반 고성능 서비스 간 통신을 지원하는 모듈입니다.

## 기능

- **Server AutoConfiguration**: gRPC 서버 자동 구성
- **Client AutoConfiguration**: gRPC 클라이언트 자동 구성
- **Interceptor 지원**: 로깅, 인증, 추적 인터셉터
- **Spring Integration**: Spring Boot 완벽 통합

## 의존성

```xml
<dependency>
    <groupId>com.eraf</groupId>
    <artifactId>eraf-integration-grpc</artifactId>
</dependency>
```

## 설정

### application.yml

```yaml
grpc:
  server:
    port: 9090
    max-inbound-message-size: 4MB

  client:
    GLOBAL:
      negotiation-type: PLAINTEXT

eraf:
  grpc:
    server:
      port: 9090
      max-message-size: 4194304  # 4MB
    client:
      default-timeout: 30000  # 30초
```

## 사용법

### 1. Proto 파일 정의

`src/main/proto/user_service.proto`:

```protobuf
syntax = "proto3";

option java_package = "com.example.grpc";
option java_multiple_files = true;

service UserService {
  rpc GetUser(UserRequest) returns (UserResponse);
  rpc ListUsers(Empty) returns (UserListResponse);
}

message UserRequest {
  string id = 1;
}

message UserResponse {
  string id = 1;
  string name = 2;
  string email = 3;
}

message Empty {}

message UserListResponse {
  repeated UserResponse users = 1;
}
```

### 2. gRPC Server 구현

```java
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    @Override
    public void getUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        UserResponse response = UserResponse.newBuilder()
            .setId(request.getId())
            .setName("John Doe")
            .setEmail("john@example.com")
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listUsers(Empty request, StreamObserver<UserListResponse> responseObserver) {
        UserListResponse response = UserListResponse.newBuilder()
            .addUsers(UserResponse.newBuilder()
                .setId("1")
                .setName("User 1")
                .build())
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
```

### 3. gRPC Client 사용

```java
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class UserClientService {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    public UserResponse getUser(String userId) {
        UserRequest request = UserRequest.newBuilder()
            .setId(userId)
            .build();

        return userServiceStub.getUser(request);
    }

    public List<UserResponse> listUsers() {
        Empty request = Empty.newBuilder().build();
        UserListResponse response = userServiceStub.listUsers(request);
        return response.getUsersList();
    }
}
```

### 4. Client 설정

```yaml
grpc:
  client:
    user-service:
      address: 'static://localhost:9090'
      negotiation-type: PLAINTEXT
```

## 인터셉터

### Server Interceptor

```java
import io.grpc.*;

@Component
@GrpcGlobalServerInterceptor
public class AuthInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String token = headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));

        if (token == null || !validateToken(token)) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid token"), new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }

        return next.startCall(call, headers);
    }
}
```

### Client Interceptor

```java
import io.grpc.*;

@Component
public class ClientAuthInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(
                    Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER),
                    "Bearer " + getToken()
                );
                super.start(responseListener, headers);
            }
        };
    }
}
```

## 스트리밍

### Server-side Streaming

```java
@Override
public void streamUsers(Empty request, StreamObserver<UserResponse> responseObserver) {
    for (User user : getAllUsers()) {
        UserResponse response = toUserResponse(user);
        responseObserver.onNext(response);

        // 스트리밍 제어
        Thread.sleep(100);
    }
    responseObserver.onCompleted();
}
```

### Bidirectional Streaming

```java
@Override
public StreamObserver<ChatMessage> chat(StreamObserver<ChatMessage> responseObserver) {
    return new StreamObserver<ChatMessage>() {
        @Override
        public void onNext(ChatMessage message) {
            // Echo back
            responseObserver.onNext(message);
        }

        @Override
        public void onError(Throwable t) {
            responseObserver.onError(t);
        }

        @Override
        public void onCompleted() {
            responseObserver.onCompleted();
        }
    };
}
```

## 에러 처리

```java
@GrpcService
public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    @Override
    public void getUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

            responseObserver.onNext(toUserResponse(user));
            responseObserver.onCompleted();

        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND
                .withDescription(e.getMessage())
                .asRuntimeException());

        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                .withDescription("Internal error")
                .asRuntimeException());
        }
    }
}
```

## 참고

- [gRPC Documentation](https://grpc.io/docs/)
- [grpc-spring-boot-starter](https://github.com/yidongnan/grpc-spring-boot-starter)
- [Protocol Buffers](https://protobuf.dev/)
