package com.eraf.grpc.interceptor;

import io.grpc.*;

/**
 * gRPC 클라이언트 메타데이터 인터셉터
 *
 * 클라이언트 호출 시 traceId, userId 등 컨텍스트 정보를 메타데이터에 추가합니다.
 */
public class ClientMetadataInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Context에서 메타데이터 전파
                String traceId = MetadataPropagationInterceptor.TRACE_ID_CTX.get();
                if (traceId != null) {
                    headers.put(MetadataPropagationInterceptor.TRACE_ID_KEY, traceId);
                }

                String userId = MetadataPropagationInterceptor.USER_ID_CTX.get();
                if (userId != null) {
                    headers.put(MetadataPropagationInterceptor.USER_ID_KEY, userId);
                }

                String tenantId = MetadataPropagationInterceptor.TENANT_ID_CTX.get();
                if (tenantId != null) {
                    headers.put(MetadataPropagationInterceptor.TENANT_ID_KEY, tenantId);
                }

                super.start(responseListener, headers);
            }
        };
    }
}
