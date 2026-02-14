package com.eraf.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CryptoException - 암호화 예외")
class CryptoExceptionTest {

    @Test
    @DisplayName("메시지만")
    void messageOnly() {
        CryptoException ex = new CryptoException("test error");
        assertEquals("test error", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    @DisplayName("메시지 + 원인")
    void messageWithCause() {
        Throwable cause = new RuntimeException("root cause");
        CryptoException ex = new CryptoException("test error", cause);
        assertEquals("test error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    @DisplayName("RuntimeException 상속")
    void isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new CryptoException("test"));
    }
}
