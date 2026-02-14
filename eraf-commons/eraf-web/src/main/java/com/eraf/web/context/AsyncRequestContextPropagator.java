package com.eraf.web.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

import java.util.concurrent.Callable;

/**
 * 비동기 환경에서 RequestContext 전파
 * @Async, CompletableFuture 등에서 RequestContext를 유지
 */
public class AsyncRequestContextPropagator implements TaskDecorator {

    private static final Logger log = LoggerFactory.getLogger(AsyncRequestContextPropagator.class);

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        // 현재 스레드의 RequestContext 캡처
        RequestContext context = RequestContextHolder.getContext();

        return () -> {
            try {
                // 비동기 스레드에 RequestContext 설정
                if (context != null) {
                    RequestContextHolder.setContext(context);
                    MdcContextPropagator.propagate(context);
                    log.trace("RequestContext propagated to async thread: {}", context.getRequestId());
                }

                // 원래 작업 실행
                runnable.run();

            } finally {
                // 비동기 스레드 정리
                RequestContextHolder.clear();
                MdcContextPropagator.clear();
                log.trace("RequestContext cleared in async thread");
            }
        };
    }

    /**
     * Callable을 위한 데코레이터
     */
    public static <T> Callable<T> decorateCallable(Callable<T> callable) {
        // 현재 스레드의 RequestContext 캡처
        RequestContext context = RequestContextHolder.getContext();

        return () -> {
            try {
                // 비동기 스레드에 RequestContext 설정
                if (context != null) {
                    RequestContextHolder.setContext(context);
                    MdcContextPropagator.propagate(context);
                }

                // 원래 작업 실행
                return callable.call();

            } finally {
                // 비동기 스레드 정리
                RequestContextHolder.clear();
                MdcContextPropagator.clear();
            }
        };
    }

    /**
     * Runnable을 위한 데코레이터 (정적 메서드)
     */
    public static Runnable decorateRunnable(Runnable runnable) {
        // 현재 스레드의 RequestContext 캡처
        RequestContext context = RequestContextHolder.getContext();

        return () -> {
            try {
                // 비동기 스레드에 RequestContext 설정
                if (context != null) {
                    RequestContextHolder.setContext(context);
                    MdcContextPropagator.propagate(context);
                }

                // 원래 작업 실행
                runnable.run();

            } finally {
                // 비동기 스레드 정리
                RequestContextHolder.clear();
                MdcContextPropagator.clear();
            }
        };
    }

    /**
     * CompletableFuture에서 사용할 수 있는 헬퍼 메서드
     */
    public static void executeWithContext(Runnable runnable) {
        RequestContext context = RequestContextHolder.getContext();

        try {
            if (context != null) {
                RequestContextHolder.setContext(context);
                MdcContextPropagator.propagate(context);
            }
            runnable.run();
        } finally {
            RequestContextHolder.clear();
            MdcContextPropagator.clear();
        }
    }

    /**
     * CompletableFuture에서 사용할 수 있는 헬퍼 메서드 (반환값 있음)
     */
    public static <T> T executeWithContext(Callable<T> callable) throws Exception {
        RequestContext context = RequestContextHolder.getContext();

        try {
            if (context != null) {
                RequestContextHolder.setContext(context);
                MdcContextPropagator.propagate(context);
            }
            return callable.call();
        } finally {
            RequestContextHolder.clear();
            MdcContextPropagator.clear();
        }
    }
}
