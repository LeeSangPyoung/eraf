package com.eraf.event;

import com.eraf.messaging.MessageListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("이벤트/메시징 어노테이션 테스트")
class AnnotationTest {

    // ========== @EventHandler ==========
    @Test
    @DisplayName("@EventHandler - @EventListener 메타 어노테이션 포함")
    void eventHandlerHasEventListener() {
        assertTrue(EventHandler.class.isAnnotationPresent(EventListener.class));
    }

    @Test
    @DisplayName("@EventHandler - 기본값 확인")
    void eventHandlerDefaults() throws NoSuchMethodException {
        EventHandler annotation = AnnotationTest.class
                .getDeclaredMethod("eventHandlerMethod")
                .getAnnotation(EventHandler.class);
        assertEquals(0, annotation.value().length);
        assertEquals("", annotation.condition());
    }

    @EventHandler
    void eventHandlerMethod() {}

    // ========== @AsyncEventHandler ==========
    @Test
    @DisplayName("@AsyncEventHandler - @Async 메타 어노테이션 포함")
    void asyncEventHandlerHasAsync() {
        assertTrue(AsyncEventHandler.class.isAnnotationPresent(Async.class));
    }

    @Test
    @DisplayName("@AsyncEventHandler - @EventListener 메타 어노테이션 포함")
    void asyncEventHandlerHasEventListener() {
        assertTrue(AsyncEventHandler.class.isAnnotationPresent(EventListener.class));
    }

    @Test
    @DisplayName("@AsyncEventHandler - 기본값 확인")
    void asyncEventHandlerDefaults() throws NoSuchMethodException {
        AsyncEventHandler annotation = AnnotationTest.class
                .getDeclaredMethod("asyncEventHandlerMethod")
                .getAnnotation(AsyncEventHandler.class);
        assertEquals(0, annotation.value().length);
        assertEquals("", annotation.condition());
        assertEquals("", annotation.executor());
    }

    @AsyncEventHandler
    void asyncEventHandlerMethod() {}

    // ========== @AfterCommit ==========
    @Test
    @DisplayName("@AfterCommit - @TransactionalEventListener 메타 어노테이션 포함")
    void afterCommitHasTransactionalEventListener() {
        assertTrue(AfterCommit.class.isAnnotationPresent(TransactionalEventListener.class));
    }

    @Test
    @DisplayName("@AfterCommit - 기본값 확인")
    void afterCommitDefaults() throws NoSuchMethodException {
        AfterCommit annotation = AnnotationTest.class
                .getDeclaredMethod("afterCommitMethod")
                .getAnnotation(AfterCommit.class);
        assertEquals(0, annotation.value().length);
        assertEquals("", annotation.condition());
        assertFalse(annotation.fallbackExecution());
    }

    @AfterCommit
    void afterCommitMethod() {}

    // ========== @MessageListener ==========
    @Test
    @DisplayName("@MessageListener - 속성 확인")
    void messageListenerAttributes() throws NoSuchMethodException {
        MessageListener annotation = AnnotationTest.class
                .getDeclaredMethod("messageListenerMethod")
                .getAnnotation(MessageListener.class);
        assertEquals("my-topic", annotation.destination());
        assertEquals("", annotation.group());
        assertEquals(1, annotation.concurrency());
        assertEquals("", annotation.errorHandler());
    }

    @Test
    @DisplayName("@MessageListener - 커스텀 속성")
    void messageListenerCustom() throws NoSuchMethodException {
        MessageListener annotation = AnnotationTest.class
                .getDeclaredMethod("customListenerMethod")
                .getAnnotation(MessageListener.class);
        assertEquals("orders", annotation.destination());
        assertEquals("order-group", annotation.group());
        assertEquals(3, annotation.concurrency());
        assertEquals("myErrorHandler", annotation.errorHandler());
    }

    @MessageListener(destination = "my-topic")
    void messageListenerMethod() {}

    @MessageListener(destination = "orders", group = "order-group", concurrency = 3, errorHandler = "myErrorHandler")
    void customListenerMethod() {}
}
