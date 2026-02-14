package com.eraf.crypto.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ErafEncryptionProperties - 암호화 설정")
class ErafEncryptionPropertiesTest {

    @Test
    @DisplayName("기본값")
    void defaults() {
        ErafEncryptionProperties props = new ErafEncryptionProperties();
        assertTrue(props.isEnabled());
        assertEquals("PBEWITHHMACSHA512ANDAES_256", props.getAlgorithm());
        assertEquals(1000, props.getKeyObtentionIterations());
        assertEquals(1, props.getPoolSize());
        assertEquals("org.jasypt.salt.RandomSaltGenerator", props.getSaltGeneratorClassname());
        assertEquals("org.jasypt.iv.RandomIvGenerator", props.getIvGeneratorClassname());
        assertEquals("base64", props.getStringOutputType());
        assertEquals("JASYPT_ENCRYPTOR_PASSWORD", props.getPasswordEnvName());
        assertEquals("jasypt.encryptor.password", props.getPasswordPropertyName());
        assertEquals("ENC(", props.getPrefix());
        assertEquals(")", props.getSuffix());
    }

    @Test
    @DisplayName("setter/getter")
    void setterGetter() {
        ErafEncryptionProperties props = new ErafEncryptionProperties();
        props.setEnabled(false);
        props.setAlgorithm("AES");
        props.setKeyObtentionIterations(5000);
        props.setPoolSize(4);
        props.setSaltGeneratorClassname("custom.SaltGen");
        props.setIvGeneratorClassname("custom.IvGen");
        props.setStringOutputType("hex");
        props.setPasswordEnvName("MY_PASSWORD");
        props.setPasswordPropertyName("my.password");
        props.setPrefix("DEC(");
        props.setSuffix("]");

        assertFalse(props.isEnabled());
        assertEquals("AES", props.getAlgorithm());
        assertEquals(5000, props.getKeyObtentionIterations());
        assertEquals(4, props.getPoolSize());
        assertEquals("custom.SaltGen", props.getSaltGeneratorClassname());
        assertEquals("custom.IvGen", props.getIvGeneratorClassname());
        assertEquals("hex", props.getStringOutputType());
        assertEquals("MY_PASSWORD", props.getPasswordEnvName());
        assertEquals("my.password", props.getPasswordPropertyName());
        assertEquals("DEC(", props.getPrefix());
        assertEquals("]", props.getSuffix());
    }
}
