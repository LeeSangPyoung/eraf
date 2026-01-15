package com.eraf.core.async;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 비동기 작업 관리
 */
public class AsyncTask<T> {

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Map<String, TaskInfo<?>> tasks = new ConcurrentHashMap<>();

    private final String taskId;
    private final CompletableFuture<T> future;
    private final AtomicInteger progress = new AtomicInteger(0);
    private volatile String status = "PENDING";
    private volatile String message;

    private AsyncTask(String taskId, CompletableFuture<T> future) {
        this.taskId = taskId;
        this.future = future;
    }

    /**
     * 비동기 작업 제출
     */
    public static <T> AsyncTask<T> submit(Supplier<T> task) {
        String taskId = generateTaskId();
        CompletableFuture<T> future = CompletableFuture.supplyAsync(task, executor);

        AsyncTask<T> asyncTask = new AsyncTask<>(taskId, future);
        asyncTask.status = "RUNNING";

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                asyncTask.status = "FAILED";
                asyncTask.message = ex.getMessage();
            } else {
                asyncTask.status = "COMPLETED";
                asyncTask.progress.set(100);
            }
        });

        tasks.put(taskId, new TaskInfo<>(asyncTask));
        return asyncTask;
    }

    /**
     * 비동기 작업 제출 (진행률 콜백 포함)
     */
    public static <T> AsyncTask<T> submit(Consumer<ProgressCallback> task) {
        String taskId = generateTaskId();
        AsyncTask<T> asyncTask = new AsyncTask<>(taskId, new CompletableFuture<>());
        asyncTask.status = "RUNNING";

        ProgressCallback callback = new ProgressCallback() {
            @Override
            public void setProgress(int progress) {
                asyncTask.progress.set(Math.min(100, Math.max(0, progress)));
            }

            @Override
            public void setMessage(String message) {
                asyncTask.message = message;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void complete(Object result) {
                asyncTask.status = "COMPLETED";
                asyncTask.progress.set(100);
                ((CompletableFuture<T>) asyncTask.future).complete((T) result);
            }

            @Override
            public void fail(Throwable error) {
                asyncTask.status = "FAILED";
                asyncTask.message = error.getMessage();
                asyncTask.future.completeExceptionally(error);
            }
        };

        executor.submit(() -> {
            try {
                task.accept(callback);
            } catch (Exception e) {
                callback.fail(e);
            }
        });

        tasks.put(taskId, new TaskInfo<>(asyncTask));
        return asyncTask;
    }

    /**
     * 작업 ID 조회
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * 상태 조회
     */
    public String getStatus() {
        return status;
    }

    /**
     * 진행률 조회 (0-100)
     */
    public int getProgress() {
        return progress.get();
    }

    /**
     * 메시지 조회
     */
    public String getMessage() {
        return message;
    }

    /**
     * 완료 여부
     */
    public boolean isDone() {
        return future.isDone();
    }

    /**
     * 결과 조회 (블로킹)
     */
    public T getResult() throws ExecutionException, InterruptedException {
        return future.get();
    }

    /**
     * 결과 조회 (타임아웃)
     */
    public T getResult(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        return future.get(timeout, unit);
    }

    /**
     * 작업 취소
     */
    public boolean cancel() {
        boolean cancelled = future.cancel(true);
        if (cancelled) {
            status = "CANCELLED";
        }
        return cancelled;
    }

    /**
     * ID로 작업 조회
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<AsyncTask<T>> get(String taskId) {
        TaskInfo<?> info = tasks.get(taskId);
        return Optional.ofNullable(info).map(i -> (AsyncTask<T>) i.task);
    }

    /**
     * 완료된 작업 정리
     */
    public static void cleanup() {
        tasks.entrySet().removeIf(entry -> entry.getValue().task.isDone());
    }

    private static String generateTaskId() {
        return "task-" + System.currentTimeMillis() + "-" + ThreadLocalRandom.current().nextInt(10000);
    }

    /**
     * 진행률 콜백
     */
    public interface ProgressCallback {
        void setProgress(int progress);

        void setMessage(String message);

        void complete(Object result);

        void fail(Throwable error);
    }

    private record TaskInfo<T>(AsyncTask<T> task) {
    }
}
