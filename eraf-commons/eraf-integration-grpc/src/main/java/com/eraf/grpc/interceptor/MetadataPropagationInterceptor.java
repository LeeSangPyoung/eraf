package com.eraf.grpc.interceptor;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gRPC 메타데이터 전파 인터셉터
 *
 * 서버 측에서 수신한 메타데이터(traceId, userId 등)를 ThreadLocal에 저장합니다.
 */
public class MetadataPropagationInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(MetadataPropagationInterceptor.class);

    public static final Metadata.Key<String> TRACE_ID_KEY =
            Metadata.Key.of("x-trace-id", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> USER_ID_KEY =
            Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> TENANT_ID_KEY =
            Metadata.Key.of("x-tenant-id", Metadata.ASCII_STRING_MARSHALLER);

    public static final Context.Key<String> TRACE_ID_CTX = Context.key("traceId");
    public static final Context.Key<String> USER_ID_CTX = Context.key("userId");
    public static final Context.Key<String> TENANT_ID_CTX = Context.key("tenantId");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        Context ctx = Context.current();

        String traceId = headers.get(TRACE_ID_KEY);
        if (traceId != null) {
            ctx = ctx.withValue(TRACE_ID_CTX, traceId);
        }

        String userId = headers.get(USER_ID_KEY);
        if (userId != null) {
            ctx = ctx.withValue(USER_ID_CTX, userId);
        }

        String tenantId = headers.get(TENANT_ID_KEY);
        if (tenantId != null) {
            ctx = ctx.withValue(TENANT_ID_CTX, tenantId);
        }

        return Contexts.interceptCall(ctx, call, headers, next);
    }
}
