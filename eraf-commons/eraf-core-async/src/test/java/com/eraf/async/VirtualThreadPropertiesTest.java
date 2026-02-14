package com.eraf.async;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VirtualThreadProperties - Virtual Thread 설정")
class VirtualThreadPropertiesTest {

    @Test
    @DisplayName("기본값")
    void defaults() {
        VirtualThreadProperties props = new VirtualThreadProperties();
        assertTrue(props.isEnabled());
        assertEquals("virtual-", props.getNamePrefix());
        assertEquals(0, props.getStackSize());
    }

    @Test
    @DisplayName("setter/getter")
    void setterGetter() {
        VirtualThreadProperties props = new VirtualThreadProperties();
        props.setEnabled(false);
        props.setNamePrefix("custom-");
        props.setStackSize(1024);

        assertFalse(props.isEnabled());
        assertEquals("custom-", props.getNamePrefix());
        assertEquals(1024, props.getStackSize());
    }
}
