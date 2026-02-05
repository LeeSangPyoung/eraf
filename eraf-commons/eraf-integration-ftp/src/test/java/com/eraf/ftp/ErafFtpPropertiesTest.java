package com.eraf.ftp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErafFtpPropertiesTest {

    @Test
    @DisplayName("기본값 확인")
    void testDefaultValues() {
        // Given
        ErafFtpProperties properties = new ErafFtpProperties();

        // Then
        assertNotNull(properties);
        assertEquals(21, properties.getPort());
    }

    @Test
    @DisplayName("FTP 서버 설정")
    void testServerSettings() {
        // Given
        ErafFtpProperties properties = new ErafFtpProperties();

        // When
        properties.setHost("ftp.example.com");
        properties.setPort(2121);
        properties.setUsername("user");
        properties.setPassword("pass");

        // Then
        assertEquals("ftp.example.com", properties.getHost());
        assertEquals(2121, properties.getPort());
        assertEquals("user", properties.getUsername());
    }

    @Test
    @DisplayName("SFTP 모드 설정")
    void testSftpMode() {
        // Given
        ErafFtpProperties properties = new ErafFtpProperties();

        // When
        properties.setUseSftp(true);
        properties.setPort(22);

        // Then
        assertTrue(properties.isUseSftp());
        assertEquals(22, properties.getPort());
    }
}
