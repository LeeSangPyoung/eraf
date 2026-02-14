package com.eraf.async;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AsyncTask - 비동기 작업 관리")
class AsyncTaskTest {

    @Test
    @DisplayName("submit - Supplier로 비동기 작업 제출")
    void submitSupplier() throws ExecutionException, InterruptedException, TimeoutException {
        AsyncTask<String> task = AsyncTask.submit(() -> "hello");

        String result = task.getResult(5, TimeUnit.SECONDS);
        assertEquals("hello", result);
        assertTrue(task.isDone());
        assertEquals("COMPLETED", task.getStatus());
        assertEquals(100, task.getProgress());
        assertNotNull(task.getTaskId());
    }

    @Test
    @DisplayName("submit - 실패 시 FAILED 상태")
    void submitFailure() throws InterruptedException {
        AsyncTask<String> task = AsyncTask.submit(() -> {
            throw new RuntimeException("test error");
        });

        // 완료될 때까지 대기
        Thread.sleep(500);

        assertTrue(task.isDone());
        assertEquals("FAILED", task.getStatus());
        assertNotNull(task.getMessage());
    }

    @Test
    @DisplayName("submit - ProgressCallback으로 진행률 관리")
    void submitWithProgress() throws ExecutionException, InterruptedException, TimeoutException {
        AsyncTask<String> task = AsyncTask.submit(callback -> {
            callback.setProgress(50);
            callback.setMessage("절반 완료");
            callback.complete("done");
        });

        String result = task.getResult(5, TimeUnit.SECONDS);
        assertEquals("done", result);
        assertEquals("COMPLETED", task.getStatus());
        assertEquals(100, task.getProgress());
    }

    @Test
    @DisplayName("submit - ProgressCallback 실패 처리")
    void submitWithProgressFailure() throws InterruptedException {
        AsyncTask<String> task = AsyncTask.submit(callback -> {
            callback.setProgress(30);
            callback.fail(new RuntimeException("progress error"));
        });

        Thread.sleep(500);

        assertTrue(task.isDone());
        assertEquals("FAILED", task.getStatus());
        assertEquals("progress error", task.getMessage());
    }

    @Test
    @DisplayName("submit - 예외 발생 시 callback.fail 자동 호출")
    void submitWithProgressException() throws InterruptedException {
        AsyncTask<String> task = AsyncTask.submit(callback -> {
            throw new RuntimeException("unexpected");
        });

        Thread.sleep(500);

        assertTrue(task.isDone());
        assertEquals("FAILED", task.getStatus());
    }

    @Test
    @DisplayName("cancel - 작업 취소")
    void cancel() {
        AsyncTask<String> task = AsyncTask.submit(() -> {
            try { Thread.sleep(10000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "never";
        });

        boolean cancelled = task.cancel();
        assertTrue(cancelled);
        assertEquals("CANCELLED", task.getStatus());
    }

    @Test
    @DisplayName("get - ID로 작업 조회")
    void getById() {
        AsyncTask<String> task = AsyncTask.submit(() -> "test");

        Optional<AsyncTask<String>> found = AsyncTask.get(task.getTaskId());
        assertTrue(found.isPresent());
        assertEquals(task.getTaskId(), found.get().getTaskId());
    }

    @Test
    @DisplayName("get - 없는 ID 조회 시 empty")
    void getByIdNotFound() {
        Optional<AsyncTask<String>> found = AsyncTask.get("non-existent");
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("cleanup - 완료된 작업 정리")
    void cleanup() throws InterruptedException {
        AsyncTask<String> task = AsyncTask.submit(() -> "done");
        Thread.sleep(500);

        assertTrue(task.isDone());
        AsyncTask.cleanup();

        Optional<AsyncTask<String>> found = AsyncTask.get(task.getTaskId());
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("getResult - 타임아웃")
    void getResultTimeout() {
        AsyncTask<String> task = AsyncTask.submit(() -> {
            try { Thread.sleep(10000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "never";
        });

        assertThrows(TimeoutException.class,
                () -> task.getResult(100, TimeUnit.MILLISECONDS));
        task.cancel();
    }

    @Test
    @DisplayName("getResult - 블로킹 호출")
    void getResultBlocking() throws ExecutionException, InterruptedException {
        AsyncTask<Integer> task = AsyncTask.submit(() -> 42);
        Integer result = task.getResult();
        assertEquals(42, result);
    }
}
